package com.kampplus.havali.presentation.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.DepartureReminder
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.PlanSuggestion
import com.kampplus.havali.domain.model.countNear
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.CommunityReportRepository
import com.kampplus.havali.domain.repository.GroupVoteRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.PlanRepository
import com.kampplus.havali.domain.repository.ReminderScheduler
import com.kampplus.havali.domain.repository.WeatherRepository
import com.kampplus.havali.domain.usecase.plan.CastVoteUseCase
import com.kampplus.havali.domain.usecase.plan.DetectPlanConflictsUseCase
import com.kampplus.havali.domain.usecase.plan.EstimateDepartureUseCase
import com.kampplus.havali.domain.usecase.plan.ScorePlanUseCase
import com.kampplus.havali.domain.usecase.plan.SuggestBetterTimeUseCase
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.appContainer
import com.kampplus.havali.presentation.common.dataOrNull
import com.kampplus.havali.presentation.common.loadState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Planlar ekranı: grup oylaması, hafta şeridi, plan ayrıntısı ve havaya göre öneriler.
 *
 * Planların, oylamadaki önerinin ve evin konumları tek bir çoklu istekle sorulur.
 * Plan listesi değişip yeni bir konum eklenirse (ör. oylamayla Çeşme planı) tahmin yeniden istenir.
 */
class PlansViewModel(
    private val places: PlaceRepository,
    private val weather: WeatherRepository,
    private val plans: PlanRepository,
    private val groupVotes: GroupVoteRepository,
    private val reminders: ReminderScheduler,
    reports: CommunityReportRepository,
    comfort: ComfortRepository,
    private val castVote: CastVoteUseCase,
    private val estimateDeparture: EstimateDepartureUseCase,
    private val scorePlan: ScorePlanUseCase,
    private val detectConflicts: DetectPlanConflictsUseCase,
    private val suggestBetterTime: SuggestBetterTimeUseCase,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val local = MutableStateFlow(LocalState(selectedDate = today()))
    private val forecasts = MutableStateFlow<LoadState<Map<GeoPoint, Forecast>>>(LoadState.Loading)
    private var lastPoints: List<GeoPoint> = emptyList()

    val uiState: StateFlow<PlansUiState> = combine(
        combine(local, forecasts, ::Pair),
        combine(plans.plans, groupVotes.proposal, reports.reports, comfort.profile, ::Context)
    ) { (localState, forecastState), context -> buildState(localState, forecastState, context) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlansUiState())

    init {
        viewModelScope.launch {
            combine(plans.plans, groupVotes.proposal) { planList, proposal -> pointsOf(planList, proposal) }
                .distinctUntilChanged()
                .collectLatest(::loadForecasts)
        }
    }

    fun onEvent(event: PlansEvent) {
        when (event) {
            is PlansEvent.Vote -> castVote(event.choice)
            PlansEvent.ChangeVote -> groupVotes.clearMyVote()
            is PlansEvent.SelectDate -> local.update { it.copy(selectedDate = event.date, selectedPlanId = null) }
            is PlansEvent.SelectPlan -> local.update { it.copy(selectedPlanId = event.planId) }
            is PlansEvent.SetReminder -> setReminder(event.enabled)
            PlansEvent.AcceptBetterTime -> acceptBetterTime()
            is PlansEvent.ApplySuggestion -> applySuggestion(event.suggestion)
            is PlansEvent.DismissSuggestion -> local.update { it.copy(dismissed = it.dismissed + event.planId) }
            PlansEvent.Retry -> viewModelScope.launch { loadForecasts(lastPoints) }
        }
    }

    private suspend fun loadForecasts(points: List<GeoPoint>) {
        lastPoints = points
        forecasts.value = LoadState.Loading
        forecasts.value = loadState { points.zip(weather.getForecasts(points)).toMap() }
    }

    private fun pointsOf(planList: List<Plan>, proposal: GroupProposal): List<GeoPoint> =
        (listOf(places.home().point, proposal.point) + planList.map { it.point }).distinct()

    private fun setReminder(enabled: Boolean) {
        val detail = uiState.value.detail ?: return
        when (enabled) {
            true -> reminders.schedule(detail.toReminder())
            false -> reminders.cancel(detail.plan.id)
        }
        local.update { state ->
            val ids = when (enabled) {
                true -> state.reminders + detail.plan.id
                false -> state.reminders - detail.plan.id
            }
            state.copy(reminders = ids)
        }
    }

    private fun acceptBetterTime() {
        uiState.value.detail?.let { detail ->
            detail.betterTime?.let { plans.move(detail.plan.id, it.start) }
        }
    }

    private fun applySuggestion(suggestion: PlanSuggestion) {
        when (suggestion) {
            is PlanSuggestion.MoveToDay -> plans.move(suggestion.plan.id, suggestion.date.atTime(suggestion.plan.start.toLocalTime()))
            is PlanSuggestion.PreferIndoor -> Unit
        }
        local.update { it.copy(dismissed = it.dismissed + suggestion.plan.id) }
    }

    private fun buildState(localState: LocalState, forecastState: LoadState<Map<GeoPoint, Forecast>>, context: Context): PlansUiState {
        val forecastMap = forecastState.dataOrNull().orEmpty()
        val homeForecast = forecastMap[places.home().point]
        val days = (0 until WEEK_DAYS).map { today().plusDays(it.toLong()) }
        val dayPlans = context.plans.filter { it.start.toLocalDate() == localState.selectedDate }
        val selectedPlan = dayPlans.firstOrNull { it.id == localState.selectedPlanId } ?: dayPlans.defaultSelection()
        return PlansUiState(
            loading = forecastState is LoadState.Loading,
            error = (forecastState as? LoadState.Error)?.error,
            days = days.map { date -> DayCell(date, homeForecast?.day(date), context.plans.count { it.start.toLocalDate() == date }) },
            selectedDate = localState.selectedDate,
            dayPlans = dayPlans,
            selectedPlanId = selectedPlan?.id,
            proposal = ProposalUi(context.proposal, forecastMap[context.proposal.point]?.day(context.proposal.start.toLocalDate())),
            detail = selectedPlan?.let { plan -> forecastMap[plan.point]?.let { detailOf(plan, it, context, localState) } },
            suggestions = homeForecast
                ?.let { detectConflicts(context.plans, it.daily) }
                .orEmpty()
                .filterNot { it.plan.id in localState.dismissed }
        )
    }

    /** Ayrıntı yalnızca saatlik tahminin kapsadığı planlar için hesaplanır (uzak günler için tahmin tahmine dönüşür). */
    private fun detailOf(plan: Plan, forecast: Forecast, context: Context, localState: LocalState): PlanDetail? =
        forecast.takeIf { !plan.done && it.covers(plan.start) }?.let {
            val weatherAtPlan = it.at(plan.start)
            val reportsOnRoute = context.reports.countNear(plan.point)
            val departure = estimateDeparture(plan, weatherAtPlan, reportsOnRoute)
            PlanDetail(
                plan = plan,
                weather = weatherAtPlan,
                departure = departure,
                score = scorePlan(weatherAtPlan, departure, context.profile.feelsLikeFor(weatherAtPlan.apparentTemperatureC)),
                betterTime = suggestBetterTime(plan, it, reportsOnRoute).takeIf { plan.flexibleTime() },
                reminderOn = plan.id in localState.reminders
            )
        }

    /** Arkadaş buluşması gibi grup dışı planların saati esnetilebilir; grup kararıyla oluşan plan esnetilmez. */
    private fun Plan.flexibleTime(): Boolean = groupName == null && outdoor

    private fun List<Plan>.defaultSelection(): Plan? = firstOrNull { !it.done && it.outdoor } ?: firstOrNull { !it.done }

    private fun PlanDetail.toReminder() = DepartureReminder(
        planId = plan.id,
        planTitle = plan.title,
        placeName = plan.placeName,
        remindAt = plan.start.toLocalDate().atTime(departure.leaveAt).minusMinutes(REMIND_BEFORE_MINUTES),
        leaveAt = departure.leaveAt
    )

    private data class LocalState(
        val selectedDate: LocalDate,
        val selectedPlanId: String? = null,
        val reminders: Set<String> = emptySet(),
        val dismissed: Set<String> = emptySet()
    )

    private data class Context(
        val plans: List<Plan>,
        val proposal: GroupProposal,
        val reports: List<CommunityReport>,
        val profile: ComfortProfile
    )

    companion object {
        /** Çıkıştan kaç dakika önce hatırlatılacağı. */
        const val REMIND_BEFORE_MINUTES = 15L
        private const val WEEK_DAYS = 7
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                PlansViewModel(
                    places = c.placeRepository,
                    weather = c.weatherRepository,
                    plans = c.planRepository,
                    groupVotes = c.groupVoteRepository,
                    reminders = c.reminderScheduler,
                    reports = c.reportRepository,
                    comfort = c.comfortRepository,
                    castVote = c.castVote,
                    estimateDeparture = c.estimateDeparture,
                    scorePlan = c.scorePlan,
                    detectConflicts = c.detectPlanConflicts,
                    suggestBetterTime = c.suggestBetterTime
                )
            }
        }
    }
}
