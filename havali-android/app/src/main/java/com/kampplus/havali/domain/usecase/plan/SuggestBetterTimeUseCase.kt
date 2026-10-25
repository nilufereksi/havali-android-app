package com.kampplus.havali.domain.usecase.plan

import com.kampplus.havali.domain.model.BetterTime
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.Plan

/**
 * Plan saati esnetilebilirse bir-iki saat sonrasını dener; daha kuru ve daha kısa
 * yolculuklu bir saat varsa önerir. Yoksa null döner ve kart hiç gösterilmez.
 */
class SuggestBetterTimeUseCase(private val estimateDeparture: EstimateDepartureUseCase) {

    operator fun invoke(plan: Plan, forecast: Forecast, reportsOnRoute: Int): BetterTime? {
        val planned = estimateDeparture(plan, forecast.at(plan.start), reportsOnRoute)
        return CANDIDATE_HOURS
            .map { plan.start.plusHours(it) }
            .map { start ->
                val weather = forecast.at(start)
                val advice = estimateDeparture(plan.copy(start = start), weather, reportsOnRoute)
                BetterTime(start, weather.precipitationProbability, advice.travelMinutes)
            }
            .filter { it.precipitationProbability <= DRY_PERCENT && it.travelMinutes < planned.travelMinutes }
            .minByOrNull { it.travelMinutes }
    }

    private companion object {
        val CANDIDATE_HOURS = listOf(1L, 2L)
        const val DRY_PERCENT = 30
    }
}
