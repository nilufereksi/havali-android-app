package com.kampplus.havali.data.repository

import com.kampplus.havali.data.catalog.PlaceCatalog
import com.kampplus.havali.data.remote.api.AirQualityApi
import com.kampplus.havali.data.remote.api.ForecastApi
import com.kampplus.havali.data.remote.api.GeocodingApi
import com.kampplus.havali.data.remote.mapper.toDomain
import com.kampplus.havali.data.remote.mapper.toPlace
import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.repository.AirQualityRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Canlı hava verisi: Open-Meteo.
 *
 * `suspend` tek başına thread değiştirmez; ağ işini ana thread'den (UI) almak için
 * `withContext(ioDispatcher)` ile IO havuzuna geçilir.
 */
class OpenMeteoWeatherRepository(
    private val api: ForecastApi,
    private val ioDispatcher: CoroutineDispatcher
) : WeatherRepository {

    override suspend fun getForecast(point: GeoPoint): Forecast = withContext(ioDispatcher) {
        api.getForecast(point.latitude, point.longitude).toDomain(point)
    }

    override suspend fun getForecasts(points: List<GeoPoint>): List<Forecast> = withContext(ioDispatcher) {
        val response = api.getForecasts(
            latitudes = points.joinToString(",") { it.latitude.toString() },
            longitudes = points.joinToString(",") { it.longitude.toString() }
        )
        response.zip(points) { dto, point -> dto.toDomain(point) }
    }
}

class OpenMeteoAirQualityRepository(
    private val api: AirQualityApi,
    private val ioDispatcher: CoroutineDispatcher
) : AirQualityRepository {

    override suspend fun getAirQuality(point: GeoPoint): AirQuality = withContext(ioDispatcher) {
        api.getAirQuality(point.latitude, point.longitude).toDomain()
    }
}

/** Bilinen yerler sabit katalogdan, çevrimiçi arama Open-Meteo geocoding servisinden gelir. */
class CatalogPlaceRepository(
    private val api: GeocodingApi,
    private val ioDispatcher: CoroutineDispatcher
) : PlaceRepository {

    override fun knownPlaces(): List<Place> = PlaceCatalog.places

    override fun home(): Place = PlaceCatalog.home()

    override suspend fun searchOnline(query: String): List<Place> = withContext(ioDispatcher) {
        api.search(query).results.orEmpty().mapNotNull { it.toPlace() }
    }
}
