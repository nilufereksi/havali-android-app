package com.kampplus.havali.presentation.common

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.ActivityReason
import com.kampplus.havali.domain.model.ActivityType
import com.kampplus.havali.domain.model.AirQualityLevel
import com.kampplus.havali.domain.model.AlertType
import com.kampplus.havali.domain.model.CareTip
import com.kampplus.havali.domain.model.ChecklistGroup
import com.kampplus.havali.domain.model.ComfortFeedback
import com.kampplus.havali.domain.model.ComfortSensitivity
import com.kampplus.havali.domain.model.DelayFactor
import com.kampplus.havali.domain.model.HazardType
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.OutfitPiece
import com.kampplus.havali.domain.model.PlanFactor
import com.kampplus.havali.domain.model.PollenLevel
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.RiskLevel
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.TrafficLevel
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.TripWarmth
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.components.HavaliIcons

/*
 * Domain enum'larının ekrandaki karşılıkları (metin kaynağı ve ikon).
 *
 * Domain katmanı Türkçe metin bilmez; çeviri burada, tek yerde ve `when` ile yapılır.
 * `when` enum üzerinde tam olduğundan yeni bir sabit eklenince derleyici burayı hatırlatır.
 * Uygulamayı başka dile çevirmek için yalnızca values-xx/strings.xml eklemek yeter.
 */

@get:StringRes
val WeatherKind.labelRes: Int
    get() = when (this) {
        WeatherKind.CLEAR -> R.string.weather_clear
        WeatherKind.CLOUDY -> R.string.weather_cloudy
        WeatherKind.FOG -> R.string.weather_fog
        WeatherKind.RAIN -> R.string.weather_rain
        WeatherKind.SNOW -> R.string.weather_snow
        WeatherKind.STORM -> R.string.weather_storm
    }

val WeatherKind.icon: ImageVector
    get() = when (this) {
        WeatherKind.CLEAR -> HavaliIcons.Sun
        WeatherKind.CLOUDY -> HavaliIcons.Cloud
        WeatherKind.FOG -> HavaliIcons.Fog
        WeatherKind.RAIN -> HavaliIcons.Rain
        WeatherKind.SNOW -> HavaliIcons.Snow
        WeatherKind.STORM -> HavaliIcons.Storm
    }

@get:StringRes
val TravelMode.labelRes: Int
    get() = when (this) {
        TravelMode.WALK -> R.string.mode_walk
        TravelMode.BIKE -> R.string.mode_bike
        TravelMode.MOTORBIKE -> R.string.mode_motorbike
        TravelMode.CAR -> R.string.mode_car
        TravelMode.TRANSIT -> R.string.mode_transit
        TravelMode.PLANE -> R.string.mode_plane
        TravelMode.BUS -> R.string.mode_bus
        TravelMode.TRAIN -> R.string.mode_train
    }

val TravelMode.icon: ImageVector
    get() = when (this) {
        TravelMode.WALK -> HavaliIcons.Walk
        TravelMode.BIKE -> HavaliIcons.Bike
        TravelMode.MOTORBIKE -> HavaliIcons.Motorbike
        TravelMode.CAR -> HavaliIcons.Car
        TravelMode.TRANSIT -> HavaliIcons.Transit
        TravelMode.PLANE -> HavaliIcons.Plane
        TravelMode.BUS -> HavaliIcons.Bus
        TravelMode.TRAIN -> HavaliIcons.Train
    }

@get:StringRes
val ActivityType.labelRes: Int
    get() = when (this) {
        ActivityType.WINDSURF -> R.string.activity_windsurf
        ActivityType.KITESURF -> R.string.activity_kitesurf
        ActivityType.SUP -> R.string.activity_sup
        ActivityType.KAYAK -> R.string.activity_kayak
        ActivityType.SKIING -> R.string.activity_skiing
        ActivityType.SNOWBOARD -> R.string.activity_snowboard
        ActivityType.SEA_SWIMMING -> R.string.activity_sea_swimming
        ActivityType.HIKING -> R.string.activity_hiking
        ActivityType.RUNNING -> R.string.activity_running
        ActivityType.CYCLING -> R.string.activity_cycling
        ActivityType.INDOOR_POOL -> R.string.activity_indoor_pool
    }

val ActivityType.icon: ImageVector
    get() = when (this) {
        ActivityType.WINDSURF, ActivityType.KITESURF -> HavaliIcons.Sail
        ActivityType.SUP, ActivityType.KAYAK -> HavaliIcons.Wave
        ActivityType.SKIING, ActivityType.SNOWBOARD -> HavaliIcons.Ski
        ActivityType.SEA_SWIMMING, ActivityType.INDOOR_POOL -> HavaliIcons.Swim
        ActivityType.HIKING -> HavaliIcons.Mountain
        ActivityType.RUNNING -> HavaliIcons.Run
        ActivityType.CYCLING -> HavaliIcons.Bike
    }

/** Gerekçe cümlesi; `%1$d` yerine kararın sayısı yazılır. */
@get:StringRes
val ActivityReason.textRes: Int
    get() = when (this) {
        ActivityReason.INDOOR_BEST -> R.string.reason_indoor_best
        ActivityReason.OUTDOOR_BETTER -> R.string.reason_outdoor_better
        ActivityReason.MILD_AND_DRY -> R.string.reason_mild_and_dry
        ActivityReason.CALM_WATER -> R.string.reason_calm_water
        ActivityReason.GOOD_WIND -> R.string.reason_good_wind
        ActivityReason.WIND_TOO_WEAK -> R.string.reason_wind_too_weak
        ActivityReason.WIND_TOO_STRONG -> R.string.reason_wind_too_strong
        ActivityReason.RAIN_LIKELY -> R.string.reason_rain_likely
        ActivityReason.TOO_COLD -> R.string.reason_too_cold
        ActivityReason.TOO_HOT -> R.string.reason_too_hot
        ActivityReason.FRESH_SNOW -> R.string.reason_fresh_snow
        ActivityReason.NO_SNOW -> R.string.reason_no_snow
        ActivityReason.ICY_GROUND -> R.string.reason_icy_ground
        ActivityReason.LOW_VISIBILITY -> R.string.reason_low_visibility
    }

@get:StringRes
val Suitability.labelRes: Int
    get() = when (this) {
        Suitability.GOOD -> R.string.verdict_good
        Suitability.CAUTION -> R.string.verdict_caution
        Suitability.BAD -> R.string.verdict_bad
    }

@get:StringRes
val ItemKey.labelRes: Int
    get() = when (this) {
        ItemKey.UMBRELLA -> R.string.item_umbrella
        ItemKey.SUNSCREEN -> R.string.item_sunscreen
        ItemKey.LIP_BALM -> R.string.item_lip_balm
        ItemKey.SUNGLASSES -> R.string.item_sunglasses
        ItemKey.WATERPROOF_SHOES -> R.string.item_waterproof_shoes
        ItemKey.SPARE_SOCKS -> R.string.item_spare_socks
        ItemKey.PROTECTIVE_GEAR -> R.string.item_protective_gear
        ItemKey.VEHICLE_LOCK -> R.string.item_vehicle_lock
        ItemKey.TIRE_AND_BRAKE_CHECK -> R.string.item_tire_and_brake_check
        ItemKey.VISOR_ANTI_FOG -> R.string.item_visor_anti_fog
        ItemKey.HELMET_AND_VEST -> R.string.item_helmet_and_vest
        ItemKey.BIKE_LIGHTS -> R.string.item_bike_lights
        ItemKey.TIRE_PRESSURE -> R.string.item_tire_pressure
        ItemKey.WIPER_FLUID -> R.string.item_wiper_fluid
        ItemKey.SPARE_UMBRELLA_CAR -> R.string.item_spare_umbrella_car
        ItemKey.TRANSIT_CARD -> R.string.item_transit_card
        ItemKey.FERRY_LAYER -> R.string.item_ferry_layer
        ItemKey.COAT -> R.string.item_coat
        ItemKey.SWEATER -> R.string.item_sweater
        ItemKey.EXTRA_SWEATER -> R.string.item_extra_sweater
        ItemKey.LIGHT_LAYER -> R.string.item_light_layer
        ItemKey.LONG_TROUSERS -> R.string.item_long_trousers
        ItemKey.SHORTS -> R.string.item_shorts
        ItemKey.T_SHIRTS -> R.string.item_t_shirts
        ItemKey.SWIMWEAR -> R.string.item_swimwear
        ItemKey.JACKET_ON_BOARD -> R.string.item_jacket_on_board
        ItemKey.CABIN_CARDIGAN -> R.string.item_cabin_cardigan
        ItemKey.NECK_PILLOW -> R.string.item_neck_pillow
        ItemKey.KEEP_JACKET_CLOSE -> R.string.item_keep_jacket_close
        ItemKey.LEAVE_EARLY -> R.string.item_leave_early
        ItemKey.BAG_COVER -> R.string.item_bag_cover
        ItemKey.DRESS_IN_LAYERS -> R.string.item_dress_in_layers
    }

/** Maddenin sayısı yoksa gösterilen sabit açıklama. */
@get:StringRes
val ItemKey.noteRes: Int
    get() = when (this) {
        ItemKey.UMBRELLA, ItemKey.SPARE_UMBRELLA_CAR, ItemKey.BAG_COVER -> R.string.note_rain
        ItemKey.SUNSCREEN, ItemKey.SUNGLASSES, ItemKey.SWIMWEAR -> R.string.note_sun
        ItemKey.LIP_BALM -> R.string.note_lip_balm
        ItemKey.WATERPROOF_SHOES, ItemKey.SPARE_SOCKS -> R.string.note_wet_ground
        ItemKey.PROTECTIVE_GEAR, ItemKey.HELMET_AND_VEST, ItemKey.BIKE_LIGHTS -> R.string.note_safety
        ItemKey.VEHICLE_LOCK -> R.string.note_lock
        ItemKey.TIRE_AND_BRAKE_CHECK, ItemKey.TIRE_PRESSURE, ItemKey.WIPER_FLUID -> R.string.note_vehicle_check
        ItemKey.VISOR_ANTI_FOG -> R.string.note_visor
        ItemKey.TRANSIT_CARD -> R.string.note_transit_card
        ItemKey.FERRY_LAYER -> R.string.note_ferry
        ItemKey.COAT, ItemKey.SWEATER, ItemKey.LONG_TROUSERS -> R.string.note_cold_destination
        ItemKey.EXTRA_SWEATER -> R.string.note_extra_sweater
        ItemKey.LIGHT_LAYER, ItemKey.DRESS_IN_LAYERS -> R.string.note_layers
        ItemKey.SHORTS, ItemKey.T_SHIRTS -> R.string.note_warm_destination
        ItemKey.JACKET_ON_BOARD, ItemKey.KEEP_JACKET_CLOSE -> R.string.note_jacket_close
        ItemKey.CABIN_CARDIGAN -> R.string.note_cabin
        ItemKey.NECK_PILLOW -> R.string.note_long_ride
        ItemKey.LEAVE_EARLY -> R.string.note_leave_early
    }

/** Maddenin sayısı varsa ipucu cümlesi bu biçimle kurulur. */
@get:StringRes
val ItemKey.valueHintRes: Int
    get() = when (this) {
        ItemKey.UMBRELLA, ItemKey.WIPER_FLUID, ItemKey.TIRE_AND_BRAKE_CHECK -> R.string.hint_rain_percent
        ItemKey.SUNSCREEN -> R.string.hint_uv
        ItemKey.LIP_BALM, ItemKey.PROTECTIVE_GEAR -> R.string.hint_personal_temperature
        ItemKey.FERRY_LAYER -> R.string.hint_gust
        ItemKey.WATERPROOF_SHOES -> R.string.hint_rainy_days
        else -> R.string.hint_count
    }

@get:StringRes
val ChecklistGroup.titleRes: Int
    get() = when (this) {
        ChecklistGroup.MODE -> R.string.group_mode
        ChecklistGroup.WEATHER -> R.string.group_weather
        ChecklistGroup.CLOTHING -> R.string.group_clothing
        ChecklistGroup.RAIN_AND_CARE -> R.string.group_rain_and_care
        ChecklistGroup.JOURNEY -> R.string.group_journey
    }

@get:StringRes
val HazardType.titleRes: Int
    get() = when (this) {
        HazardType.SLIPPERY_ROAD -> R.string.hazard_slippery
        HazardType.STRONG_GUSTS -> R.string.hazard_gusts
        HazardType.LOW_VISIBILITY -> R.string.hazard_visibility
        HazardType.ICE -> R.string.hazard_ice
        HazardType.HEAVY_TRAFFIC -> R.string.hazard_traffic
    }

@get:StringRes
val HazardType.valueRes: Int
    get() = when (this) {
        HazardType.SLIPPERY_ROAD -> R.string.hazard_slippery_value
        HazardType.STRONG_GUSTS -> R.string.hazard_gusts_value
        HazardType.LOW_VISIBILITY -> R.string.hazard_visibility_value
        HazardType.ICE -> R.string.hazard_ice_value
        HazardType.HEAVY_TRAFFIC -> R.string.hazard_traffic_value
    }

val HazardType.icon: ImageVector
    get() = when (this) {
        HazardType.SLIPPERY_ROAD -> HavaliIcons.Drop
        HazardType.STRONG_GUSTS -> HavaliIcons.Wind
        HazardType.LOW_VISIBILITY -> HavaliIcons.Fog
        HazardType.ICE -> HavaliIcons.Snow
        HazardType.HEAVY_TRAFFIC -> HavaliIcons.Car
    }

@get:StringRes
val RiskLevel.labelRes: Int
    get() = when (this) {
        RiskLevel.VERY_LOW -> R.string.risk_very_low
        RiskLevel.LOW -> R.string.risk_low
        RiskLevel.MEDIUM -> R.string.risk_medium
        RiskLevel.HIGH -> R.string.risk_high
        RiskLevel.VERY_HIGH -> R.string.risk_very_high
    }

/** Riskin üç renk kovası: Rahat / Dikkat / Riskli. */
val RiskLevel.suitability: Suitability
    get() = when (this) {
        RiskLevel.VERY_LOW, RiskLevel.LOW -> Suitability.GOOD
        RiskLevel.MEDIUM -> Suitability.CAUTION
        RiskLevel.HIGH, RiskLevel.VERY_HIGH -> Suitability.BAD
    }

@get:StringRes
val ReportType.labelRes: Int
    get() = when (this) {
        ReportType.PUDDLE -> R.string.report_puddle
        ReportType.SLIPPERY_ROAD -> R.string.report_slippery
        ReportType.ICE -> R.string.report_ice
        ReportType.FOG -> R.string.report_fog
        ReportType.FALLEN_TREE -> R.string.report_fallen_tree
        ReportType.ACCIDENT -> R.string.report_accident
        ReportType.ROADWORK -> R.string.report_roadwork
    }

@get:StringRes
val DelayFactor.textRes: Int
    get() = when (this) {
        DelayFactor.RAIN -> R.string.delay_rain
        DelayFactor.RUSH_HOUR -> R.string.delay_rush_hour
        DelayFactor.COMMUNITY_REPORTS -> R.string.delay_reports
        DelayFactor.LOW_VISIBILITY -> R.string.delay_visibility
    }

@get:StringRes
val PlanFactor.labelRes: Int
    get() = when (this) {
        PlanFactor.RAIN -> R.string.factor_rain
        PlanFactor.TRAFFIC -> R.string.factor_traffic
        PlanFactor.TEMPERATURE -> R.string.factor_temperature
    }

@get:StringRes
val CareTip.titleRes: Int
    get() = when (this) {
        CareTip.HUMID_SKIN -> R.string.care_humid_title
        CareTip.DRY_SKIN -> R.string.care_dry_title
        CareTip.WINDY_LIPS -> R.string.care_wind_title
        CareTip.HIGH_UV -> R.string.care_uv_title
        CareTip.COLD_HANDS -> R.string.care_cold_title
    }

@get:StringRes
val CareTip.bodyRes: Int
    get() = when (this) {
        CareTip.HUMID_SKIN -> R.string.care_humid_body
        CareTip.DRY_SKIN -> R.string.care_dry_body
        CareTip.WINDY_LIPS -> R.string.care_wind_body
        CareTip.HIGH_UV -> R.string.care_uv_body
        CareTip.COLD_HANDS -> R.string.care_cold_body
    }

@get:StringRes
val AirQualityLevel.labelRes: Int
    get() = when (this) {
        AirQualityLevel.GOOD -> R.string.air_good
        AirQualityLevel.FAIR -> R.string.air_fair
        AirQualityLevel.MODERATE -> R.string.air_moderate
        AirQualityLevel.POOR -> R.string.air_poor
        AirQualityLevel.UNKNOWN -> R.string.unknown
    }

@get:StringRes
val AirQualityLevel.noteRes: Int
    get() = when (this) {
        AirQualityLevel.GOOD, AirQualityLevel.FAIR -> R.string.air_note_sport_ok
        AirQualityLevel.MODERATE -> R.string.air_note_sensitive
        AirQualityLevel.POOR -> R.string.air_note_stay_inside
        AirQualityLevel.UNKNOWN -> R.string.unknown
    }

@get:StringRes
val PollenLevel.labelRes: Int
    get() = when (this) {
        PollenLevel.LOW -> R.string.pollen_low
        PollenLevel.MODERATE -> R.string.pollen_moderate
        PollenLevel.HIGH -> R.string.pollen_high
        PollenLevel.UNKNOWN -> R.string.unknown
    }

@get:StringRes
val OutfitPiece.labelRes: Int
    get() = when (this) {
        OutfitPiece.T_SHIRT -> R.string.outfit_t_shirt
        OutfitPiece.LIGHT_SWEATER -> R.string.outfit_light_sweater
        OutfitPiece.THICK_SWEATER -> R.string.outfit_thick_sweater
        OutfitPiece.LIGHT_JACKET -> R.string.outfit_light_jacket
        OutfitPiece.WATERPROOF_JACKET -> R.string.outfit_waterproof_jacket
        OutfitPiece.COAT -> R.string.outfit_coat
        OutfitPiece.SANDALS -> R.string.outfit_sandals
        OutfitPiece.CLOSED_SHOES -> R.string.outfit_closed_shoes
        OutfitPiece.WATERPROOF_BOOTS -> R.string.outfit_waterproof_boots
    }

@get:StringRes
val TripWarmth.labelRes: Int
    get() = when (this) {
        TripWarmth.SUMMER -> R.string.warmth_summer
        TripWarmth.SEASONAL -> R.string.warmth_seasonal
        TripWarmth.WINTER -> R.string.warmth_winter
    }

@get:StringRes
val ComfortSensitivity.labelRes: Int
    get() = when (this) {
        ComfortSensitivity.FEELS_COLD -> R.string.sensitivity_cold
        ComfortSensitivity.NEUTRAL -> R.string.sensitivity_neutral
        ComfortSensitivity.FEELS_WARM -> R.string.sensitivity_warm
    }

@get:StringRes
val ComfortFeedback.labelRes: Int
    get() = when (this) {
        ComfortFeedback.COLD -> R.string.feedback_cold
        ComfortFeedback.OK -> R.string.feedback_ok
        ComfortFeedback.HOT -> R.string.feedback_hot
    }

@get:StringRes
val AlertType.titleRes: Int
    get() = when (this) {
        AlertType.STORM -> R.string.alert_storm_title
        AlertType.WAVES -> R.string.alert_waves_title
        AlertType.FOG -> R.string.alert_fog_title
        AlertType.HEAVY_RAIN -> R.string.alert_rain_title
    }

/** Uyarı metni; `%1$s` yer adı, `%2$d` uyarının büyüklüğüdür. */
@get:StringRes
val AlertType.bodyRes: Int
    get() = when (this) {
        AlertType.STORM -> R.string.alert_storm_body
        AlertType.WAVES -> R.string.alert_waves_body
        AlertType.FOG -> R.string.alert_fog_body
        AlertType.HEAVY_RAIN -> R.string.alert_rain_body
    }

@get:StringRes
val TrafficLevel.labelRes: Int
    get() = when (this) {
        TrafficLevel.LIGHT -> R.string.traffic_light
        TrafficLevel.MODERATE -> R.string.traffic_moderate
        TrafficLevel.HEAVY -> R.string.traffic_heavy
    }

/** Bildirim için kısa uyarı ("Alsancak: dalga, sahile inme"). */
@get:StringRes
val AlertType.shortRes: Int
    get() = when (this) {
        AlertType.STORM -> R.string.alert_storm_short
        AlertType.WAVES -> R.string.alert_waves_short
        AlertType.FOG -> R.string.alert_fog_short
        AlertType.HEAVY_RAIN -> R.string.alert_rain_short
    }
