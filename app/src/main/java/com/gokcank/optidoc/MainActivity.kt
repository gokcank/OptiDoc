package com.gokcank.optidoc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gokcank.optidoc.ui.navigation.OptiDocNavHost
import com.gokcank.optidoc.ui.theme.BelgeTarayiciTheme
import com.gokcank.optidoc.data.settings.SettingsRepository
import com.gokcank.optidoc.data.settings.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Uygulamanın tek Activity'si.
 * @AndroidEntryPoint, Hilt'in bu Activity'ye bağımlılık enjekte edebilmesini sağlar.
 * Tüm ekran yönetimi Navigation Compose (NavHost) üzerinden yapılır.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.gokcank.optidoc.ui.components.InterstitialAdManager.loadAd(this)
        setContent {
            val themeMode by settingsRepository.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            BelgeTarayiciTheme(darkTheme = darkTheme) {
                OptiDocNavHost()
            }
        }
    }
}
