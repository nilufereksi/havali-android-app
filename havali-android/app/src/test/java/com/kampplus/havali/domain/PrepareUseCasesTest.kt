package com.kampplus.havali.domain

import com.kampplus.havali.domain.model.ChecklistGroup
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.OutfitPiece
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.TripWarmth
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.usecase.prepare.BuildDailyChecklistUseCase
import com.kampplus.havali.domain.usecase.prepare.BuildPackingListUseCase
import com.kampplus.havali.domain.usecase.prepare.SuggestOutfitUseCase
import com.kampplus.havali.testing.TestWeather
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepareUseCasesTest {

    private val feelsCold = ComfortProfile(offsetC = -3.0, samples = 18)

    @Test
    fun `motorla yagmurlu gunde ekipman, kilit, lastik ve semsiye listede`() {
        val hours = listOf(TestWeather.snapshot(temperatureC = 14.0, rain = 70, kind = WeatherKind.RAIN))

        val sections = BuildDailyChecklistUseCase()(TravelMode.MOTORBIKE, hours, feelsCold)

        val mode = sections.single { it.group == ChecklistGroup.MODE }.items.map { it.key }
        val weather = sections.single { it.group == ChecklistGroup.WEATHER }.items
        assertEquals(listOf(ItemKey.PROTECTIVE_GEAR, ItemKey.VEHICLE_LOCK, ItemKey.TIRE_AND_BRAKE_CHECK, ItemKey.VISOR_ANTI_FOG), mode)
        assertEquals(70, weather.single { it.key == ItemKey.UMBRELLA }.value)
    }

    @Test
    fun `kuru ve bulutlu gunde semsiye ve krem onerilmez`() {
        val hours = listOf(TestWeather.snapshot(temperatureC = 20.0, uv = 1.0, kind = WeatherKind.CLOUDY))

        val sections = BuildDailyChecklistUseCase()(TravelMode.WALK, hours, ComfortProfile())

        assertTrue(sections.none { it.group == ChecklistGroup.WEATHER })
    }

    @Test
    fun `ne giysem kisisel sicakliga ve islakliga gore secer`() {
        val cold = SuggestOutfitUseCase()(minPersonalC = 8.0, wet = true)
        val warm = SuggestOutfitUseCase()(minPersonalC = 26.0, wet = false)

        assertEquals(OutfitPiece.THICK_SWEATER, cold.base)
        assertEquals(OutfitPiece.WATERPROOF_JACKET, cold.outer)
        assertEquals(OutfitPiece.WATERPROOF_BOOTS, cold.shoes)
        assertEquals(OutfitPiece.T_SHIRT, warm.base)
        assertNull(warm.outer)
        assertEquals(OutfitPiece.SANDALS, warm.shoes)
    }

    @Test
    fun `soguk Istanbul icin kislik valiz ve cabuk usuyene ek kazak`() {
        val start = LocalDate.of(2026, 11, 1)
        val izmir = (0..3).map { TestWeather.day(start.plusDays(it.toLong()), 14.0, 22.0) }
        val istanbul = (0..3).map { TestWeather.day(start.plusDays(it.toLong()), 8.0, 13.0, rain = if (it < 2) 80 else 20) }

        val packing = BuildPackingListUseCase()(izmir, istanbul, TravelMode.PLANE, feelsCold)

        assertEquals(TripWarmth.WINTER, packing.warmth)
        assertEquals(2, packing.rainyDays)
        assertEquals(8, packing.coldestNightC)
        assertEquals(8, packing.temperatureGapC)
        val clothing = packing.sections.single { it.group == ChecklistGroup.CLOTHING }.items.map { it.key }
        assertTrue(ItemKey.COAT in clothing)
        assertTrue(ItemKey.EXTRA_SWEATER in clothing)
        val journey = packing.sections.single { it.group == ChecklistGroup.JOURNEY }.items.map { it.key }
        assertEquals(listOf(ItemKey.JACKET_ON_BOARD, ItemKey.CABIN_CARDIGAN), journey)
        assertFalse(ItemKey.SWIMWEAR in clothing)
    }
}
