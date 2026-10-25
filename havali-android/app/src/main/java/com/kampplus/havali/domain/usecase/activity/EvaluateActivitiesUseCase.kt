package com.kampplus.havali.domain.usecase.activity

import com.kampplus.havali.domain.model.ActivityAssessment
import com.kampplus.havali.domain.model.ActivityPlan
import com.kampplus.havali.domain.model.ActivityType
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.WeatherSnapshot

/**
 * "Bugün ne yapsam?" sorusunun cevabı.
 *
 * 1. Yerin etiketlerine uymayan aktiviteler elenir (denizi olmayan yerde SUP önerilmez).
 * 2. Kalanlar havaya göre değerlendirilir.
 * 3. En uygun olan öne çıkar; eşitlikte [ActivityType] sırası korunur (sortedBy kararlıdır).
 */
class EvaluateActivitiesUseCase {

    operator fun invoke(place: Place, weather: WeatherSnapshot): ActivityPlan {
        val assessments = ActivityType.entries
            .filter { type -> place.tags.containsAll(ActivityRules.requiredTags(type)) }
            .map { type -> ActivityAssessment(type, ActivityRules.evaluate(type, weather)) }
            .sortedBy { it.verdict.suitability.severity }
        return ActivityPlan(best = assessments.first(), others = assessments.drop(1))
    }
}
