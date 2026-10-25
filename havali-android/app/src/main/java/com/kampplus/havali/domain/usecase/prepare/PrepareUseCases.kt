package com.kampplus.havali.domain.usecase.prepare

import com.kampplus.havali.domain.model.ChecklistGroup
import com.kampplus.havali.domain.model.ChecklistItem
import com.kampplus.havali.domain.model.ChecklistSection
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.ComfortSensitivity
import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.Outfit
import com.kampplus.havali.domain.model.OutfitPiece
import com.kampplus.havali.domain.model.PackingList
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.TripWarmth
import com.kampplus.havali.domain.model.WeatherSnapshot
import kotlin.math.roundToInt

/**
 * "Kapıdan çıkmadan" listesi: ulaşım şekline özgü maddeler + günün havasına göre maddeler.
 */
class BuildDailyChecklistUseCase {

    operator fun invoke(mode: TravelMode, hours: List<WeatherSnapshot>, profile: ComfortProfile): List<ChecklistSection> {
        val day = DaySummary.of(hours, profile)
        val modeItems = ModeEssentials.daily(mode).map { ChecklistItem(it, day.valueFor(it)) }
        val weatherItems = listOfNotNull(
            ItemKey.UMBRELLA.takeIf { day.maxRain >= UMBRELLA_RAIN },
            ItemKey.SUNSCREEN.takeIf { day.maxUv >= SUNSCREEN_UV },
            ItemKey.LIP_BALM.takeIf { day.maxGust >= LIP_BALM_GUST || day.minPersonalC < LIP_BALM_COLD }
        ).map { ChecklistItem(it, day.valueFor(it)) }
        return listOf(
            ChecklistSection(ChecklistGroup.MODE, modeItems),
            ChecklistSection(ChecklistGroup.WEATHER, weatherItems)
        ).filter { it.items.isNotEmpty() }
    }

    private companion object {
        const val UMBRELLA_RAIN = 40
        const val SUNSCREEN_UV = 3.0
        const val LIP_BALM_GUST = 25.0
        const val LIP_BALM_COLD = 12.0
    }
}

/** Günün en kötü değerleri; liste maddelerinin ipucu sayıları buradan gelir. */
private data class DaySummary(val maxRain: Int, val maxUv: Double, val maxGust: Double, val minPersonalC: Double) {

    fun valueFor(key: ItemKey): Int? = when (key) {
        ItemKey.UMBRELLA, ItemKey.TIRE_AND_BRAKE_CHECK, ItemKey.WIPER_FLUID -> maxRain
        ItemKey.SUNSCREEN -> maxUv.roundToInt()
        ItemKey.PROTECTIVE_GEAR, ItemKey.LIP_BALM -> minPersonalC.roundToInt()
        ItemKey.FERRY_LAYER -> maxGust.roundToInt()
        else -> null
    }

    companion object {
        fun of(hours: List<WeatherSnapshot>, profile: ComfortProfile) = DaySummary(
            maxRain = hours.maxOfOrNull { it.precipitationProbability } ?: 0,
            maxUv = hours.maxOfOrNull { it.uvIndex } ?: 0.0,
            maxGust = hours.maxOfOrNull { it.gustKmh } ?: 0.0,
            minPersonalC = hours.minOfOrNull { profile.feelsLikeFor(it.apparentTemperatureC) } ?: 0.0
        )
    }
}

/** "Ne giysem?" kartı: kişisel hissedilen sıcaklığa ve ıslaklığa göre üç parça. */
class SuggestOutfitUseCase {

    operator fun invoke(minPersonalC: Double, wet: Boolean): Outfit = Outfit(
        base = when {
            minPersonalC < THICK_BELOW_C -> OutfitPiece.THICK_SWEATER
            minPersonalC < LIGHT_BELOW_C -> OutfitPiece.LIGHT_SWEATER
            else -> OutfitPiece.T_SHIRT
        },
        outer = when {
            wet -> OutfitPiece.WATERPROOF_JACKET
            minPersonalC < COAT_BELOW_C -> OutfitPiece.COAT
            minPersonalC < JACKET_BELOW_C -> OutfitPiece.LIGHT_JACKET
            else -> null
        },
        shoes = when {
            wet -> OutfitPiece.WATERPROOF_BOOTS
            minPersonalC >= SANDALS_FROM_C -> OutfitPiece.SANDALS
            else -> OutfitPiece.CLOSED_SHOES
        }
    )

    private companion object {
        const val THICK_BELOW_C = 10.0
        const val LIGHT_BELOW_C = 18.0
        const val COAT_BELOW_C = 6.0
        const val JACKET_BELOW_C = 16.0
        const val SANDALS_FROM_C = 24.0
    }
}

/**
 * Valiz listesi: gidilen yerin günlük tahminine, kişisel konfor profiline ve yolculuk
 * şekline göre hazırlanır. Çıkış şehri isteğe bağlıdır; verilirse sıcaklık farkı hesaplanır.
 */
class BuildPackingListUseCase {

    operator fun invoke(
        originDays: List<DailyWeather>,
        destinationDays: List<DailyWeather>,
        mode: TravelMode,
        profile: ComfortProfile
    ): PackingList {
        val destinationAverage = destinationDays.map { it.averageTemperatureC }.average()
        val originAverage = originDays.map { it.averageTemperatureC }.ifEmpty { listOf(destinationAverage) }.average()
        val warmth = TripWarmth.fromAverageTemperature(destinationAverage + profile.offsetC)
        val nights = (destinationDays.size - 1).coerceAtLeast(1)
        val rainyDays = destinationDays.count { it.precipitationProbability >= RAINY_PERCENT }
        val maxUv = destinationDays.maxOfOrNull { it.uvIndexMax } ?: 0.0
        return PackingList(
            warmth = warmth,
            temperatureGapC = (originAverage - destinationAverage).roundToInt(),
            rainyDays = rainyDays,
            coldestNightC = destinationDays.minOfOrNull { it.minTemperatureC }?.roundToInt() ?: 0,
            sections = listOf(
                ChecklistSection(ChecklistGroup.CLOTHING, clothing(warmth, nights, profile, rainyDays)),
                ChecklistSection(ChecklistGroup.RAIN_AND_CARE, care(warmth, rainyDays, maxUv)),
                ChecklistSection(ChecklistGroup.JOURNEY, ModeEssentials.trip(mode).map { ChecklistItem(it) })
            ).filter { it.items.isNotEmpty() }
        )
    }

    private fun clothing(warmth: TripWarmth, nights: Int, profile: ComfortProfile, rainyDays: Int): List<ChecklistItem> {
        val base = when (warmth) {
            TripWarmth.WINTER -> listOf(
                ChecklistItem(ItemKey.COAT),
                ChecklistItem(ItemKey.SWEATER, 2),
                ChecklistItem(ItemKey.LONG_TROUSERS, nights)
            )
            TripWarmth.SEASONAL -> listOf(
                ChecklistItem(ItemKey.LIGHT_LAYER),
                ChecklistItem(ItemKey.SWEATER, 1),
                ChecklistItem(ItemKey.LONG_TROUSERS, nights)
            )
            TripWarmth.SUMMER -> listOf(
                ChecklistItem(ItemKey.T_SHIRTS, nights + 1),
                ChecklistItem(ItemKey.SHORTS, nights),
                ChecklistItem(ItemKey.SWIMWEAR)
            )
        }
        return base + listOfNotNull(
            ChecklistItem(ItemKey.EXTRA_SWEATER).takeIf { profile.sensitivity == ComfortSensitivity.FEELS_COLD },
            ChecklistItem(ItemKey.WATERPROOF_SHOES, rainyDays).takeIf { rainyDays > 0 }
        )
    }

    private fun care(warmth: TripWarmth, rainyDays: Int, maxUv: Double): List<ChecklistItem> = listOfNotNull(
        ChecklistItem(ItemKey.UMBRELLA).takeIf { rainyDays > 0 },
        ChecklistItem(ItemKey.LIP_BALM).takeIf { warmth == TripWarmth.WINTER },
        ChecklistItem(ItemKey.SUNSCREEN, maxUv.roundToInt()).takeIf { maxUv >= SUNSCREEN_UV },
        ChecklistItem(ItemKey.SUNGLASSES).takeIf { warmth == TripWarmth.SUMMER }
    )

    private companion object {
        const val RAINY_PERCENT = 50
        const val SUNSCREEN_UV = 5.0
    }
}
