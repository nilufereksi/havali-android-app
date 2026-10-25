package com.kampplus.havali.data.remote

import com.kampplus.havali.BuildConfig
import com.kampplus.havali.data.remote.api.AirQualityApi
import com.kampplus.havali.data.remote.api.ForecastApi
import com.kampplus.havali.data.remote.api.GeocodingApi
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit kurulumları. Uygulama boyunca her servis için tek istemci yeterlidir (`by lazy`).
 *
 * Debug derlemesinde istekler Logcat'e yazılır: Logcat'te `okhttp` diye filtreleyip
 * giden adresi ve gelen JSON'u görebilirsin. Release derlemesinde günlük kapalıdır.
 */
class ApiClient {

    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = when (BuildConfig.DEBUG) {
                true -> HttpLoggingInterceptor.Level.BASIC
                false -> HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    val forecastApi: ForecastApi by lazy { create(FORECAST_BASE_URL) }
    val geocodingApi: GeocodingApi by lazy { create(GEOCODING_BASE_URL) }
    val airQualityApi: AirQualityApi by lazy { create(AIR_QUALITY_BASE_URL) }

    private inline fun <reified T> create(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)

    private companion object {
        const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        const val AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"
        const val TIMEOUT_SECONDS = 15L
    }
}
