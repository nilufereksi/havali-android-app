package com.kampplus.havali.data.catalog

import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PlaceTag

/**
 * Haritada etiketiyle görünen yerler.
 *
 * [MapZoom] hem yakınlaşma seviyesini hem de etiketin hangi ölçekte görüneceğini belirler:
 * CITY/REGION uzaktan (Türkiye görünümü), DISTRICT bölgeye yaklaşınca, STREET şehir
 * içine inince görünür.
 */
object PlaceCatalog {

    private val COAST = setOf(PlaceTag.COASTAL)
    private val CITY_COAST = setOf(PlaceTag.COASTAL, PlaceTag.URBAN)
    private val MOUNTAIN = setOf(PlaceTag.MOUNTAIN)

    const val HOME_ID = "izmir"
    const val GEMLIK_ID = "gemlik"

    /** Haritanın üstündeki hızlı geçiş çipleri: önce iki odak (İzmir, Gemlik), sonra öteki büyük şehirler. */
    val QUICK_IDS = listOf(HOME_ID, "alsancak", GEMLIK_ID, "istanbul", "ankara", "trabzon", "antalya", "erzurum")

    val places: List<Place> = listOf(
        // Türkiye geneli
        place("istanbul", "İstanbul", "Marmara", 41.01, 28.96, MapZoom.REGION, CITY_COAST),
        place("ankara", "Ankara", "İç Anadolu", 39.93, 32.85, MapZoom.REGION, setOf(PlaceTag.URBAN)),
        Place(HOME_ID, "İzmir", "Ege", GeoPoint(38.419, 27.128), MapZoom.REGION, CITY_COAST, isHome = true),
        place("antalya", "Antalya", "Akdeniz", 36.89, 30.71, MapZoom.REGION, CITY_COAST),
        place("erzurum", "Erzurum", "Doğu Anadolu", 39.905, 41.27, MapZoom.REGION, setOf(PlaceTag.URBAN, PlaceTag.MOUNTAIN)),
        place("trabzon", "Trabzon", "Karadeniz", 41.0, 39.72, MapZoom.REGION, CITY_COAST),
        place("bursa", "Bursa", "Marmara", 40.19, 29.06, MapZoom.CITY, setOf(PlaceTag.URBAN, PlaceTag.MOUNTAIN)),
        place(GEMLIK_ID, "Gemlik", "Bursa", 40.432, 29.157, MapZoom.DISTRICT, CITY_COAST + PlaceTag.FEATURED),
        place("konya", "Konya", "İç Anadolu", 37.87, 32.49, MapZoom.CITY),
        place("adana", "Adana", "Akdeniz", 37.0, 35.32, MapZoom.CITY),
        place("diyarbakir", "Diyarbakır", "Güneydoğu Anadolu", 37.92, 40.23, MapZoom.CITY),
        place("van", "Van", "Doğu Anadolu", 38.5, 43.38, MapZoom.CITY, COAST),
        place("samsun", "Samsun", "Karadeniz", 41.29, 36.33, MapZoom.CITY, CITY_COAST),
        place("kars", "Kars", "Doğu Anadolu", 40.6, 43.1, MapZoom.CITY, MOUNTAIN),
        place("canakkale", "Çanakkale", "Marmara", 40.15, 26.41, MapZoom.CITY, CITY_COAST),
        // İzmir ve çevresi
        place("alsancak", "Alsancak", "İzmir", 38.437, 27.143, MapZoom.STREET, CITY_COAST),
        place("karsiyaka", "Karşıyaka", "İzmir", 38.459, 27.11, MapZoom.STREET, CITY_COAST),
        place("bayrakli", "Bayraklı", "İzmir", 38.462, 27.165, MapZoom.STREET, CITY_COAST),
        place("bornova", "Bornova", "İzmir", 38.466, 27.216, MapZoom.STREET, setOf(PlaceTag.URBAN)),
        place("buca", "Buca", "İzmir", 38.388, 27.175, MapZoom.STREET, setOf(PlaceTag.URBAN)),
        place("cigli", "Çiğli", "İzmir", 38.49, 27.04, MapZoom.STREET, CITY_COAST),
        place("urla", "Urla", "İzmir", 38.322, 26.765, MapZoom.DISTRICT, COAST),
        place("cesme", "Çeşme", "İzmir", 38.324, 26.302, MapZoom.DISTRICT, COAST),
        place("alacati", "Alaçatı", "İzmir", 38.281, 26.373, MapZoom.DISTRICT, COAST),
        place("karaburun", "Karaburun", "İzmir", 38.637, 26.513, MapZoom.DISTRICT, COAST),
        place("menemen", "Menemen", "İzmir", 38.608, 27.069, MapZoom.DISTRICT),
        place("foca", "Foça", "İzmir", 38.671, 26.756, MapZoom.DISTRICT, COAST),
        place("seferihisar", "Seferihisar", "İzmir", 38.197, 26.838, MapZoom.DISTRICT, COAST),
        place("torbali", "Torbalı", "İzmir", 38.155, 27.362, MapZoom.DISTRICT),
        place("kemalpasa", "Kemalpaşa", "İzmir", 38.43, 27.42, MapZoom.DISTRICT),
        place("bozdag", "Bozdağ", "İzmir", 38.35, 28.08, MapZoom.DISTRICT, MOUNTAIN),
        // Gemlik ve körfez çevresi
        place("kursunlu", "Kurşunlu", "Gemlik", 40.357, 29.043, MapZoom.DISTRICT, COAST),
        place("umurbey", "Umurbey", "Gemlik", 40.405, 29.205, MapZoom.DISTRICT),
        place("kucukkumla", "Küçükkumla", "Gemlik", 40.452, 29.092, MapZoom.DISTRICT, COAST),
        place("mudanya", "Mudanya", "Bursa", 40.375, 28.883, MapZoom.DISTRICT, COAST),
        place("orhangazi", "Orhangazi", "Bursa", 40.489, 29.309, MapZoom.DISTRICT),
        place("iznik", "İznik", "Bursa", 40.429, 29.721, MapZoom.DISTRICT, COAST),
        place("armutlu", "Armutlu", "Yalova", 40.519, 28.833, MapZoom.DISTRICT, COAST),
        place("uludag", "Uludağ", "Bursa", 40.103, 29.122, MapZoom.DISTRICT, MOUNTAIN),
        // Ankara çevresi
        place("cankaya", "Çankaya", "Ankara", 39.90, 32.86, MapZoom.DISTRICT, setOf(PlaceTag.URBAN)),
        place("kecioren", "Keçiören", "Ankara", 40.0, 32.86, MapZoom.DISTRICT, setOf(PlaceTag.URBAN)),
        place("golbasi", "Gölbaşı", "Ankara", 39.79, 32.81, MapZoom.DISTRICT),
        place("polatli", "Polatlı", "Ankara", 39.58, 32.15, MapZoom.DISTRICT),
        place("beypazari", "Beypazarı", "Ankara", 40.17, 31.92, MapZoom.DISTRICT),
        place("kizilcahamam", "Kızılcahamam", "Ankara", 40.47, 32.65, MapZoom.DISTRICT, MOUNTAIN),
        // Trabzon çevresi
        place("akcaabat", "Akçaabat", "Trabzon", 41.02, 39.57, MapZoom.DISTRICT, COAST),
        place("yomra", "Yomra", "Trabzon", 40.95, 39.85, MapZoom.DISTRICT, COAST),
        place("of", "Of", "Trabzon", 40.95, 40.27, MapZoom.DISTRICT, COAST),
        place("macka", "Maçka", "Trabzon", 40.81, 39.61, MapZoom.DISTRICT, MOUNTAIN),
        place("uzungol", "Uzungöl", "Trabzon", 40.62, 40.29, MapZoom.DISTRICT, MOUNTAIN),
        // İstanbul çevresi
        place("kadikoy", "Kadıköy", "İstanbul", 40.99, 29.03, MapZoom.DISTRICT, CITY_COAST),
        place("sariyer", "Sarıyer", "İstanbul", 41.17, 29.05, MapZoom.DISTRICT, CITY_COAST),
        place("avcilar", "Avcılar", "İstanbul", 40.98, 28.72, MapZoom.DISTRICT, CITY_COAST),
        place("silivri", "Silivri", "İstanbul", 41.07, 28.25, MapZoom.DISTRICT, COAST),
        place("sile", "Şile", "İstanbul", 41.18, 29.61, MapZoom.DISTRICT, COAST),
        place("kartal", "Kartal", "İstanbul", 40.89, 29.19, MapZoom.DISTRICT, CITY_COAST),
        // Antalya çevresi
        place("kemer", "Kemer", "Antalya", 36.6, 30.56, MapZoom.DISTRICT, COAST),
        place("belek", "Belek", "Antalya", 36.86, 31.06, MapZoom.DISTRICT, COAST),
        place("manavgat", "Manavgat", "Antalya", 36.79, 31.44, MapZoom.DISTRICT, COAST),
        place("olimpos", "Olimpos", "Antalya", 36.4, 30.47, MapZoom.DISTRICT, COAST),
        place("korkuteli", "Korkuteli", "Antalya", 37.07, 30.2, MapZoom.DISTRICT),
        // Erzurum çevresi
        place("palandoken", "Palandöken", "Erzurum", 39.84, 41.29, MapZoom.DISTRICT, MOUNTAIN),
        place("pasinler", "Pasinler", "Erzurum", 39.98, 41.67, MapZoom.DISTRICT),
        place("tortum", "Tortum", "Erzurum", 40.3, 41.55, MapZoom.DISTRICT),
        place("askale", "Aşkale", "Erzurum", 39.92, 40.69, MapZoom.DISTRICT)
    )

    fun home(): Place = places.first { it.isHome }

    private fun place(
        id: String,
        name: String,
        region: String,
        latitude: Double,
        longitude: Double,
        zoom: MapZoom,
        tags: Set<PlaceTag> = emptySet()
    ) = Place(id, name, region, GeoPoint(latitude, longitude), zoom, tags)
}
