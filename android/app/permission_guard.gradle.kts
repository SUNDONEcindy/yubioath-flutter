import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension

// Build-time guard against permission creep in the shipped APK.
//
// Manifest merging folds every <uses-permission> declared by our transitive
// dependencies into the final APK, and each one surfaces on the Play Store
// listing. A dependency bump can therefore silently add a user-visible
// permission (this is exactly how androidx.media3, via CameraX, added
// ACCESS_NETWORK_STATE / "view network connections"). This task inspects the
// merged manifest of the release variant and fails the build if it contains
// any permission that is not on the allow-list below.
//
// When this fails you have two choices:
//   * The permission is unwanted -> strip it with tools:node="remove" in
//     app/src/main/AndroidManifest.xml (see ACCESS_NETWORK_STATE there).
//   * The permission is intentional -> add it to allowedPermissions with a
//     comment explaining why, so the addition is a conscious, reviewed change.

// Permissions we intentionally ship in the release APK.
val allowedPermissions =
    setOf(
        "android.permission.NFC", // talk to YubiKeys over NFC
        "android.permission.HIDE_OVERLAY_WINDOWS", // block tapjacking overlays
        "android.permission.CAMERA", // scan otpauth QR codes
    )

// AndroidX registers unexported runtime receivers behind a private,
// signature-level self-permission named
// "<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION". It is not
// user-visible and its prefix changes with the applicationId suffix (e.g.
// ".debug"), so allow it by suffix rather than by exact name.
val allowedPermissionSuffixes =
    setOf(
        ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
    )

val usesPermissionRegex = Regex("""(?s)<uses-permission(?:-sdk-23)?\b(.*?)>""")
val permissionNameRegex = Regex("""android:name\s*=\s*"([^"]+)"""")

extensions.configure<ApplicationAndroidComponentsExtension> {
    // Only the release variant ships to users; debug/profile builds get an
    // extra INTERNET permission injected by Flutter for hot reload, which must
    // never reach a release build.
    onVariants(selector().withBuildType("release")) { variant ->
        val mergedManifest = variant.artifacts.get(SingleArtifact.MERGED_MANIFEST)
        val variantName = variant.name
        val capitalizedName = variantName.replaceFirstChar { it.uppercase() }

        val checkTask =
            tasks.register("check${capitalizedName}Permissions") {
                group = "verification"
                description = "Fails the build if the $variantName APK requests an unexpected permission."
                // Consuming the artifact provider wires the implicit dependency on
                // the manifest-merging task and makes this check incremental.
                inputs.file(mergedManifest).withPropertyName("mergedManifest")

                doLast {
                    // uses-permission elements are self-closing, so the first '>'
                    // ends the tag; (?s) lets attributes span multiple lines.
                    val manifestText = mergedManifest.get().asFile.readText()
                    val declared =
                        usesPermissionRegex
                            .findAll(manifestText)
                            .mapNotNull { permissionNameRegex.find(it.groupValues[1])?.groupValues?.get(1) }
                            .toSortedSet()

                    val unexpected =
                        declared.filter { permission ->
                            permission !in allowedPermissions &&
                                allowedPermissionSuffixes.none { permission.endsWith(it) }
                        }

                    if (unexpected.isNotEmpty()) {
                        throw GradleException(
                            buildString {
                                appendLine("Permission guard: the $variantName APK requests unexpected permission(s):")
                                unexpected.forEach { appendLine("  - $it") }
                                appendLine()
                                appendLine("These are almost certainly pulled in transitively by a dependency and")
                                appendLine("would appear on the Play Store listing. If unwanted, strip them with")
                                appendLine("""tools:node="remove" in app/src/main/AndroidManifest.xml. If intentional,""")
                                append("add them to allowedPermissions in app/permission_guard.gradle.kts.")
                            },
                        )
                    }

                    logger.lifecycle(
                        "Permission guard: $variantName APK declares only expected permissions " +
                            "(${declared.joinToString(", ")}).",
                    )
                }
            }

        // Block APK assembly on the guard so `flutter build apk` (and CI) fail
        // when an unexpected permission slips in.
        afterEvaluate {
            tasks.named("assemble$capitalizedName").configure { dependsOn(checkTask) }
        }
    }
}
