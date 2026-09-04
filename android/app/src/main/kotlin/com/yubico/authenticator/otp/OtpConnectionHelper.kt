/*
 * Copyright (C) 2026 Yubico.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yubico.authenticator.otp

import com.yubico.authenticator.device.DeviceManager
import com.yubico.authenticator.yubikit.NfcState
import com.yubico.yubikit.android.transport.usb.UsbYubiKeyDevice
import com.yubico.yubikit.core.YubiKeyDevice
import com.yubico.yubikit.core.util.Result
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory

typealias OtpAction = (Result<YubiKeyDevice, Exception>) -> Unit

/**
 * Runs OTP operations against the connected YubiKey, deferring them until the key is tapped
 * when only NFC is available.
 *
 * Unlike [com.yubico.authenticator.piv.PivConnectionHelper] this hands the block a
 * [YubiKeyDevice] rather than an open connection, because the OTP application is reachable over
 * either a smart card or an OTP (HID) connection and the choice is made per device by
 * [OtpManager.withOtpSession].
 */
class OtpConnectionHelper(private val deviceManager: DeviceManager) {
    private var pendingAction: OtpAction? = null

    fun hasPending(): Boolean = pendingAction != null

    fun invokePending(device: YubiKeyDevice): Boolean {
        var requestHandled = true
        pendingAction?.let { action ->
            pendingAction = null
            // it is the pending action who handles this request
            requestHandled = false
            action.invoke(Result.success(device))
        }
        return requestHandled
    }

    fun cancelPending() {
        pendingAction?.let { action ->
            pendingAction = null
            action.invoke(Result.failure(CancellationException()))
        }
    }

    suspend fun <T : Any> useDevice(
        onComplete: ((YubiKeyDevice) -> Unit)? = null,
        waitForNfcKeyRemoval: Boolean = false,
        block: (YubiKeyDevice) -> T
    ): T {
        NfcState.waitForNfcKeyRemoval = waitForNfcKeyRemoval
        return deviceManager.withKey(
            onUsb = { useDeviceUsb(it, onComplete, block) },
            onNfc = { useDeviceNfc(onComplete, block) },
            onCancelled = { cancelPending() }
        )
    }

    private fun <T : Any> useDeviceUsb(
        device: UsbYubiKeyDevice,
        onComplete: ((YubiKeyDevice) -> Unit)?,
        block: (YubiKeyDevice) -> T
    ): T = block(device).also {
        onComplete?.invoke(device)
    }

    private suspend fun <T : Any> useDeviceNfc(
        onComplete: ((YubiKeyDevice) -> Unit)?,
        block: (YubiKeyDevice) -> T
    ): Result<T, Throwable> {
        try {
            val result =
                suspendCancellableCoroutine { outer ->
                    outer.invokeOnCancellation { pendingAction = null }
                    pendingAction = {
                        outer.resumeWith(
                            runCatching {
                                val device = it.value
                                block.invoke(device).also {
                                    onComplete?.invoke(device)
                                }
                            }
                        )
                    }
                }
            return Result.success(result)
        } catch (cancelled: CancellationException) {
            return Result.failure(cancelled)
        } catch (error: Throwable) {
            logger.error("Exception during action: ", error)
            return Result.failure(error)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OtpConnectionHelper::class.java)
    }
}
