package com.kampplus.havali.presentation.road

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kampplus.havali.data.catalog.PlaceCatalog
import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.TrafficCorridor
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.repository.AirQualityRepository
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.CommunityReportRepository
import com.kampplus.havali.domain.repository.GuideRepository
import com.kampplus.havali.domain.repository.MapRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.SettingsRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import com.kampplus.havali.domain.usecase.guide.DetectAlertsUseCase
import com.kampplus.havali.domain.usecase.guide.PredictTrafficUseCase
import com.kampplus.havali.domain.usecase.guide.SuggestAlternativesUseCase
import com.kampplus.havali.domain.usecase.health.BuildCareTipsUseCase
import com.kampplus.havali.domain.usecase.prepare.ModeEssentials
import com.kampplus.havali.domain.usecase.road.AssessRoadRiskUseCase
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.appContainer
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.common.loadState
import com.kampplus.havali.presentation.common.map
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Yol ve Sağlık ekranı. Yol sekmesi seçili şehrin (İzmir / Gemlik) tahminini yorumlar:
 * risk, fırtına-sis uyarıları ve aks aks trafik; sağlık sekmesi evin tahminini kullanır.
 */
class RoadHealthViewModel(
    private val places: PlaceRepository,
    private val weather: WeatherRepository,
    private val airQuality: AirQualityRepository,
    private val reports: CommunityReportRepository,
    private val comfort: ComfortRepository,
    private val maps: MapRepository,
    private val guide: GuideRepository,
    settings: SettingsRepository,
    private val assessRoadRisk: AssessRoadRiskUseCase,
    private val buildCareTips: BuildCareTipsUseCase,
    private val detectAlerts: DetectAlertsUseCase,
    private val predictTraffic: PredictTrafficUseCase,
    private val suggestAlternatives: SuggestAlternativesUseCase,
    private val autoPlay: Boolean = true,
    private val clock: () -> LocalDateTime = { LocalDateTime.now() }
) : ViewModel() {

    private val cities: List<Place> = listOf(places.home()) + places.knownPlaces().filter { it.id == PlaceCatalog.GEMLIK_ID }
    private val local = MutableStateFlow(LocalState(cityId = places.home().id))
    private val forecasts = MutableStateFlow<LoadState<Map<String, Forecast>>>(LoadState.Loading)
    private val geometry = MutableStateFlow<MapGeometry?>(null)
    private val air = MutableStateFlow<LoadState<AirQuality>>(LoadState.Loading)

    val uiState: StateFlow<RoadHealthUiState> = combine(
        combine(local, forecasts, geometry, ::Triple),
        combine(air, reports.reports, comfort.profile, ::Triple)
    ) { (localState, forecastState, geometryState), (airState, reportList, profile) ->
        RoadHealthUiState(
            tab = localState.tab,
            placeName = places.home().name,
            road = forecastState.map { roadUi(it, localState, profile, geometryState, reportList) },
            reports = reportList,
            reportFormOpen = localState.reportFormOpen,
            reportSent = localState.reportSent,
            health = forecastState.map { all -> all[places.home().id]?.let { healthUi(it, profile, localState) } },
            airQuality = airState
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), RoadHealthUiState())

    init {
        load()
        viewModelScope.launch { geometry.value = loadState { maps.loadGeometry() }.dataOrNull() }
        viewModelScope.launch { settings.dataSource.drop(1).collect { load() } }
        if (autoPlay) playTraffic()
    }

    fun onEvent(event: RoadHealthEvent) {
        when (event) {
            is RoadHealthEvent.SelectTab -> local.update { it.copy(tab = event.tab) }
            is RoadHealthEvent.SelectMode -> local.update { it.copy(mode = event.mode) }
            is RoadHealthEvent.SelectCity -> local.update { it.copy(cityId = event.cityId, selectedCorridorId = null) }
            is RoadHealthEvent.SelectTrafficStep -> local.update { it.copy(trafficStep = event.index, playing = false) }
            RoadHealthEvent.ToggleTrafficPlay -> local.update { it.copy(playing = !it.playing) }
            is RoadHealthEvent.SelectCorridor -> local.update { it.copy(selectedCorridorId = event.corridorId) }
            RoadHealthEvent.OpenReportForm -> local.update { it.copy(reportFormOpen = !it.reportFormOpen, reportSent = false) }
            is RoadHealthEvent.SendReport -> sendReport(event.type)
            is RoadHealthEvent.ToggleConfirm -> reports.toggleConfirm(event.reportId)
            is RoadHealthEvent.Dismiss -> reports.dismiss(event.reportId)
            RoadHealthEvent.ApplySunscreen -> local.update { it.copy(sunscreenAppliedAt = clock()) }
            RoadHealthEvent.Retry -> load()
        }
    }

    /** Ev ve trafik şehirlerinin tahmini tek çoklu istekle gelir; hava kalitesi ayrı servisten. */
    private fun load() {
        val home = places.home().point
        viewModelScope.launch {
            forecasts.value = LoadState.Loading
            forecasts.value = loadState { cities.map { it.id }.zip(weather.getForecasts(cities.map { it.point })).toMap() }
        }
        viewModelScope.launch {
            air.value = LoadState.Loading
            air.value = loadState { airQuality.getAirQuality(home) }
        }
    }

    /** Trafik zaman çubuğu oynatılıyorsa her adımda bir saat ileri gider. */
    private fun playTraffic() {
        viewModelScope.launch {
            while (isActive) {
                delay(TRAFFIC_STEP_MS)
                local.update { it.copy(trafficStep = it.nextStep()) }
            }
        }
    }

    private fun sendReport(type: ReportType) {
        val city = cities.first { it.id == local.value.cityId }
        reports.add(type, city.name, city.point)
        local.update { it.copy(reportFormOpen = false, reportSent = true) }
    }

    private fun roadUi(
        all: Map<String, Forecast>,
        state: LocalState,
        profile: ComfortProfile,
        geometryState: MapGeometry?,
        reportList: List<CommunityReport>
    ): RoadUi? {
        val city = cities.firstOrNull { it.id == state.cityId } ?: return null
        val forecast = all[city.id] ?: return null
        val hours = forecast.nextHours(ROAD_HOURS)
        return RoadUi(
            city = city,
            cities = cities,
            kind = hours.firstOrNull { it.kind.isWet }?.kind ?: forecast.current.kind,
            mode = state.mode,
            risk = assessRoadRisk(state.mode, hours),
            alerts = detectAlerts(city, hours),
            personalC = profile.feelsLikeFor(forecast.current.apparentTemperatureC).roundToInt(),
            gear = ModeEssentials.daily(state.mode),
            traffic = trafficUi(city, forecast, state, geometryState, reportList)
        )
    }

    private fun trafficUi(
        city: Place,
        forecast: Forecast,
        state: LocalState,
        geometryState: MapGeometry?,
        reportList: List<CommunityReport>
    ): TrafficUi {
        val firstHour = clock().truncatedTo(ChronoUnit.HOURS)
        val time = firstHour.plusHours(state.trafficStep.toLong())
        val corridors = guide.corridors(city.id).map { corridor ->
            predictTraffic(corridor, time, forecast.at(time), reportList.count { report -> corridor.passesNear(report.point) })
        }
        return TrafficUi(
            geometry = geometryState,
            corridors = corridors,
            labels = places.knownPlaces().filter { it.zoom >= MapZoom.DISTRICT && it.point.distanceKmTo(city.point) <= LABEL_RADIUS_KM },
            stepTimes = (0 until TRAFFIC_STEPS).map { firstHour.plusHours(it.toLong()).toLocalTime() },
            step = state.trafficStep,
            playing = state.playing,
            selected = corridors.firstOrNull { it.corridor.id == state.selectedCorridorId }
                ?: corridors.filterNot { it.corridor.isAlternative }.maxByOrNull { it.delayMinutes },
            userPicked = state.selectedCorridorId != null,
            alternatives = suggestAlternatives(corridors)
        )
    }

    private fun TrafficCorridor.passesNear(point: GeoPoint) = points.any { it.distanceKmTo(point) <= REPORT_RADIUS_KM }

    private fun healthUi(forecast: Forecast, profile: ComfortProfile, localState: LocalState): HealthUi {
        val hours = forecast.nextHours(ROAD_HOURS)
        val peak = hours.maxByOrNull { it.uvIndex }
        return HealthUi(
            profile = profile,
            uvMax = (peak?.uvIndex ?: forecast.current.uvIndex).roundToInt(),
            uvPeakAt = peak?.time?.toLocalTime(),
            sunscreenAppliedAt = localState.sunscreenAppliedAt,
            careTips = buildCareTips(forecast.current, profile)
        )
    }

    private data class LocalState(
        val tab: RoadHealthTab = RoadHealthTab.ROAD,
        val mode: TravelMode = TravelMode.MOTORBIKE,
        val cityId: String,
        val trafficStep: Int = 0,
        val playing: Boolean = true,
        val selectedCorridorId: String? = null,
        val reportFormOpen: Boolean = false,
        val reportSent: Boolean = false,
        val sunscreenAppliedAt: LocalDateTime? = null
    ) {
        fun nextStep(): Int = when (playing) {
            true -> (trafficStep + 1) % TRAFFIC_STEPS
            false -> trafficStep
        }
    }

    companion object {
        private const val ROAD_HOURS = 12
        private const val TRAFFIC_STEPS = 6
        private const val TRAFFIC_STEP_MS = 2200L
        private const val LABEL_RADIUS_KM = 25.0
        private const val REPORT_RADIUS_KM = 1.5
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                RoadHealthViewModel(
                    places = c.placeRepository,
                    weather = c.weatherRepository,
                    airQuality = c.airQualityRepository,
                    reports = c.reportRepository,
                    comfort = c.comfortRepository,
                    maps = c.mapRepository,
                    guide = c.guideRepository,
                    settings = c.settingsRepository,
                    assessRoadRisk = c.assessRoadRisk,
                    buildCareTips = c.buildCareTips,
                    detectAlerts = c.detectAlerts,
                    predictTraffic = c.predictTraffic,
                    suggestAlternatives = c.suggestAlternatives
                )
            }
        }
    }
}
