package com.kampplus.havali.domain.usecase.plan

import com.kampplus.havali.domain.model.DailyWeather
import com.kampplus.havali.domain.model.DepartureAdvice
import com.kampplus.havali.domain.model.FactorRating
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.PlanFactor
import com.kampplus.havali.domain.model.PlanScore
import com.kampplus.havali.domain.model.PlanSuggestion
import com.kampplus.havali.domain.model.Suitability
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.VoteChoice
import com.kampplus.havali.domain.model.VoteOutcome
import com.kampplus.havali.domain.model.WeatherSnapshot
import com.kampplus.havali.domain.repository.GroupVoteRepository
import com.kampplus.havali.domain.repository.PlanRepository
import kotlin.math.roundToInt

/**
 * Plan uygunluk puanı (0-100).
 *
 * 100'den başlanır; yağış olasılığı, trafikten gelen ek süre ve kişisel konfor
 * aralığının dışında kalan sıcaklık kadar puan düşülür.
 */
class ScorePlanUseCase {

    operator fun invoke(weather: WeatherSnapshot, departure: DepartureAdvice, personalFeelsLikeC: Double): PlanScore {
        val extraMinutes = departure.travelMinutes - departure.usualMinutes
        val outOfComfort = (COMFORT_LOW_C - personalFeelsLikeC).coerceAtLeast(0.0) +
            (personalFeelsLikeC - COMFORT_HIGH_C).coerceAtLeast(0.0)
        val penalty = weather.precipitationProbability * RAIN_WEIGHT +
            extraMinutes * TRAFFIC_WEIGHT +
            outOfComfort * TEMPERATURE_WEIGHT
        return PlanScore(
            value = (MAX_SCORE - penalty).roundToInt().coerceIn(0, MAX_SCORE),
            factors = listOf(
                FactorRating(PlanFactor.RAIN, rate(weather.precipitationProbability.toDouble(), RAIN_CAUTION, RAIN_BAD)),
                FactorRating(PlanFactor.TRAFFIC, rate(extraMinutes.toDouble(), TRAFFIC_CAUTION, TRAFFIC_BAD)),
                FactorRating(PlanFactor.TEMPERATURE, rate(outOfComfort, TEMP_CAUTION, TEMP_BAD))
            )
        )
    }

    private fun rate(value: Double, caution: Double, bad: Double): Suitability = when {
        value >= bad -> Suitability.BAD
        value >= caution -> Suitability.CAUTION
        else -> Suitability.GOOD
    }

    private companion object {
        const val MAX_SCORE = 100
        const val COMFORT_LOW_C = 12.0
        const val COMFORT_HIGH_C = 28.0
        const val RAIN_WEIGHT = 0.35
        const val TRAFFIC_WEIGHT = 0.8
        const val TEMPERATURE_WEIGHT = 0.5
        const val RAIN_CAUTION = 30.0
        const val RAIN_BAD = 60.0
        const val TRAFFIC_CAUTION = 8.0
        const val TRAFFIC_BAD = 25.0
        const val TEMP_CAUTION = 4.0
        const val TEMP_BAD = 10.0
    }
}

/**
 * Hafta planlarını günlük tahminle karşılaştırır.
 *
 * Yağış olasılığı yüksek bir güne denk gelen açık hava planı için:
 * plan esnekse ilk kuru güne taşımayı, değilse kapalı mekânı önerir.
 */
class DetectPlanConflictsUseCase {

    operator fun invoke(plans: List<Plan>, days: List<DailyWeather>): List<PlanSuggestion> = plans
        .filter { it.outdoor && !it.done }
        .mapNotNull { plan ->
            days.firstOrNull { it.date == plan.start.toLocalDate() }
                ?.takeIf { it.precipitationProbability >= RAINY_DAY_PERCENT }
                ?.let { suggestionFor(plan, it, days) }
        }

    private fun suggestionFor(plan: Plan, rainyDay: DailyWeather, days: List<DailyWeather>): PlanSuggestion {
        val dryDay = days.firstOrNull { it.date.isAfter(rainyDay.date) && it.precipitationProbability < DRY_DAY_PERCENT }
        return when {
            plan.flexible && dryDay != null -> PlanSuggestion.MoveToDay(plan, dryDay.date, dryDay.maxTemperatureC.roundToInt())
            else -> PlanSuggestion.PreferIndoor(plan, rainyDay.precipitationProbability)
        }
    }

    private companion object {
        const val RAINY_DAY_PERCENT = 60
        const val DRY_DAY_PERCENT = 30
    }
}

/**
 * Grup oylamasında kullanıcının oyunu kaydeder.
 *
 * Karar "evet" çıkarsa ve kullanıcı da katılıyorsa plan takvime eklenir.
 */
class CastVoteUseCase(
    private val groupVotes: GroupVoteRepository,
    private val plans: PlanRepository
) {

    operator fun invoke(choice: VoteChoice): GroupProposal {
        val updated = groupVotes.vote(choice)
        val joins = updated.outcome == VoteOutcome.ACCEPTED && choice == VoteChoice.YES
        if (joins) plans.add(updated.toPlan())
        return updated
    }

    private fun GroupProposal.toPlan() = Plan(
        id = "group-$id",
        title = title,
        placeName = placeName,
        point = point,
        start = start,
        outdoor = true,
        flexible = false,
        mode = TravelMode.CAR,
        distanceKm = GROUP_TRIP_KM,
        groupName = groupName
    )

    private companion object {
        const val GROUP_TRIP_KM = 85.0
    }
}
