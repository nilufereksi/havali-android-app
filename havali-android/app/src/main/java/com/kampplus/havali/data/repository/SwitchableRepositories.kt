package com.kampplus.havali.data.repository

import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.AirQualityLevel
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.PollenLevel
import com.kampplus.havali.domain.repository.AirQualityRepository
import com.kampplus.havali.domain.repository.SettingsRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import kotlinx.coroutines.delay

/**
 * Haritadaki "DEMO / CANLI" anahtarının arkasındaki strateji seçici.
 *
 * Ekranlar tek bir [WeatherRepository] görür; hangi kaynağın kullanılacağına her çağrıda
 * ayarlara bakılarak karar verilir. Yeni bir kaynak (ör. önbellek) eklemek ekranlara
 * dokunmadan buradaki `when`'e bir dal eklemekle olur.
 */
class SwitchableWeatherRepository(
    private val settings: SettingsRepository,
    private val live: WeatherRepository,
    private val demo: WeatherRepository
) : WeatherRepository {

    private val active: WeatherRepository
        get() = when (settings.dataSource.value) {
            DataSource.LIVE -> live
            DataSource.DEMO -> demo
        }

    override suspend fun getForecast(point: GeoPoint): Forecast = active.getForecast(point)

    override suspend fun getForecasts(points: List<GeoPoint>): List<Forecast> = active.getForecasts(points)
}

class SwitchableAirQualityRepository(
    private val settings: SettingsRepository,
    private val live: AirQualityRepository,
    private val demo: AirQualityRepository
) : AirQualityRepository {

    override suspend fun getAirQuality(point: GeoPoint): AirQuality = when (settings.dataSource.value) {
        DataSource.LIVE -> live.getAirQuality(point)
        DataSource.DEMO -> demo.getAirQuality(point)
    }
}

/** Demo senaryosunda hava kalitesi: iyi-orta, zeytin poleni orta (Ege sonbaharı). */
class DemoAirQualityRepository(private val latencyMillis: Long = 300L) : AirQualityRepository {

    override suspend fun getAirQuality(point: GeoPoint): AirQuality {
        delay(latencyMillis)
        return AirQuality(level = AirQualityLevel.FAIR, europeanAqi = DEMO_AQI, pollen = PollenLevel.MODERATE)
    }

    private companion object {
        const val DEMO_AQI = 28
    }
}
