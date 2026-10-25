package com.kampplus.havali.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*
 * Cihazda kalıcı küçük ayarlar (SharedPreferences).
 *
 * Değer önce StateFlow'a yazılır, böylece ekran anında güncellenir; sonra diske
 * `apply()` ile arka planda kaydedilir. Uygulama kapanıp açılınca diskteki değer okunur.
 */

class PreferencesSettingsRepository(private val prefs: SharedPreferences) : SettingsRepository {

    private val state = MutableStateFlow(readDataSource())
    override val dataSource: StateFlow<DataSource> = state.asStateFlow()

    override fun setDataSource(source: DataSource) {
        state.value = source
        prefs.edit { putString(KEY_DATA_SOURCE, source.name) }
    }

    /** Demo varsayılandır: inceleme sırasında internet olmasa da uygulama hikâyesini anlatır. */
    private fun readDataSource(): DataSource {
        val stored = prefs.getString(KEY_DATA_SOURCE, null)
        return DataSource.entries.firstOrNull { it.name == stored } ?: DataSource.DEMO
    }

    private companion object {
        const val KEY_DATA_SOURCE = "data_source"
    }
}

class PreferencesComfortRepository(private val prefs: SharedPreferences) : ComfortRepository {

    private val state = MutableStateFlow(read())
    override val profile: StateFlow<ComfortProfile> = state.asStateFlow()

    override fun save(profile: ComfortProfile) {
        state.value = profile
        prefs.edit {
            putFloat(KEY_OFFSET, profile.offsetC.toFloat())
            putInt(KEY_SAMPLES, profile.samples)
        }
    }

    /**
     * İlk açılışta demo profili: kullanıcı geçmiş 18 geri bildirimde ortalamadan
     * 3 °C daha çabuk üşümüş ("Sana göre hissedilen" kartı bunu gösterir).
     */
    private fun read() = ComfortProfile(
        offsetC = prefs.getFloat(KEY_OFFSET, DEMO_OFFSET_C).toDouble(),
        samples = prefs.getInt(KEY_SAMPLES, DEMO_SAMPLES)
    )

    private companion object {
        const val KEY_OFFSET = "comfort_offset"
        const val KEY_SAMPLES = "comfort_samples"
        const val DEMO_OFFSET_C = -3.0f
        const val DEMO_SAMPLES = 18
    }
}
