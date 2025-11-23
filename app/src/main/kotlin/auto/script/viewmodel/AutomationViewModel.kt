package auto.script.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AutomationViewModel @Inject constructor() : ViewModel() {

    // A11yService 状态
    private val _a11yStatus = MutableLiveData<Boolean>()
    val a11yStatus: LiveData<Boolean> get() = _a11yStatus

    // Shizuku 状态
    private val _shizukuStatus = MutableLiveData<Boolean>()
    val shizukuStatus: LiveData<Boolean> get() = _shizukuStatus

    // Bind Service 状态
    private val _bindServiceStatus = MutableLiveData<Boolean>()
    val bindServiceStatus: LiveData<Boolean> get() = _bindServiceStatus

    // 更新方法
    fun updateA11yStatus(connected: Boolean) {
        Log.d("AutomationViewModel", "Updating A11y status: $connected")
        _a11yStatus.postValue(connected)
    }

    fun updateShizukuStatus(connected: Boolean) {
        _shizukuStatus.postValue(connected)
    }

    fun updateBindServiceStatus(connected: Boolean) {
        _bindServiceStatus.postValue(connected)
    }
}
