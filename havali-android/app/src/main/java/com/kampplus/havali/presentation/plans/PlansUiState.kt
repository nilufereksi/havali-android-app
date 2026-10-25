package com.kampplus.havali.presentation.plans

import com.kampplus.havali.domain.model.BetterTime
import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.DepartureAdvice
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.PlanScore
import com.kampplus.havali.domain.model.PlanSuggestion
import com.kampplus.havali.domain.model.VoteChoice
import com.kampplus.havali.domain.model.VoteOutcome
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.presentation.common.UiError
import java.time.LocalDate

data class PlansUiState(
    val loading: Boolean = true,
    val error: UiError? = null,
    val days: List<DayCell> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dayPlans: List<Plan> = emptyList(),
    val selectedPlanId: String? = null,
    val proposal: ProposalUi? = null,
    val detail: PlanDetail? = null,
    val suggestions: List<PlanSuggestion> = emptyList()
)

/** Hafta şeridindeki bir gün. */
data class DayCell(val date: LocalDate, val weather: DailyWeather?, val planCount: Int)

/** Grup oylaması kartı: öneri + önerilen günün havası. */
data class ProposalUi(val proposal: GroupProposal, val day: DailyWeather?)

/** Seçili planın ayrıntısı: puan, akıllı çıkış önerisi, hatırlatıcı ve varsa daha rahat saat. */
data class PlanDetail(
    val plan: Plan,
    val weather: WeatherSnapshot,
    val departure: DepartureAdvice,
    val score: PlanScore,
    val betterTime: BetterTime?,
    val reminderOn: Boolean
)

sealed interface PlansEvent {
    data class Vote(val choice: VoteChoice) : PlansEvent
    data object ChangeVote : PlansEvent
    data class SelectDate(val date: LocalDate) : PlansEvent
    data class SelectPlan(val planId: String) : PlansEvent
    data class SetReminder(val enabled: Boolean) : PlansEvent
    data object AcceptBetterTime : PlansEvent
    data class ApplySuggestion(val suggestion: PlanSuggestion) : PlansEvent
    data class DismissSuggestion(val planId: String) : PlansEvent
    data object Retry : PlansEvent
}

/** Kullanıcı oy verdikten sonra gösterilecek sonuç. */
enum class VoteResult { JOINED, SKIPPED, CANCELLED, WAITING }

val ProposalUi.result: VoteResult?
    get() = proposal.myVote?.let { mine ->
        when {
            proposal.outcome == VoteOutcome.PENDING -> VoteResult.WAITING
            proposal.outcome == VoteOutcome.DECLINED -> VoteResult.CANCELLED
            mine == VoteChoice.YES -> VoteResult.JOINED
            else -> VoteResult.SKIPPED
        }
    }
