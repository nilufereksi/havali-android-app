package com.kampplus.havali.di

import android.content.Context
import com.google.gson.Gson
import com.kampplus.havali.data.catalog.GuideCatalog
import com.kampplus.havali.data.local.AssetMapRepository
import com.kampplus.havali.data.local.PreferencesComfortRepository
import com.kampplus.havali.data.local.PreferencesSettingsRepository
import com.kampplus.havali.data.memory.DemoSeed
import com.kampplus.havali.data.memory.InMemoryCommunityReportRepository
import com.kampplus.havali.data.memory.InMemoryGroupVoteRepository
import com.kampplus.havali.data.memory.InMemoryPlanRepository
import com.kampplus.havali.data.remote.ApiClient
import com.kampplus.havali.data.repository.CatalogPlaceRepository
import com.kampplus.havali.data.repository.DemoAirQualityRepository
import com.kampplus.havali.data.repository.DemoWeatherRepository
import com.kampplus.havali.data.repository.OpenMeteoAirQualityRepository
import com.kampplus.havali.data.repository.OpenMeteoWeatherRepository
import com.kampplus.havali.data.repository.SwitchableAirQualityRepository
import com.kampplus.havali.data.repository.SwitchableWeatherRepository
import com.kampplus.havali.domain.repository.AirQualityRepository
import com.kampplus.havali.domain.repository.ComfortRepository
import com.kampplus.havali.domain.repository.CommunityReportRepository
import com.kampplus.havali.domain.repository.GroupVoteRepository
import com.kampplus.havali.domain.repository.GuideRepository
import com.kampplus.havali.domain.repository.MapRepository
import com.kampplus.havali.domain.repository.PlaceRepository
import com.kampplus.havali.domain.repository.PlanRepository
import com.kampplus.havali.domain.repository.ReminderScheduler
import com.kampplus.havali.domain.repository.SettingsRepository
import com.kampplus.havali.domain.repository.WeatherRepository
import com.kampplus.havali.domain.usecase.activity.EvaluateActivitiesUseCase
import com.kampplus.havali.domain.usecase.guide.DetectAlertsUseCase
import com.kampplus.havali.domain.usecase.guide.EvaluateCampingUseCase
import com.kampplus.havali.domain.usecase.guide.PredictTrafficUseCase
import com.kampplus.havali.domain.usecase.guide.SuggestAlternativesUseCase
import com.kampplus.havali.domain.usecase.health.BuildCareTipsUseCase
import com.kampplus.havali.domain.usecase.health.RecordComfortFeedbackUseCase
import com.kampplus.havali.domain.usecase.place.GetMapWeatherUseCase
import com.kampplus.havali.domain.usecase.place.SearchPlacesUseCase
import com.kampplus.havali.domain.usecase.plan.CastVoteUseCase
import com.kampplus.havali.domain.usecase.plan.DetectPlanConflictsUseCase
import com.kampplus.havali.domain.usecase.plan.EstimateDepartureUseCase
import com.kampplus.havali.domain.usecase.plan.ScorePlanUseCase
import com.kampplus.havali.domain.usecase.plan.SuggestBetterTimeUseCase
import com.kampplus.havali.domain.usecase.prepare.BuildDailyChecklistUseCase
import com.kampplus.havali.domain.usecase.prepare.BuildPackingListUseCase
import com.kampplus.havali.domain.usecase.prepare.SuggestOutfitUseCase
import com.kampplus.havali.domain.usecase.road.AssessRoadRiskUseCase
import com.kampplus.havali.domain.usecase.summary.SummarizeDayUseCase
import com.kampplus.havali.notification.AlarmReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Elle bağımlılık enjeksiyonu (manual DI).
 *
 * Bütün nesneler burada, uygulama başına BİR kez oluşturulur ve ViewModel'lere
 * yapıcı (constructor) parametresi olarak verilir. Hilt gibi bir kütüphane de aynı işi
 * yapardı; küçük bir projede bu dosya hangi sınıfın neye bağlı olduğunu tek bakışta
 * gösterdiği için tercih edildi. Testlerde aynı sınıflara sahte (fake) nesne verilir.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val apiClient = ApiClient()

    // --- Data katmanı: repository uygulamaları ---
    val settingsRepository: SettingsRepository = PreferencesSettingsRepository(preferences)
    val comfortRepository: ComfortRepository = PreferencesComfortRepository(preferences)

    val weatherRepository: WeatherRepository = SwitchableWeatherRepository(
        settings = settingsRepository,
        live = OpenMeteoWeatherRepository(apiClient.forecastApi, ioDispatcher),
        demo = DemoWeatherRepository()
    )
    val airQualityRepository: AirQualityRepository = SwitchableAirQualityRepository(
        settings = settingsRepository,
        live = OpenMeteoAirQualityRepository(apiClient.airQualityApi, ioDispatcher),
        demo = DemoAirQualityRepository()
    )
    val placeRepository: PlaceRepository = CatalogPlaceRepository(apiClient.geocodingApi, ioDispatcher)
    val mapRepository: MapRepository = AssetMapRepository(appContext.assets, Gson(), ioDispatcher)

    val planRepository: PlanRepository = InMemoryPlanRepository(DemoSeed.plans(LocalDateTime.now()))
    val groupVoteRepository: GroupVoteRepository = InMemoryGroupVoteRepository(DemoSeed.proposal(LocalDate.now()))
    val reportRepository: CommunityReportRepository = InMemoryCommunityReportRepository(DemoSeed.reports())
    val reminderScheduler: ReminderScheduler = AlarmReminderScheduler(appContext)
    val guideRepository: GuideRepository = GuideCatalog

    // --- Domain katmanı: kullanım senaryoları ---
    val estimateDeparture = EstimateDepartureUseCase()
    val scorePlan = ScorePlanUseCase()
    val detectPlanConflicts = DetectPlanConflictsUseCase()
    val suggestBetterTime = SuggestBetterTimeUseCase(estimateDeparture)
    val castVote = CastVoteUseCase(groupVoteRepository, planRepository)
    val evaluateActivities = EvaluateActivitiesUseCase()
    val summarizeDay = SummarizeDayUseCase()
    val searchPlaces = SearchPlacesUseCase(placeRepository)
    val getMapWeather = GetMapWeatherUseCase(placeRepository, weatherRepository)
    val assessRoadRisk = AssessRoadRiskUseCase()
    val recordComfortFeedback = RecordComfortFeedbackUseCase(comfortRepository)
    val buildCareTips = BuildCareTipsUseCase()
    val buildDailyChecklist = BuildDailyChecklistUseCase()
    val suggestOutfit = SuggestOutfitUseCase()
    val buildPackingList = BuildPackingListUseCase()
    val evaluateCamping = EvaluateCampingUseCase()
    val detectAlerts = DetectAlertsUseCase()
    val predictTraffic = PredictTrafficUseCase()
    val suggestAlternatives = SuggestAlternativesUseCase()

    private companion object {
        const val PREFERENCES_NAME = "havali_prefs"
    }
}
