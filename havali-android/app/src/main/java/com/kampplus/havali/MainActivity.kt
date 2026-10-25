package com.kampplus.havali

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kampplus.havali.presentation.navigation.HavaliApp
import com.kampplus.havali.presentation.theme.HavaliTheme

/**
 * Tek Activity: bütün ekranlar Compose ile bu Activity'nin içinde çizilir.
 *
 * Yaşam döngüsü metotları Logcat'e yazar. Logcat'te "Lifecycle" diye filtreleyip
 * uygulamayı arka plana al / döndür / kapat: onPause, onStop, onDestroy sırasını görürsün.
 * Ekran döndürülünce Activity yeniden oluşturulur ama ViewModel'ler ve
 * rememberSaveable değerleri korunur.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate (yeniden oluşturma: ${savedInstanceState != null})")
        enableEdgeToEdge()
        setContent {
            // Koyu tema varsayılandır; sağ üstteki yuvarlak düğme açık temaya geçirir. Ekran dönse de seçim korunur.
            var darkTheme by rememberSaveable { mutableStateOf(true) }
            HavaliTheme(darkTheme = darkTheme) {
                HavaliApp(
                    windowSizeClass = calculateWindowSizeClass(this),
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy (yapılandırma değişikliği: $isChangingConfigurations)")
        super.onDestroy()
    }

    private companion object {
        const val TAG = "Lifecycle"
    }
}
