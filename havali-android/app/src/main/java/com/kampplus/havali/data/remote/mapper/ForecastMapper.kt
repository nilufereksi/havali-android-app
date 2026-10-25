package com.kampplus.havali.data.remote.mapper

import com.kampplus.havali.data.remote.dto.AirQualityDto
import com.kampplus.havali.data.remote.dto.CurrentDto
import com.kampplus.havali.data.remote.dto.DailyDto
import com.kampplus.havali.data.remote.dto.ForecastDto
import com.kampplus.havali.data.remote.dto.GeocodingResultDto
import com.kampplus.havali.data.remote.dto.HourlyDto
import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.AirQualityLevel
import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PollenLevel
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.model.WeatherSnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/*
 * Ağ modeli (DTO) -> domain modeli çevirileri.
 *
 * Servis veriyi SÜTUN biçiminde gönderir: `hourly.time[i]` ile `hourly.temperature_2m[i]`
 * aynı saate aittir. Aşağıdaki fonksiyonlar sütunları satırlara çevirir. Zorunlu alanı
 * eksik bir satır atlanır; tek bozuk kayıt yüzünden bütün ekran çökmez.
 */

private const val CM_PER_METER = 100.0
private const val UNKNOWN_VISIBILITY_METERS = 20_000.0

/** `current` bloğu yoksa yanıt bozuktur; ViewModel bunu hata durumuna çevirir. */
fun ForecastDto.toDomain(requested: GeoPoint): Forecast {
    val currentDto = current ?: throw IllegalStateException("Yanıtta current bloğu yok")
    val hours = hourly.toSnapshots()
    return Forecast(
        point = GeoPoint(latitude ?: requested.latitude, longitude ?: requested.longitude),
        current = currentDto.toSnapshot(hours),
        hourly = hours,
        daily = daily.toDailyWeather()
    )
}

/** Anlık blokta olmayan alanlar (yağış olasılığı, UV, görüş, kar) aynı saatin satırından alınır. */
private fun CurrentDto.toSnapshot(hours: List<WeatherSnapshot>): WeatherSnapshot {
    val now = LocalDateTime.parse(time ?: throw IllegalStateException("Anlık zaman yok"))
    val sameHour = hours.firstOrNull { it.time == now.truncatedTo(ChronoUnit.HOURS) }
    val currentTemperature = temperature ?: sameHour?.temperatureC ?: 0.0
    return WeatherSnapshot(
        time = now,
        temperatureC = currentTemperature,
        apparentTemperatureC = apparentTemperature ?: currentTemperature,
        kind = WeatherKind.fromWmoCode(weatherCode ?: 0),
        precipitationProbability = sameHour?.precipitationProbability ?: 0,
        windKmh = windSpeed ?: 0.0,
        gustKmh = windGusts ?: windSpeed ?: 0.0,
        uvIndex = sameHour?.uvIndex ?: 0.0,
        humidityPercent = humidity ?: 0,
        visibilityMeters = sameHour?.visibilityMeters ?: UNKNOWN_VISIBILITY_METERS,
        snowDepthCm = sameHour?.snowDepthCm ?: 0.0
    )
}

private fun HourlyDto?.toSnapshots(): List<WeatherSnapshot> {
    val times = this?.time ?: return emptyList()
    return times.indices.mapNotNull { i ->
        val temperatureC = temperature?.getOrNull(i) ?: return@mapNotNull null
        val code = weatherCode?.getOrNull(i) ?: return@mapNotNull null
        WeatherSnapshot(
            time = LocalDateTime.parse(times[i]),
            temperatureC = temperatureC,
            apparentTemperatureC = apparentTemperature?.getOrNull(i) ?: temperatureC,
            kind = WeatherKind.fromWmoCode(code),
            precipitationProbability = precipitationProbability?.getOrNull(i) ?: 0,
            windKmh = windSpeed?.getOrNull(i) ?: 0.0,
            gustKmh = windGusts?.getOrNull(i) ?: 0.0,
            uvIndex = uvIndex?.getOrNull(i) ?: 0.0,
            humidityPercent = humidity?.getOrNull(i) ?: 0,
            visibilityMeters = visibility?.getOrNull(i) ?: UNKNOWN_VISIBILITY_METERS,
            snowDepthCm = (snowDepth?.getOrNull(i) ?: 0.0) * CM_PER_METER
        )
    }
}

private fun DailyDto?.toDailyWeather(): List<DailyWeather> {
    val dates = this?.time ?: return emptyList()
    return dates.indices.mapNotNull { i ->
        val max = temperatureMax?.getOrNull(i) ?: return@mapNotNull null
        val min = temperatureMin?.getOrNull(i) ?: return@mapNotNull null
        DailyWeather(
            date = LocalDate.parse(dates[i]),
            minTemperatureC = min,
            maxTemperatureC = max,
            kind = WeatherKind.fromWmoCode(weatherCode?.getOrNull(i) ?: 0),
            precipitationProbability = precipitationProbabilityMax?.getOrNull(i) ?: 0,
            uvIndexMax = uvIndexMax?.getOrNull(i) ?: 0.0
        )
    }
}

/** Arama sonucunu haritada gösterilebilir bir yere çevirir. Koordinatı eksik kayıt atlanır. */
fun GeocodingResultDto.toPlace(): Place? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return Place(
        id = "geo-${id ?: name.hashCode()}",
        name = name.orEmpty(),
        region = listOfNotNull(admin1, country).distinct().joinToString(", "),
        point = GeoPoint(lat, lon),
        zoom = MapZoom.CITY
    )
}

fun AirQualityDto.toDomain(): AirQuality {
    val aqi = current?.europeanAqi
    val pollen = listOfNotNull(current?.grassPollen, current?.olivePollen, current?.birchPollen).maxOrNull()
    return AirQuality(
        level = AirQualityLevel.fromEuropeanAqi(aqi),
        europeanAqi = aqi,
        pollen = PollenLevel.fromGrainsPerCubicMeter(pollen)
    )
}
