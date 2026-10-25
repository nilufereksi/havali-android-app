package com.kampplus.havali.domain.model

/** Çanta ve valiz listelerinde geçebilecek her şey. Ekran adını ve ipucunu kaynak dosyasından okur. */
enum class ItemKey {
    UMBRELLA,
    SUNSCREEN,
    LIP_BALM,
    SUNGLASSES,
    WATERPROOF_SHOES,
    SPARE_SOCKS,
    PROTECTIVE_GEAR,
    VEHICLE_LOCK,
    TIRE_AND_BRAKE_CHECK,
    VISOR_ANTI_FOG,
    HELMET_AND_VEST,
    BIKE_LIGHTS,
    TIRE_PRESSURE,
    WIPER_FLUID,
    SPARE_UMBRELLA_CAR,
    TRANSIT_CARD,
    FERRY_LAYER,
    COAT,
    SWEATER,
    EXTRA_SWEATER,
    LIGHT_LAYER,
    LONG_TROUSERS,
    SHORTS,
    T_SHIRTS,
    SWIMWEAR,
    JACKET_ON_BOARD,
    CABIN_CARDIGAN,
    NECK_PILLOW,
    KEEP_JACKET_CLOSE,
    LEAVE_EARLY,
    BAG_COVER,
    DRESS_IN_LAYERS
}

/** @param value İpucu cümlesinde kullanılan sayı (adet, %, °C...). Gerekmiyorsa null. */
data class ChecklistItem(val key: ItemKey, val value: Int? = null)

enum class ChecklistGroup { MODE, WEATHER, CLOTHING, RAIN_AND_CARE, JOURNEY }

data class ChecklistSection(val group: ChecklistGroup, val items: List<ChecklistItem>)

/** Gidilen yerin havasına göre valizin genel karakteri. */
enum class TripWarmth {
    SUMMER,
    SEASONAL,
    WINTER;

    companion object {
        private const val SUMMER_FROM_C = 22.0
        private const val SEASONAL_FROM_C = 14.0

        fun fromAverageTemperature(averageC: Double): TripWarmth = when {
            averageC >= SUMMER_FROM_C -> SUMMER
            averageC >= SEASONAL_FROM_C -> SEASONAL
            else -> WINTER
        }
    }
}

/**
 * Valiz listesi ve onu destekleyen özet bilgiler.
 *
 * @param temperatureGapC Çıkış noktası ile varış arasındaki ortalama sıcaklık farkı (pozitif = varış daha soğuk).
 */
data class PackingList(
    val warmth: TripWarmth,
    val temperatureGapC: Int,
    val rainyDays: Int,
    val coldestNightC: Int,
    val sections: List<ChecklistSection>
)

enum class OutfitPiece {
    T_SHIRT,
    LIGHT_SWEATER,
    THICK_SWEATER,
    LIGHT_JACKET,
    WATERPROOF_JACKET,
    COAT,
    SANDALS,
    CLOSED_SHOES,
    WATERPROOF_BOOTS
}

/** "Ne giysem?" kartının üç parçası. Dış katman sıcak ve kuru havada gerekmez. */
data class Outfit(val base: OutfitPiece, val outer: OutfitPiece?, val shoes: OutfitPiece)
