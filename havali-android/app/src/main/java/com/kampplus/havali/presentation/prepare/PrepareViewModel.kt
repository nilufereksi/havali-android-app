package com.kampplus.havali.presentation.prepare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kampplus.havali.domain.model.AlertType
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.ItemKey
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.PlaceTag
import com.kampplus.havali.domain.model.SummaryFact
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherAlert
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.SettingsRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import com.kampplus.havali.domain.usecase.guide.DetectAlertsUseCase
import com.kampplus.havali.domain.usecase.prepare.BuildDailyChecklistUseCase
import com.kampplus.havali.domain.usecase.prepare.BuildPackingListUseCase
import com.kampplus.havali.domain.usecase.prepare.SuggestOutfitUseCase
import com.kampplus.havali.domain.usecase.summary.SummarizeDayUseCase
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.appContainer
import com.kampplus.havali.presentation.common.loadState
import com.kampplus.havali.presentation.common.map
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Hazırlık ekranı: günlük çıkış listesi ve şehirler arası valiz.
 * Valiz için iki şehrin 14 günlük tahmini aynı anda (paralel) istenir.
 */
class PrepareViewModel(
    private val places: PlaceRepository,
    private val weather: WeatherRepository,
    settings: SettingsRepository,
    comfort: ComfortRepository,
    private val buildDailyChecklist: BuildDailyChecklistUseCase,
    private val suggestOutfit: SuggestOutfitUseCase,
    private val buildPackingList: BuildPackingListUseCase,
    private val summarizeDay: SummarizeDayUseCase,
    private val detectAlerts: DetectAlertsUseCase,
    today: LocalDate = LocalDate.now()
) : ViewModel() {

    private val choices = places.knownPlaces().filter { it.zoom == MapZoom.CITY || it.zoom == MapZoom.REGION }
    private val local = MutableStateFlow(
        LocalState(
            origin = places.home(),
            destination = choices.firstOrNull { it.id == DEFAULT_DESTINATION_ID } ?: places.home(),
            tripStart = today.plusDays(TRIP_STARTS_IN_DAYS)
        )
    )

    /** Dalga uyarısı şehir adıyla değil, evin yakınındaki kıyı semtiyle verilir ("Alsancak kıyısında"). */
    private val coastSpot: Place? = places.knownPlaces().firstOrNull {
        it.zoom == MapZoom.STREET && PlaceTag.COASTAL in it.tags && it.point.distanceKmTo(places.home().point) <= COAST_RADIUS_KM
    }
    private val homeForecast = MutableStateFlow<LoadState<Forecast>>(LoadState.Loading)
    private val tripForecasts = MutableStateFlow<LoadState<TripForecasts>>(LoadState.Loading)

    val uiState: StateFlow<PrepareUiState> = combine(local, homeForecast, tripForecasts, comfort.profile) {
            localState,
            home,
            trip,
            profile
        ->
        PrepareUiState(
            tab = localState.tab,
            profile = profile,
            daily = home.map { dailyUi(it, localState, profile) },
            trip = tripUi(localState, trip, profile)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PrepareUiState())

    init {
        loadHome()
        viewModelScope.launch {
            combine(local.map { it.origin to it.destination }.distinctUntilChanged(), settings.dataSource) { route, _: DataSource -> route }
                .collectLatest { (origin, destination) -> loadTrip(origin, destination) }
        }
        viewModelScope.launch { settings.dataSource.drop(1).collect { loadHome() } }
    }

    fun onEvent(event: PrepareEvent) {
        when (event) {
            is PrepareEvent.SelectTab -> local.update { it.copy(tab = event.tab) }
            is PrepareEvent.SelectDailyMode -> local.update { it.copy(dailyMode = event.mode, checkedDaily = emptySet()) }
            is PrepareEvent.ToggleDaily -> local.update { it.copy(checkedDaily = it.checkedDaily.toggle(event.key)) }
            is PrepareEvent.SelectOrigin -> local.update { it.copy(origin = event.place) }
            is PrepareEvent.SelectDestination -> local.update { it.copy(destination = event.place, checkedTrip = emptySet()) }
            is PrepareEvent.ChangeNights -> local.update { it.copy(nights = (it.nights + event.delta).coerceIn(MIN_NIGHTS, MAX_NIGHTS)) }
            is PrepareEvent.SelectTripMode -> local.update { it.copy(tripMode = event.mode) }
            is PrepareEvent.ToggleTrip -> local.update { it.copy(checkedTrip = it.checkedTrip.toggle(event.key)) }
            PrepareEvent.Retry -> retry()
        }
    }

    private fun loadHome() {
        viewModelScope.launch {
            homeForecast.value = LoadState.Loading
            homeForecast.value = loadState { weather.getForecast(places.home().point) }
        }
    }

    /** Çıkış ve varış tahminleri tek çoklu istekle gelir; çıkış seçilmediyse yalnızca varış sorulur. */
    private suspend fun loadTrip(origin: Place?, destination: Place) {
        tripForecasts.value = LoadState.Loading
        tripForecasts.value = loadState {
            val points = listOfNotNull(destination.point, origin?.point)
            val forecasts = weather.getForecasts(points)
            TripForecasts(destination = forecasts.first(), origin = forecasts.getOrNull(1))
        }
    }

    private fun retry() {
        loadHome()
        val state = local.value
        viewModelScope.launch { loadTrip(state.origin, state.destination) }
    }

    private fun dailyUi(forecast: Forecast, state: LocalState, profile: ComfortProfile): DailyPrepUi {
        val hours = forecast.nextHours(DAY_HOURS).ifEmpty { listOf(forecast.current) }
        val minPersonal = hours.minOf { profile.feelsLikeFor(it.apparentTemperatureC) }
        val wet = hours.any { it.kind.isWet || it.precipitationProbability >= WET_PERCENT }
        return DailyPrepUi(
            mode = state.dailyMode,
            nowC = forecast.current.temperatureC,
            laterC = hours.last().temperatureC,
            minPersonalC = minPersonal,
            outfit = suggestOutfit(minPersonal, wet),
            sections = buildDailyChecklist(state.dailyMode, hours, profile),
            checked = state.checkedDaily,
            rain = summarizeDay(forecast, profile).firstOrNull { it is SummaryFact.RainNow || it is SummaryFact.RainArrives },
            kind = hours.firstOrNull { it.kind.isWet }?.kind ?: forecast.current.kind,
            alerts = alerts(hours)
        )
    }

    private fun alerts(hours: List<WeatherSnapshot>): List<WeatherAlert> {
        val cityAlerts = detectAlerts(places.home(), hours).filterNot { it.type == AlertType.WAVES }
        val coastAlerts = coastSpot?.let { spot -> detectAlerts(spot, hours).filter { it.type == AlertType.WAVES } }.orEmpty()
        return (cityAlerts + coastAlerts).sortedBy { it.type.ordinal }
    }

    private fun tripUi(state: LocalState, forecasts: LoadState<TripForecasts>, profile: ComfortProfile) = TripUi(
        choices = choices,
        origin = state.origin,
        destination = state.destination,
        start = state.tripStart,
        nights = state.nights,
        mode = state.tripMode,
        result = forecasts.map { trip ->
            val originDays = trip.origin?.daily?.within(state).orEmpty()
            trip.destination.daily.within(state).takeIf { it.isNotEmpty() }?.let { destinationDays ->
                TripResultUi(
                    originDays = originDays,
                    destinationDays = destinationDays,
                    packing = buildPackingList(originDays, destinationDays, state.tripMode, profile),
                    checked = state.checkedTrip
                )
            }
        }
    )

    /** Yolculuk günleri: başlangıç dahil, gece sayısı kadar sonrası. */
    private fun List<DailyWeather>.within(state: LocalState): List<DailyWeather> {
        val last = state.tripStart.plusDays(state.nights.toLong())
        return filter { !it.date.isBefore(state.tripStart) && !it.date.isAfter(last) }
    }

    private fun Set<ItemKey>.toggle(key: ItemKey): Set<ItemKey> = when (key in this) {
        true -> this - key
        false -> this + key
    }

    private data class TripForecasts(val destination: Forecast, val origin: Forecast?)

    private data class LocalState(
        val tab: PrepareTab = PrepareTab.TODAY,
        val dailyMode: TravelMode = TravelMode.MOTORBIKE,
        val checkedDaily: Set<ItemKey> = emptySet(),
        val origin: Place?,
        val destination: Place,
        val tripStart: LocalDate,
        val nights: Int = DEFAULT_NIGHTS,
        val tripMode: TravelMode = TravelMode.PLANE,
        val checkedTrip: Set<ItemKey> = emptySet()
    )

    companion object {
        private const val DEFAULT_DESTINATION_ID = "istanbul"
        private const val TRIP_STARTS_IN_DAYS = 7L
        private const val DEFAULT_NIGHTS = 3
        private const val MIN_NIGHTS = 1
        private const val MAX_NIGHTS = 6
        private const val DAY_HOURS = 15
        private const val WET_PERCENT = 50
        private const val COAST_RADIUS_KM = 15.0
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                PrepareViewModel(
                    places = c.placeRepository,
                    weather = c.weatherRepository,
                    settings = c.settingsRepository,
                    comfort = c.comfortRepository,
                    buildDailyChecklist = c.buildDailyChecklist,
                    suggestOutfit = c.suggestOutfit,
                    buildPackingList = c.buildPackingList,
                    summarizeDay = c.summarizeDay,
                    detectAlerts = c.detectAlerts
                )
            }
        }
    }
}
