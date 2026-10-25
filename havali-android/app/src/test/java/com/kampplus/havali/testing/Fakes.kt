package com.kampplus.havali.testing

import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.AirQualityLevel
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PollenLevel
import com.kampplus.havali.domain.repository.AirQualityRepository
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.MapRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.SettingsRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
 * Testler için sahte (fake) repository'ler. Gerçek ağ ya da cihaz olmadan
 * ViewModel'in davranışı denenir; [failWith] ile "uçak modu" taklit edilir.
 */

class FakeWeatherRepository(private val forecast: Forecast, var failWith: Exception? = null) : WeatherRepository {
    var calls = 0
        private set

    override suspend fun getForecast(point: GeoPoint): Forecast {
        calls++
        failWith?.let { throw it }
        return forecast
    }

    override suspend fun getForecasts(points: List<GeoPoint>): List<Forecast> = points.map { getForecast(it) }
}

class FakeAirQualityRepository : AirQualityRepository {
    override suspend fun getAirQuality(point: GeoPoint) = AirQuality(AirQualityLevel.GOOD, 15, PollenLevel.LOW)
}

class FakePlaceRepository(private val home: Place) : PlaceRepository {
    override fun knownPlaces(): List<Place> = listOf(home)
    override fun home(): Place = home
    override suspend fun searchOnline(query: String): List<Place> = emptyList()
}

class FakeComfortRepository(initial: ComfortProfile = ComfortProfile()) : ComfortRepository {
    private val state = MutableStateFlow(initial)
    override val profile: StateFlow<ComfortProfile> = state

    override fun save(profile: ComfortProfile) {
        state.value = profile
    }
}

class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(DataSource.DEMO)
    override val dataSource: StateFlow<DataSource> = state

    override fun setDataSource(source: DataSource) {
        state.value = source
    }
}

class FakeMapRepository : MapRepository {
    override suspend fun loadGeometry() = MapGeometry(emptyMap())
}
