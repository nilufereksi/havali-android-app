package com.kampplus.havali.presentation.today

import com.kampplus.havali.domain.model.ActivityPlan
import com.kampplus.havali.domain.model.CampingAdvice
import com.kampplus.havali.domain.model.ComfortFeedback
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.model.DepartureAdvice
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.Outfit
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.RiskLevel
import com.kampplus.havali.domain.model.SummaryFact
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.components.CityCardModel
import com.kampplus.havali.presentation.today.map.CameraTarget
import com.kampplus.havali.presentation.today.map.MapMarker
import java.time.LocalTime

/** "Bugün" ekranının çizilmesi için gereken her şey. Ekran yalnızca bunu okur. */
data class TodayUiState(
    val geometry: LoadState<MapGeometry> = LoadState.Loading,
    val markers: List<MapMarker> = emptyList(),
    val reportPoints: List<GeoPoint> = emptyList(),
    val camera: CameraTarget = CameraTarget.Turkey,
    val selected: Place? = null,
    val focus: LoadState<FocusWeather> = LoadState.Loading,
    val step: Int = 0,
    val stepTimes: List<LocalTime> = emptyList(),
    val playing: Boolean = true,
    val dataSource: DataSource = DataSource.DEMO,
    val quickPlaces: List<Place> = emptyList(),
    val campsitePoints: List<GeoPoint> = emptyList(),
    val nextDeparture: TodayPlanItem? = null,
    val shortcuts: TodayShortcuts? = null,
    val feedback: ComfortFeedback? = null,
    val search: SearchUiState = SearchUiState()
)

/**
 * Odaktaki yerin havası: harita kartı, "Bugün ne yapsam?" ve gün özeti bunu kullanır.
 * Hiçbir yer seçili değilken (Türkiye görünümü) odak kullanıcının şehridir.
 */
data class FocusWeather(
    val place: Place,
    val isHome: Boolean,
    val snapshot: WeatherSnapshot,
    val personalC: Double,
    val activities: ActivityPlan,
    val summary: List<SummaryFact>,
    val camping: List<CampingAdvice>
)

/**
 * Ana ekranın en altındaki yönlendirici kutucuklar: her biri bir sayfanın tek cümlelik özeti.
 * Dokununca ilgili sayfaya gidilir.
 */
data class TodayShortcuts(
    val pendingVoteBy: String?,
    val nextPlan: TodayPlanItem?,
    val roadMode: TravelMode,
    val roadRisk: RiskLevel,
    val uvMax: Int,
    val outfit: Outfit
)

data class TodayPlanItem(val plan: Plan, val weather: WeatherSnapshot?, val departure: DepartureAdvice?)

/** Şehir arama paneli. [remote] null ise çevrimiçi arama henüz yapılmadı demektir. */
data class SearchUiState(
    val query: String = "",
    val local: List<CityCardModel> = emptyList(),
    val remote: LoadState<List<CityCardModel>>? = null
)

/** Ekrandan ViewModel'e giden kullanıcı olayları. Tek giriş noktası, `when` ile dağıtılır. */
sealed interface TodayEvent {
    data class SelectPlace(val place: Place) : TodayEvent
    data object ShowTurkey : TodayEvent
    data class SelectStep(val index: Int) : TodayEvent
    data object TogglePlay : TodayEvent
    data object ToggleDataSource : TodayEvent
    data class GiveFeedback(val feedback: ComfortFeedback) : TodayEvent
    data class QueryChanged(val query: String) : TodayEvent
    data object SearchOnline : TodayEvent
    data object Retry : TodayEvent
}
