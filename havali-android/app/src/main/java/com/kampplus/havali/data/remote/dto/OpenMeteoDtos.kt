package com.kampplus.havali.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Open-Meteo yanıtlarının ham (ağ) modelleri: DTO = Data Transfer Object.
 *
 * Neden domain modelinden ayrı? Servis veriyi "sütun" biçiminde gönderir ve alan adları
 * alt çizgilidir. Ekran ise satır satır, anlamlı isimli modeller ister. Çeviri
 * ForecastMapper.kt dosyasındadır. Gson Kotlin varsayılan değerlerini bilmediği için
 * eksik gelebilecek her alan nullable tanımlanır.
 */

data class ForecastDto(
    val latitude: Double?,
    val longitude: Double?,
    val current: CurrentDto?,
    val hourly: HourlyDto?,
    val daily: DailyDto?
)

data class CurrentDto(
    val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_gusts_10m") val windGusts: Double?
)

data class HourlyDto(
    val time: List<String>?,
    @SerializedName("temperature_2m") val temperature: List<Double?>?,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double?>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>?,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double?>?,
    @SerializedName("wind_gusts_10m") val windGusts: List<Double?>?,
    @SerializedName("uv_index") val uvIndex: List<Double?>?,
    @SerializedName("relative_humidity_2m") val humidity: List<Int?>?,
    val visibility: List<Double?>?,
    /** Metre cinsinden gelir; domain santimetre kullanır. */
    @SerializedName("snow_depth") val snowDepth: List<Double?>?
)

data class DailyDto(
    val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?,
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double?>?,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double?>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>?,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double?>?
)

/** Arama sonucu yoksa servis `results` alanını hiç göndermez: null = boş. */
data class GeocodingDto(val results: List<GeocodingResultDto>?)

data class GeocodingResultDto(
    val id: Long?,
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("country_code") val countryCode: String?,
    val country: String?,
    val admin1: String?
)

data class AirQualityDto(val current: AirQualityCurrentDto?)

data class AirQualityCurrentDto(
    @SerializedName("european_aqi") val europeanAqi: Int?,
    @SerializedName("grass_pollen") val grassPollen: Double?,
    @SerializedName("olive_pollen") val olivePollen: Double?,
    @SerializedName("birch_pollen") val birchPollen: Double?
)
