package com.kampplus.havali.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kampplus.havali.data.catalog.PlaceCatalog
import com.kampplus.havali.domain.model.Campsite
import com.kampplus.havali.domain.model.ComfortFeedback
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.countNear
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.CommunityReportRepository
import com.kampplus.havali.domain.repository.GroupVoteRepository
import com.kampplus.havali.domain.repository.GuideRepository
import com.kampplus.havali.domain.repository.MapRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.PlanRepository
import com.kampplus.havali.domain.repository.SettingsRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import com.kampplus.havali.domain.usecase.activity.EvaluateActivitiesUseCase
import com.kampplus.havali.domain.usecase.guide.EvaluateCampingUseCase
import com.kampplus.havali.domain.usecase.health.RecordComfortFeedbackUseCase
import com.kampplus.havali.domain.usecase.place.GetMapWeatherUseCase
import com.kampplus.havali.domain.usecase.place.SearchPlacesUseCase
import com.kampplus.havali.domain.usecase.plan.EstimateDepartureUseCase
import com.kampplus.havali.domain.usecase.prepare.SuggestOutfitUseCase
import com.kampplus.havali.domain.usecase.road.AssessRoadRiskUseCase
import com.kampplus.havali.domain.usecase.summary.SummarizeDayUseCase
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.appContainer
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.common.loadState
import com.kampplus.havali.presentation.common.map
import com.kampplus.havali.presentation.components.CityCardModel
import com.kampplus.havali.presentation.today.map.CameraTarget
import com.kampplus.havali.presentation.today.map.MapMarker
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
 * "Bugün" ekranının ViewModel'i.
 *
 * Ekran döndürülse de yaşar; veriyi bir kez yükler. Bütün ekran durumu tek bir
 * [StateFlow] olarak yayınlanır (tek doğruluk kaynağı). Ağ ve dosya işleri
 * `viewModelScope.launch` içinde başlar; ViewModel yok olunca coroutine'ler de iptal edilir.
 */
class TodayViewModel(
    private val places: PlaceRepository,
    private val weather: WeatherRepository,
    private val maps: MapRepository,
    private val settings: SettingsRepository,
    comfort: ComfortRepository,
    plans: PlanRepository,
    reports: CommunityReportRepository,
    groupVotes: GroupVoteRepository,
    private val guide: GuideRepository,
    private val getMapWeather: GetMapWeatherUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val evaluateActivities: EvaluateActivitiesUseCase,
    private val summarizeDay: SummarizeDayUseCase,
    private val estimateDeparture: EstimateDepartureUseCase,
    private val recordFeedback: RecordComfortFeedbackUseCase,
    private val evaluateCamping: EvaluateCampingUseCase,
    private val assessRoadRisk: AssessRoadRiskUseCase,
    private val suggestOutfit: SuggestOutfitUseCase,
    private val clock: () -> LocalDateTime = { LocalDateTime.now() }
) : ViewModel() {

    private val local = MutableStateFlow(LocalState())
    private val geometry = MutableStateFlow<LoadState<MapGeometry>>(LoadState.Loading)
    private val mapWeather = MutableStateFlow<LoadState<Map<String, Forecast>>>(LoadState.Loading)

    val uiState: StateFlow<TodayUiState> = combine(
        combine(local, geometry, mapWeather, ::Triple),
        combine(settings.dataSource, comfort.profile, plans.plans, reports.reports, groupVotes.proposal, ::Context)
    ) { (localState, geometryState, weatherState), context ->
        buildState(localState, geometryState, weatherState, context)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TodayUiState())

    init {
        loadGeometry()
        loadMapWeather()
        playIntro()
        playTimeline()
        reloadWhenSourceChanges()
    }

    fun onEvent(event: TodayEvent) {
        when (event) {
            is TodayEvent.SelectPlace -> select(event.place)
            TodayEvent.ShowTurkey -> local.update { it.copy(selected = null, touched = true, customForecast = null) }
            is TodayEvent.SelectStep -> local.update { it.copy(step = event.index, playing = false) }
            TodayEvent.TogglePlay -> local.update { it.copy(playing = !it.playing) }
            TodayEvent.ToggleDataSource -> settings.setDataSource(settings.dataSource.value.other())
            is TodayEvent.GiveFeedback -> giveFeedback(event.feedback)
            is TodayEvent.QueryChanged -> filterKnown(event.query)
            TodayEvent.SearchOnline -> searchOnline()
            TodayEvent.Retry -> retry()
        }
    }

    private fun loadGeometry() {
        viewModelScope.launch { geometry.value = loadState { maps.loadGeometry() } }
    }

    private fun loadMapWeather() {
        viewModelScope.launch {
            mapWeather.value = LoadState.Loading
            mapWeather.value = loadState(isEmpty = { it.isEmpty() }) { getMapWeather() }
        }
    }

    /** Açılışta Türkiye'den kullanıcının şehrine yaklaşılır; kullanıcı o arada haritaya dokunduysa yaklaşılmaz. */
    private fun playIntro() {
        viewModelScope.launch {
            delay(INTRO_MS)
            local.update { state ->
                when (state.touched) {
                    true -> state
                    false -> state.copy(selected = places.home())
                }
            }
        }
    }

    /** Zaman çubuğu oynatılıyorsa her adımda bir sonraki saate geçer. */
    private fun playTimeline() {
        viewModelScope.launch {
            while (isActive) {
                delay(STEP_MS)
                local.update { it.copy(step = it.nextStep()) }
            }
        }
    }

    private fun reloadWhenSourceChanges() {
        viewModelScope.launch {
            settings.dataSource.drop(1).collect {
                loadMapWeather()
                local.value.selected?.let(::loadCustomForecastIfNeeded)
            }
        }
    }

    private fun select(place: Place) {
        local.update { it.copy(selected = place, touched = true, customForecast = null) }
        loadCustomForecastIfNeeded(place)
    }

    /** Haritada olmayan (aramayla bulunan) bir yer seçildiyse onun tahmini ayrıca istenir. */
    private fun loadCustomForecastIfNeeded(place: Place) {
        place.takeUnless { known -> places.knownPlaces().any { it.id == known.id } }?.let { custom ->
            viewModelScope.launch {
                local.update { it.copy(customForecast = LoadState.Loading) }
                val result = loadState { weather.getForecast(custom.point) }
                local.update { it.copy(customForecast = result) }
            }
        }
    }

    private fun giveFeedback(feedback: ComfortFeedback) {
        recordFeedback(feedback)
        local.update { it.copy(feedback = feedback) }
    }

    private fun filterKnown(query: String) {
        val forecasts = mapWeather.value.dataOrNull().orEmpty()
        val matches = searchPlaces.filterKnown(query).take(MAX_LOCAL_RESULTS).map { it.toCard(forecasts[it.id]) }
        local.update { it.copy(search = SearchUiState(query = query, local = matches, remote = null)) }
    }

    private fun searchOnline() {
        val query = local.value.search.query
        viewModelScope.launch {
            local.update { it.copy(search = it.search.copy(remote = LoadState.Loading)) }
            val result = loadState(isEmpty = { it.isEmpty() }) { searchPlaces.searchOnline(query).map { it.toCard(null) } }
            local.update { it.copy(search = it.search.copy(remote = result)) }
        }
    }

    private fun retry() {
        loadGeometry()
        loadMapWeather()
        local.value.selected?.let(::loadCustomForecastIfNeeded)
    }

    private fun buildState(
        localState: LocalState,
        geometryState: LoadState<MapGeometry>,
        weatherState: LoadState<Map<String, Forecast>>,
        context: Context
    ): TodayUiState {
        val now = clock()
        val hoursAhead = STEP_HOURS[localState.step]
        val forecasts = weatherState.dataOrNull().orEmpty()
        val home = places.home()
        val focusPlace = localState.selected ?: home
        val focusForecast = localState.customForecast ?: weatherState.map { it[focusPlace.id] }
        val todayPlans = context.plans
            .filter { it.start.toLocalDate() == now.toLocalDate() }
            .map { plan -> planItem(plan, forecasts[home.id], context.reports) }
        val nextDeparture = todayPlans.firstOrNull { it.departure != null && it.plan.outdoor && it.plan.start.isAfter(now) }
        return TodayUiState(
            geometry = geometryState,
            markers = markers(localState, forecasts, hoursAhead),
            reportPoints = context.reports.map { it.point },
            camera = localState.selected?.let(CameraTarget::of) ?: CameraTarget.Turkey,
            selected = localState.selected,
            focus = focusForecast.map { focusWeather(focusPlace, it, hoursAhead, context.profile) },
            step = localState.step,
            stepTimes = STEP_HOURS.map { now.truncatedTo(ChronoUnit.HOURS).plusHours(it.toLong()).toLocalTime() },
            playing = localState.playing,
            dataSource = context.dataSource,
            quickPlaces = PlaceCatalog.QUICK_IDS.mapNotNull { id -> places.knownPlaces().firstOrNull { it.id == id } },
            campsitePoints = guide.campsites().map { it.point },
            nextDeparture = nextDeparture,
            shortcuts = forecasts[home.id]?.let { homeForecast ->
                val upcoming = context.plans.firstOrNull { !it.done && it.start.isAfter(now) }
                shortcuts(homeForecast, context, nextDeparture ?: upcoming?.let { planItem(it, homeForecast, context.reports) })
            },
            feedback = localState.feedback,
            search = localState.search
        )
    }

    /** Alt kutucuklar evin tahmininden beslenir: oylama, yol riski, UV ve kıyafet. */
    private fun shortcuts(home: Forecast, context: Context, nextPlan: TodayPlanItem?): TodayShortcuts {
        val hours = home.nextHours(SHORTCUT_HOURS).ifEmpty { listOf(home.current) }
        val minPersonal = hours.minOf { context.profile.feelsLikeFor(it.apparentTemperatureC) }
        return TodayShortcuts(
            pendingVoteBy = context.proposal.proposer.name.takeIf { context.proposal.myVote == null },
            nextPlan = nextPlan,
            roadMode = SHORTCUT_MODE,
            roadRisk = assessRoadRisk(SHORTCUT_MODE, hours).level,
            uvMax = hours.maxOf { it.uvIndex }.roundToInt(),
            outfit = suggestOutfit(minPersonal, hours.any { it.kind.isWet || it.precipitationProbability >= WET_PERCENT })
        )
    }

    private fun markers(localState: LocalState, forecasts: Map<String, Forecast>, hoursAhead: Int): List<MapMarker> {
        val custom = localState.selected?.takeIf { selected -> places.knownPlaces().none { it.id == selected.id } }
        val customForecast = localState.customForecast?.dataOrNull()
        val known = places.knownPlaces().map { place ->
            MapMarker(place, forecasts[place.id]?.after(hoursAhead), selected = place.id == localState.selected?.id)
        }
        return known + listOfNotNull(custom?.let { MapMarker(it, customForecast?.after(hoursAhead), selected = true) })
    }

    private fun focusWeather(place: Place, forecast: Forecast, hoursAhead: Int, profile: ComfortProfile): FocusWeather {
        val snapshot = forecast.after(hoursAhead)
        return FocusWeather(
            place = place,
            isHome = place.isHome,
            snapshot = snapshot,
            personalC = profile.feelsLikeFor(snapshot.apparentTemperatureC),
            activities = evaluateActivities(place, snapshot),
            summary = summarizeDay(forecast, profile),
            camping = evaluateCamping(campsitesNear(place), snapshot)
        )
    }

    /** Odaktaki yere en yakın kamp alanının şehrindeki bütün önerilen alanlar (ör. İzmir'in üç kamp bölgesi). */
    private fun campsitesNear(place: Place): List<Campsite> {
        val nearest = guide.campsites()
            .minByOrNull { it.point.distanceKmTo(place.point) }
            ?.takeIf { it.point.distanceKmTo(place.point) <= CAMP_RADIUS_KM }
        return guide.campsites().filter { it.cityId == nearest?.cityId }
    }

    private fun planItem(plan: Plan, homeForecast: Forecast?, reports: List<CommunityReport>): TodayPlanItem {
        val weatherAtPlan = homeForecast?.at(plan.start)
        val departure = weatherAtPlan
            ?.takeUnless { plan.done }
            ?.let { estimateDeparture(plan, it, reports.countNear(plan.point)) }
        return TodayPlanItem(plan, weatherAtPlan, departure)
    }

    private fun Place.toCard(forecast: Forecast?) = CityCardModel(this, forecast?.current?.kind, forecast?.current?.temperatureC)

    /** ViewModel'in kendi tuttuğu (repository'den gelmeyen) ekran durumu. */
    private data class LocalState(
        val selected: Place? = null,
        val touched: Boolean = false,
        val step: Int = 0,
        val playing: Boolean = true,
        val feedback: ComfortFeedback? = null,
        val customForecast: LoadState<Forecast>? = null,
        val search: SearchUiState = SearchUiState()
    ) {
        fun nextStep(): Int = when (playing) {
            true -> (step + 1) % STEP_HOURS.size
            false -> step
        }
    }

    private data class Context(
        val dataSource: DataSource,
        val profile: ComfortProfile,
        val plans: List<Plan>,
        val reports: List<CommunityReport>,
        val proposal: GroupProposal
    )

    private fun DataSource.other(): DataSource = when (this) {
        DataSource.LIVE -> DataSource.DEMO
        DataSource.DEMO -> DataSource.LIVE
    }

    companion object {
        /** Zaman çubuğunun adımları: şimdi, +3, +6, +9, +12 saat. */
        val STEP_HOURS = listOf(0, 3, 6, 9, 12)
        private const val INTRO_MS = 1700L
        private const val STEP_MS = 1900L
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val MAX_LOCAL_RESULTS = 8
        private const val SHORTCUT_HOURS = 12
        private const val WET_PERCENT = 50
        private const val CAMP_RADIUS_KM = 90.0
        private val SHORTCUT_MODE = TravelMode.MOTORBIKE

        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                TodayViewModel(
                    places = c.placeRepository,
                    weather = c.weatherRepository,
                    maps = c.mapRepository,
                    settings = c.settingsRepository,
                    comfort = c.comfortRepository,
                    plans = c.planRepository,
                    reports = c.reportRepository,
                    groupVotes = c.groupVoteRepository,
                    guide = c.guideRepository,
                    getMapWeather = c.getMapWeather,
                    searchPlaces = c.searchPlaces,
                    evaluateActivities = c.evaluateActivities,
                    summarizeDay = c.summarizeDay,
                    estimateDeparture = c.estimateDeparture,
                    recordFeedback = c.recordComfortFeedback,
                    evaluateCamping = c.evaluateCamping,
                    assessRoadRisk = c.assessRoadRisk,
                    suggestOutfit = c.suggestOutfit
                )
            }
        }
    }
}
