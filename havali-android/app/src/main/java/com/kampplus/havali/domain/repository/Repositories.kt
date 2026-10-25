package com.kampplus.havali.domain.repository

import com.kampplus.havali.domain.model.AirQuality
import com.kampplus.havali.domain.model.Campsite
import com.kampplus.havali.domain.model.ComfortProfile
import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.DataSource
import com.kampplus.havali.domain.model.DepartureReminder
import com.kampplus.havali.domain.model.Forecast
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.Place
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.TrafficCorridor
import com.kampplus.havali.domain.model.VoteChoice
import java.time.LocalDateTime
import kotlinx.coroutines.flow.StateFlow

/*
 * Domain katmanının dış dünyadan beklentileri.
 *
 * Bu dosyada yalnızca ARAYÜZLER vardır. Verinin internetten mi, cihazdan mı yoksa
 * demo senaryosundan mı geldiğini domain bilmez; uygulamaları data katmanındadır.
 * Böylece testlerde sahte (fake) uygulama vermek tek satırdır.
 */

interface WeatherRepository {
    suspend fun getForecast(point: GeoPoint): Forecast

    /** Birden çok nokta için tek istekte tahmin (harita etiketleri ve animasyonları). */
    suspend fun getForecasts(points: List<GeoPoint>): List<Forecast>
}

interface AirQualityRepository {
    suspend fun getAirQuality(point: GeoPoint): AirQuality
}

interface PlaceRepository {
    /** Haritada gösterilen, uygulamanın tanıdığı yerler. */
    fun knownPlaces(): List<Place>

    /** Kullanıcının yaşadığı yer. */
    fun home(): Place

    /** İsme göre çevrimiçi arama (Open-Meteo geocoding). */
    suspend fun searchOnline(query: String): List<Place>
}

interface MapRepository {
    suspend fun loadGeometry(): MapGeometry
}

interface PlanRepository {
    val plans: StateFlow<List<Plan>>

    fun add(plan: Plan)

    fun move(planId: String, newStart: LocalDateTime)
}

interface GroupVoteRepository {
    val proposal: StateFlow<GroupProposal>

    fun vote(choice: VoteChoice): GroupProposal

    fun clearMyVote()
}

interface CommunityReportRepository {
    val reports: StateFlow<List<CommunityReport>>

    fun toggleConfirm(reportId: String)

    fun dismiss(reportId: String)

    fun add(type: ReportType, placeName: String, point: GeoPoint)
}

interface ComfortRepository {
    val profile: StateFlow<ComfortProfile>

    fun save(profile: ComfortProfile)
}

interface SettingsRepository {
    val dataSource: StateFlow<DataSource>

    fun setDataSource(source: DataSource)
}

/** Çıkış hatırlatıcısını sisteme kurar (Android'de AlarmManager + BroadcastReceiver). */
interface ReminderScheduler {
    fun schedule(reminder: DepartureReminder)

    fun cancel(planId: String)
}

/** Şehir rehberi: kamp alanları ve trafik aksları (uygulamayla gelen sabit katalog). */
interface GuideRepository {
    fun campsites(): List<Campsite>

    fun corridors(cityId: String): List<TrafficCorridor>
}
