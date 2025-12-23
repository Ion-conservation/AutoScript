package auto.script.viewmodel

import androidx.lifecycle.ViewModel
import auto.script.ui.theme.AppThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    // 使用 StateFlow 存储当前主题
    private val _currentTheme = MutableStateFlow(AppThemeStyle.MINT_SODA)
    val currentTheme: StateFlow<AppThemeStyle> = _currentTheme

    fun switchTheme(newStyle: AppThemeStyle) {
        _currentTheme.value = newStyle
    }
}