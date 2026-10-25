package com.kampplus.havali.presentation.road

import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.AlternativeRoute
import com.kampplus.havali.domain.model.CareAdvice
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.CorridorTraffic
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.RoadRisk
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherAlert
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.presentation.common.LoadState
import java.time.LocalDateTime
import java.time.LocalTime

enum class RoadHealthTab { ROAD, HEALTH }

data class RoadHealthUiState(
    val tab: RoadHealthTab = RoadHealthTab.ROAD,
    val placeName: String = "",
    val road: LoadState<RoadUi> = LoadState.Loading,
    val reports: List<CommunityReport> = emptyList(),
    val reportFormOpen: Boolean = false,
    val reportSent: Boolean = false,
    val health: LoadState<HealthUi> = LoadState.Loading,
    val airQuality: LoadState<AirQuality> = LoadState.Loading
)

/** Yol sekmesi: seçili şehir ve ulaşım şekli için risk, uyarılar, trafik, kişisel hissedilen ve ekipman. */
data class RoadUi(
    val city: Place,
    val cities: List<Place>,
    val kind: WeatherKind,
    val mode: TravelMode,
    val risk: RoadRisk,
    val alerts: List<WeatherAlert>,
    val personalC: Int,
    val gear: List<ItemKey>,
    val traffic: TrafficUi
)

/** Trafik haritası: aksların saatlik tahmini ve zaman çubuğu. */
data class TrafficUi(
    val geometry: MapGeometry?,
    val corridors: List<CorridorTraffic>,
    val labels: List<Place>,
    val stepTimes: List<LocalTime>,
    val step: Int,
    val playing: Boolean,
    val selected: CorridorTraffic?,
    val userPicked: Boolean,
    val alternatives: List<AlternativeRoute>
) {
    /** Seçili (yoksa en yoğun) aks için önerilen yan yol. */
    val focusAlternative: AlternativeRoute?
        get() = alternatives.firstOrNull { it.main.corridor.id == selected?.corridor?.id }

    /** Şu an önerilen (haritada saks mavisiyle çizilecek) yan yolların kimlikleri. */
    val suggestedIds: Set<String> get() = alternatives.map { it.alternative.corridor.id }.toSet()
}

/** Sağlık sekmesi: konfor profili, UV ve güneş kremi, cilt/sağlık ipuçları. */
data class HealthUi(
    val profile: ComfortProfile,
    val uvMax: Int,
    val uvPeakAt: LocalTime?,
    val sunscreenAppliedAt: LocalDateTime?,
    val careTips: List<CareAdvice>
)

sealed interface RoadHealthEvent {
    data class SelectTab(val tab: RoadHealthTab) : RoadHealthEvent
    data class SelectMode(val mode: TravelMode) : RoadHealthEvent
    data class SelectCity(val cityId: String) : RoadHealthEvent
    data class SelectTrafficStep(val index: Int) : RoadHealthEvent
    data object ToggleTrafficPlay : RoadHealthEvent
    data class SelectCorridor(val corridorId: String) : RoadHealthEvent
    data object OpenReportForm : RoadHealthEvent
    data class SendReport(val type: ReportType) : RoadHealthEvent
    data class ToggleConfirm(val reportId: String) : RoadHealthEvent
    data class Dismiss(val reportId: String) : RoadHealthEvent
    data object ApplySunscreen : RoadHealthEvent
    data object Retry : RoadHealthEvent
}
