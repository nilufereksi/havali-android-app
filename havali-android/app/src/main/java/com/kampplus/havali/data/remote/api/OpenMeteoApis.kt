package com.kampplus.havali.data.remote.api

import com.kampplus.havali.data.remote.dto.AirQualityDto
import com.kampplus.havali.data.remote.dto.ForecastDto
import com.kampplus.havali.data.remote.dto.GeocodingDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo servislerinin "menüleri". Retrofit gövdeleri kendisi yazar; biz yalnızca
 * adresi ve parametreleri tarif ederiz. Üç servis üç ayrı sunucudadır.
 * Open-Meteo anahtar istemez ve ticari olmayan kullanımda ücretsizdir.
 */
interface ForecastApi {

    /** Tek nokta için anlık + saatlik + günlük tahmin. */
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
        @Query("timezone") timezone: String = TIMEZONE
    ): ForecastDto

    /**
     * Birden çok nokta için tek istek: enlem ve boylamlar virgülle ayrılır,
     * servis bu durumda bir JSON DİZİSİ döner. Harita etiketleri bunu kullanır.
     */
    @GET("v1/forecast")
    suspend fun getForecasts(
        @Query("latitude") latitudes: String,
        @Query("longitude") longitudes: String,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = MAP_FORECAST_DAYS,
        @Query("timezone") timezone: String = TIMEZONE
    ): List<ForecastDto>

    companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,weather_code,relative_humidity_2m,wind_speed_10m,wind_gusts_10m"
        const val HOURLY_FIELDS =
            "temperature_2m,apparent_temperature,weather_code,precipitation_probability,wind_speed_10m," +
                "wind_gusts_10m,uv_index,relative_humidity_2m,visibility,snow_depth"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,uv_index_max"

        /** Valiz için birkaç gün sonrası da gerekir; 14 gün yeterli. */
        const val FORECAST_DAYS = 14
        const val MAP_FORECAST_DAYS = 2
        const val TIMEZONE = "auto"
    }
}

/** Şehir arama: `geocoding-api.open-meteo.com/v1/search?name=İzmir&language=tr` */
interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "tr"
    ): GeocodingDto
}

/** Hava kalitesi ve polen: `air-quality-api.open-meteo.com/v1/air-quality` */
interface AirQualityApi {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "european_aqi,grass_pollen,olive_pollen,birch_pollen",
        @Query("timezone") timezone: String = ForecastApi.TIMEZONE
    ): AirQualityDto
}
