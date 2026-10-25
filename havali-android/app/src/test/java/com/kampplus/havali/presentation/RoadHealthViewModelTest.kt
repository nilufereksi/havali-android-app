package com.kampplus.havali.presentation

import com.kampplus.havali.data.catalog.GuideCatalog
import com.kampplus.havali.data.memory.InMemoryCommunityReportRepository
import com.kampplus.havali.domain.model.AlertType
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.MapZoom
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.RiskLevel
import com.kampplus.havali.domain.model.TrafficLevel
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.WeatherKind
import com.kampplus.havali.domain.usecase.guide.DetectAlertsUseCase
import com.kampplus.havali.domain.usecase.guide.PredictTrafficUseCase
import com.kampplus.havali.domain.usecase.guide.SuggestAlternativesUseCase
import com.kampplus.havali.domain.usecase.health.BuildCareTipsUseCase
import com.kampplus.havali.domain.usecase.road.AssessRoadRiskUseCase
import com.kampplus.havali.presentation.common.LoadState
import com.kampplus.havali.presentation.common.UiError
import com.kampplus.havali.presentation.road.RoadHealthEvent
import com.kampplus.havali.presentation.road.RoadHealthViewModel
import com.kampplus.havali.testing.FakeAirQualityRepository
import com.kampplus.havali.testing.FakeComfortRepository
import com.kampplus.havali.testing.FakeMapRepository
import com.kampplus.havali.testing.FakePlaceRepository
import com.kampplus.havali.testing.FakeSettingsRepository
import com.kampplus.havali.testing.FakeWeatherRepository
import com.kampplus.havali.testing.MainDispatcherRule
import com.kampplus.havali.testing.TestWeather
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoadHealthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val home = Place("izmir", "İzmir", "Ege", GeoPoint(38.42, 27.13), MapZoom.REGION, isHome = true)
    private val rainyHours = (0 until 12).map {
        TestWeather.snapshot(time = TestWeather.NOW.plusHours(it.toLong()), rain = 80, gustKmh = 40.0, kind = WeatherKind.RAIN)
    }
    private val reports = InMemoryCommunityReportRepository(emptyList())

    private fun viewModel(weather: FakeWeatherRepository) = RoadHealthViewModel(
        places = FakePlaceRepository(home),
        weather = weather,
        airQuality = FakeAirQualityRepository(),
        reports = reports,
        comfort = FakeComfortRepository(),
        maps = FakeMapRepository(),
        guide = GuideCatalog,
        settings = FakeSettingsRepository(),
        assessRoadRisk = AssessRoadRiskUseCase(),
        buildCareTips = BuildCareTipsUseCase(),
        detectAlerts = DetectAlertsUseCase(),
        predictTraffic = PredictTrafficUseCase(),
        suggestAlternatives = SuggestAlternativesUseCase(),
        autoPlay = false,
        clock = { TestWeather.NOW }
    )

    @Test
    fun `tahmin gelince secili ulasim icin yol riski hesaplanir`() = runTest {
        val vm = viewModel(FakeWeatherRepository(TestWeather.forecast(rainyHours)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val road = vm.uiState.value.road as LoadState.Success
        assertEquals(TravelMode.MOTORBIKE, road.data.mode)
        assertEquals(RiskLevel.VERY_HIGH, road.data.risk.level)

        vm.onEvent(RoadHealthEvent.SelectMode(TravelMode.TRANSIT))
        val transit = vm.uiState.value.road as LoadState.Success
        assertTrue(transit.data.risk.level.ordinal < RiskLevel.VERY_HIGH.ordinal)
    }

    @Test
    fun `ucak modunda baglanti hatasi gosterilir, tekrar dene veriyi getirir`() = runTest {
        val weather = FakeWeatherRepository(TestWeather.forecast(rainyHours), failWith = IOException("uçak modu"))
        val vm = viewModel(weather)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(UiError.NO_CONNECTION, (vm.uiState.value.road as LoadState.Error).error)

        weather.failWith = null
        vm.onEvent(RoadHealthEvent.Retry)

        assertTrue(vm.uiState.value.road is LoadState.Success)
        assertEquals(2, weather.calls)
    }

    @Test
    fun `rapor eklemek listeye kullanicinin raporunu ekler ve tesekkur gosterir`() = runTest {
        val vm = viewModel(FakeWeatherRepository(TestWeather.forecast(rainyHours)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onEvent(RoadHealthEvent.OpenReportForm)
        vm.onEvent(RoadHealthEvent.SendReport(ReportType.PUDDLE))

        val state = vm.uiState.value
        assertTrue(state.reportSent)
        assertTrue(state.reports.first().mine)
        assertEquals(ReportType.PUDDLE, state.reports.first().type)
    }

    @Test
    fun `firtinali yagmurda uyari cikar ve izmir akslarinin trafigi tahmin edilir`() = runTest {
        val vm = viewModel(FakeWeatherRepository(TestWeather.forecast(rainyHours)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val road = (vm.uiState.value.road as LoadState.Success).data
        assertEquals(AlertType.HEAVY_RAIN, road.alerts.last().type)
        assertTrue(road.traffic.corridors.isNotEmpty())
        assertEquals(TrafficLevel.MODERATE, road.traffic.corridors.first { it.corridor.id == "altinyol" }.level)
        assertTrue(road.traffic.alternatives.any { it.alternative.corridor.id == "anadolu-cd" && it.savedMinutes > 0 })

        vm.onEvent(RoadHealthEvent.SelectCorridor("altinyol"))
        assertEquals("Altınyol", (vm.uiState.value.road as LoadState.Success).data.traffic.selected?.corridor?.name)
    }
}
