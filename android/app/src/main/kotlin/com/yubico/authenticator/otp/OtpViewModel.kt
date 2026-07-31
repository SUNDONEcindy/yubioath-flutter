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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yubico.authenticator.ViewModelData
import com.yubico.authenticator.otp.data.OtpState

class OtpViewModel : ViewModel() {
    private val _state = MutableLiveData<ViewModelData>()
    val state: LiveData<ViewModelData> = _state

    private val _currentSerial = MutableLiveData<Int?>()
    val currentSerial: LiveData<Int?> = _currentSerial

    fun setSerial(serial: Int?) {
        _currentSerial.postValue(serial)
    }

    fun setState(state: OtpState) {
        _state.postValue(ViewModelData.Value(state))
    }

    fun clearState() {
        _state.postValue(ViewModelData.Empty)
    }
}
