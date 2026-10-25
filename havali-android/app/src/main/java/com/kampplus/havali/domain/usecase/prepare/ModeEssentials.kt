package com.kampplus.havali.domain.usecase.prepare

import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.TravelMode

/**
 * Her ulaşım şeklinin kendine özgü hatırlatmaları.
 *
 * Motor seçilince "koruyucu ekipmanın, kilit, lastik kontrolü" gelir; uçakta ise
 * "montu valize koyma, üstüne giy". Kural tek `when`'de durur, ekran bunu bilmez.
 */
object ModeEssentials {

    /** Günlük çıkış ("Bugün çıkarken") için. */
    fun daily(mode: TravelMode): List<ItemKey> = when (mode) {
        TravelMode.WALK -> listOf(ItemKey.WATERPROOF_SHOES, ItemKey.SPARE_SOCKS)
        TravelMode.BIKE -> listOf(ItemKey.HELMET_AND_VEST, ItemKey.VEHICLE_LOCK, ItemKey.TIRE_PRESSURE, ItemKey.BIKE_LIGHTS)
        TravelMode.MOTORBIKE -> listOf(
            ItemKey.PROTECTIVE_GEAR,
            ItemKey.VEHICLE_LOCK,
            ItemKey.TIRE_AND_BRAKE_CHECK,
            ItemKey.VISOR_ANTI_FOG
        )
        TravelMode.CAR -> listOf(ItemKey.TIRE_PRESSURE, ItemKey.WIPER_FLUID, ItemKey.SPARE_UMBRELLA_CAR)
        TravelMode.TRANSIT -> listOf(ItemKey.TRANSIT_CARD, ItemKey.FERRY_LAYER)
        TravelMode.PLANE, TravelMode.BUS, TravelMode.TRAIN -> trip(mode)
    }

    /** Şehirler arası yolculuk (Valiz) için. */
    fun trip(mode: TravelMode): List<ItemKey> = when (mode) {
        TravelMode.PLANE -> listOf(ItemKey.JACKET_ON_BOARD, ItemKey.CABIN_CARDIGAN)
        TravelMode.BUS -> listOf(ItemKey.KEEP_JACKET_CLOSE, ItemKey.NECK_PILLOW)
        TravelMode.CAR -> listOf(ItemKey.TIRE_PRESSURE, ItemKey.LEAVE_EARLY, ItemKey.WIPER_FLUID)
        TravelMode.MOTORBIKE -> listOf(
            ItemKey.PROTECTIVE_GEAR,
            ItemKey.VEHICLE_LOCK,
            ItemKey.TIRE_AND_BRAKE_CHECK,
            ItemKey.BAG_COVER
        )
        TravelMode.TRAIN -> listOf(ItemKey.DRESS_IN_LAYERS, ItemKey.UMBRELLA)
        TravelMode.WALK, TravelMode.BIKE, TravelMode.TRANSIT -> daily(mode)
    }
}
