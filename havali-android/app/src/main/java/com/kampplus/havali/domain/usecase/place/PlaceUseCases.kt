package com.kampplus.havali.domain.usecase.place

import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import java.util.Locale

/**
 * Şehir arama.
 *
 * Önce uygulamanın tanıdığı yerler anında süzülür (yazdıkça liste değişir); kullanıcı
 * aramayı onaylarsa Open-Meteo'ya sorulur. Türkçe büyük/küçük harf kuralları (İ/i, I/ı)
 * için tr dili kullanılır.
 */
class SearchPlacesUseCase(private val places: PlaceRepository) {

    fun filterKnown(query: String): List<Place> {
        val needle = query.trim().lowercase(TURKISH)
        return places.knownPlaces().filter { needle.isEmpty() || it.name.lowercase(TURKISH).contains(needle) }
    }

    suspend fun searchOnline(query: String): List<Place> = places.searchOnline(query.trim())

    private companion object {
        val TURKISH: Locale = Locale.forLanguageTag("tr")
    }
}

/**
 * Haritadaki bütün yerlerin havasını tek istekte getirir.
 * Sonuç yer kimliğine göre eşlenir; harita her yerin üstüne kendi animasyonunu çizer.
 */
class GetMapWeatherUseCase(
    private val places: PlaceRepository,
    private val weather: WeatherRepository
) {

    suspend operator fun invoke(): Map<String, Forecast> {
        val known = places.knownPlaces()
        val forecasts = weather.getForecasts(known.map { it.point })
        return known.map { it.id }.zip(forecasts).toMap()
    }
}
