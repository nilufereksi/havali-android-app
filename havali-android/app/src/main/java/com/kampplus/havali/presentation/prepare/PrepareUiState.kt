package com.kampplus.havali.presentation.prepare

import com.kampplus.havali.domain.model.ChecklistSection
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.Outfit
import com.kampplus.havali.domain.model.PackingList
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.SummaryFact
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherAlert
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.common.LoadState
import java.time.LocalDate

enum class PrepareTab { TODAY, TRIP }

data class PrepareUiState(
    val tab: PrepareTab = PrepareTab.TODAY,
    val profile: ComfortProfile = ComfortProfile(),
    val daily: LoadState<DailyPrepUi> = LoadState.Loading,
    val trip: TripUi? = null
)

/** "Bugün çıkarken": ne giysem kartı ve ulaşım şekline göre kontrol listesi. */
data class DailyPrepUi(
    val mode: TravelMode,
    val nowC: Double,
    val laterC: Double,
    val minPersonalC: Double,
    val outfit: Outfit,
    val sections: List<ChecklistSection>,
    val checked: Set<ItemKey>,
    val rain: SummaryFact?,
    val kind: WeatherKind,
    val alerts: List<WeatherAlert>
) {
    val total: Int get() = sections.sumOf { it.items.size }
    val done: Int get() = sections.sumOf { section -> section.items.count { it.key in checked } }
}

/** "Valiz": nereden (isteğe bağlı), nereye, kaç gece, nasıl gidiliyor ve sonuç. */
data class TripUi(
    val choices: List<Place>,
    val origin: Place?,
    val destination: Place,
    val start: LocalDate,
    val nights: Int,
    val mode: TravelMode,
    val result: LoadState<TripResultUi>
)

data class TripResultUi(
    val originDays: List<DailyWeather>,
    val destinationDays: List<DailyWeather>,
    val packing: PackingList,
    val checked: Set<ItemKey>
) {
    val total: Int get() = packing.sections.sumOf { it.items.size }
    val done: Int get() = packing.sections.sumOf { section -> section.items.count { it.key in checked } }
}

sealed interface PrepareEvent {
    data class SelectTab(val tab: PrepareTab) : PrepareEvent
    data class SelectDailyMode(val mode: TravelMode) : PrepareEvent
    data class ToggleDaily(val key: ItemKey) : PrepareEvent
    data class SelectOrigin(val place: Place?) : PrepareEvent
    data class SelectDestination(val place: Place) : PrepareEvent
    data class ChangeNights(val delta: Int) : PrepareEvent
    data class SelectTripMode(val mode: TravelMode) : PrepareEvent
    data class ToggleTrip(val key: ItemKey) : PrepareEvent
    data object Retry : PrepareEvent
}
