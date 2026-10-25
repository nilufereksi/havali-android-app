package com.kampplus.havali.data.catalog

import com.kampplus.havali.domain.model.Campsite
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.TrafficCorridor
import com.kampplus.havali.domain.repository.GuideRepository

/**
 * Şehir rehberi: önerilen kamp alanları ve ana trafik aksları.
 * Kamp alanları bilinen kamp bölgeleridir; işletme adı değil bölge adı kullanılır.
 */
object GuideCatalog : GuideRepository {

    private val CAMPSITES = listOf(
        camp("karaburun-camp", "Karaburun koyları", "izmir", 38.66, 26.47),
        camp("akkum-camp", "Seferihisar · Akkum", "izmir", 38.16, 26.78),
        camp("bozdag-camp", "Bozdağ yaylası", "izmir", 38.33, 28.05),
        camp("kursunlu-camp", "Kurşunlu sahili", "gemlik", 40.355, 29.02),
        camp("uludag-camp", "Uludağ · Sarıalan", "gemlik", 40.11, 29.06),
        camp("agva-camp", "Şile · Ağva", "istanbul", 41.14, 29.85),
        camp("polonezkoy-camp", "Polonezköy", "istanbul", 41.11, 29.21),
        camp("soguksu-camp", "Soğuksu Milli Parkı", "ankara", 40.48, 32.61),
        camp("eymir-camp", "Eymir Gölü", "ankara", 39.82, 32.83),
        camp("uzungol-camp", "Uzungöl", "trabzon", 40.61, 40.30),
        camp("altindere-camp", "Altındere Vadisi", "trabzon", 40.70, 39.66),
        camp("olimpos-camp", "Olimpos · Çıralı", "antalya", 36.41, 30.47),
        camp("adrasan-camp", "Adrasan", "antalya", 36.30, 30.47),
        camp("tortum-camp", "Tortum Gölü", "erzurum", 40.66, 41.65)
    )

    /**
     * Akslar gerçek güzergâhı izleyen ara noktalarla tanımlıdır (kıyı yolu kıyıyı, çevre yolu
     * şehrin kuzey ve doğusunu dolaşır); çizimde noktalar yumuşak eğriyle birleştirilir.
     */
    private val CORRIDORS = listOf(
        road(
            "altinyol", "Altınyol", "izmir", 0.45,
            38.4555 to 27.098, 38.456 to 27.112, 38.459 to 27.127, 38.462 to 27.142, 38.46 to 27.156, 38.456 to 27.168, 38.452 to 27.179,
            alternativeId = "anadolu-cd"
        ),
        road(
            "anadolu-cd", "Anadolu Caddesi", "izmir", 0.2,
            38.461 to 27.1, 38.468 to 27.118, 38.472 to 27.138, 38.472 to 27.16, 38.469 to 27.18, 38.466 to 27.205,
            isAlternative = true
        ),
        road(
            "ankara-cd", "Ankara Caddesi", "izmir", 0.4, 38.452 to 27.179, 38.456 to 27.19, 38.46 to 27.2, 38.464 to 27.21,
            38.466 to 27.216
        ),
        road(
            "cevreyolu", "Çevre yolu", "izmir", 0.5,
            38.495 to 27.045, 38.49 to 27.08, 38.485 to 27.115, 38.48 to 27.15, 38.476 to 27.185, 38.465 to 27.21,
            38.445 to 27.215, 38.425 to 27.205, 38.405 to 27.19, 38.388 to 27.175,
            alternativeId = "yesildere"
        ),
        road(
            "kordon", "Kordon · Atatürk Cd.", "izmir", 0.3, 38.444 to 27.146, 38.438 to 27.141, 38.432 to 27.136, 38.426 to 27.132,
            38.419 to 27.128
        ),
        road(
            "sahil-bulvari", "Sahil Bulvarı", "izmir", 0.35,
            38.419 to 27.128, 38.413 to 27.11, 38.408 to 27.095, 38.402 to 27.078, 38.396 to 27.06, 38.392 to 27.045,
            alternativeId = "inonu-cd"
        ),
        road(
            "inonu-cd", "İnönü Caddesi", "izmir", 0.2,
            38.417 to 27.125, 38.411 to 27.105, 38.405 to 27.085, 38.398 to 27.065, 38.392 to 27.05,
            isAlternative = true
        ),
        road(
            "yesildere", "Yeşildere", "izmir", 0.25, 38.419 to 27.128, 38.413 to 27.138, 38.406 to 27.15, 38.399 to 27.16, 38.392 to 27.168,
            38.388 to 27.175
        ),
        road(
            "d575", "Bursa – Gemlik yolu", "gemlik", 0.5,
            40.205 to 29.065, 40.25 to 29.07, 40.29 to 29.085, 40.33 to 29.105, 40.37 to 29.125, 40.405 to 29.145, 40.43 to 29.155,
            alternativeId = "umurbey-yolu"
        ),
        road(
            "umurbey-yolu", "Umurbey üzerinden", "gemlik", 0.2,
            40.205 to 29.065, 40.26 to 29.12, 40.32 to 29.16, 40.37 to 29.19, 40.405 to 29.205, 40.43 to 29.165,
            isAlternative = true
        ),
        road(
            "gemlik-mudanya", "Gemlik – Mudanya", "gemlik", 0.3,
            40.43 to 29.155, 40.415 to 29.115, 40.395 to 29.08, 40.37 to 29.04, 40.36 to 29.0, 40.37 to 28.95, 40.375 to 28.89
        ),
        road(
            "gemlik-orhangazi", "Gemlik – Orhangazi", "gemlik", 0.35, 40.432 to 29.16, 40.445 to 29.2, 40.46 to 29.24, 40.475 to 29.28,
            40.489 to 29.309
        ),
        road(
            "gemlik-sahil", "Gemlik sahil yolu", "gemlik", 0.25, 40.44 to 29.12, 40.438 to 29.14, 40.432 to 29.157, 40.426 to 29.175,
            40.42 to 29.19
        )
    )

    override fun campsites(): List<Campsite> = CAMPSITES

    override fun corridors(cityId: String): List<TrafficCorridor> = CORRIDORS.filter { it.cityId == cityId }

    private fun camp(id: String, name: String, cityId: String, latitude: Double, longitude: Double) =
        Campsite(id, name, cityId, GeoPoint(latitude, longitude))

    private fun road(
        id: String,
        name: String,
        cityId: String,
        baseLoad: Double,
        vararg points: Pair<Double, Double>,
        alternativeId: String? = null,
        isAlternative: Boolean = false
    ) = TrafficCorridor(id, name, cityId, points.map { (lat, lon) -> GeoPoint(lat, lon) }, baseLoad, alternativeId, isAlternative)
}
