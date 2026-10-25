package com.kampplus.havali.data.memory

import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.Member
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.TravelMode
import com.kampplus.havali.domain.model.VoteChoice
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Uygulama ilk açıldığında görünen örnek içerik.
 *
 * Tarihler "bugün"e göre hesaplanır; demo hangi gün açılırsa açılsın planlar bu
 * haftaya düşer. Gerçek bir sunucu eklendiğinde yalnızca repository uygulaması
 * değişir, bu nesne testlerde ve önizlemelerde kullanılmaya devam eder.
 */
object DemoSeed {

    const val SELF_ID = "me"
    private const val MEETING_HOUR = 18
    private const val LATEST_SAME_DAY_HOUR = 21

    private val KORDON = GeoPoint(38.437, 27.141)
    private val KONAK = GeoPoint(38.419, 27.128)
    private val BORNOVA = GeoPoint(38.466, 27.216)
    private val BAYRAKLI = GeoPoint(38.462, 27.165)
    private val BUCA = GeoPoint(38.388, 27.175)
    private val KULTURPARK = GeoPoint(38.43, 27.145)
    private val CESME_ILICA = GeoPoint(38.324, 26.302)
    private val KARSIYAKA = GeoPoint(38.459, 27.11)

    fun plans(now: LocalDateTime): List<Plan> = plans(now.toLocalDate(), meetingTime(now))

    private fun plans(today: LocalDate, meeting: LocalDateTime): List<Plan> = listOf(
        plan("run", "Kordon'da koşu", "Kordon", KORDON, today.atTime(9, 30), TravelMode.WALK, 1.2, outdoor = true, done = true),
        plan("dentist", "Diş hekimi", "Konak", KONAK, today.atTime(13, 0), TravelMode.TRANSIT, 4.5, outdoor = false),
        plan("cem", "Cem'le buluşma", "Alsancak Kordon", KORDON, meeting, TravelMode.CAR, 6.1, outdoor = true),
        plan("work", "Motorla işe", "Bornova", BORNOVA, today.plusDays(1).atTime(8, 30), TravelMode.MOTORBIKE, 9.0, outdoor = false),
        plan("pilates", "Pilates", "Bayraklı", BAYRAKLI, today.plusDays(2).atTime(19, 0), TravelMode.TRANSIT, 5.0, outdoor = false),
        plan("football", "Halı saha", "Buca", BUCA, today.plusDays(3).atTime(20, 0), TravelMode.CAR, 7.0, outdoor = true),
        plan(
            "picnic",
            "Kültürpark'ta piknik",
            "Kültürpark",
            KULTURPARK,
            today.plusDays(4).atTime(12, 0),
            TravelMode.WALK,
            3.0,
            outdoor = true,
            flexible = true
        )
    )

    /**
     * Akşam buluşması, demo hangi saatte açılırsa açılsın önümüzde kalsın diye:
     * normalde 18:00; akşamüstü açılırsa iki saat sonrası, gece açılırsa ertesi gün 18:00.
     */
    private fun meetingTime(now: LocalDateTime): LocalDateTime = when {
        now.hour < MEETING_HOUR - 1 -> now.toLocalDate().atTime(MEETING_HOUR, 0)
        now.hour < LATEST_SAME_DAY_HOUR -> now.truncatedTo(ChronoUnit.HOURS).plusHours(2)
        else -> now.toLocalDate().plusDays(1).atTime(MEETING_HOUR, 0)
    }

    /** Okunurluk için kısa kurucu: sabit saatli (esnek olmayan) plan varsayılandır. */
    private fun plan(
        id: String,
        title: String,
        placeName: String,
        point: GeoPoint,
        start: LocalDateTime,
        mode: TravelMode,
        distanceKm: Double,
        outdoor: Boolean,
        flexible: Boolean = false,
        done: Boolean = false
    ) = Plan(id, title, placeName, point, start, outdoor, flexible, mode, distanceKm, done = done)

    fun proposal(today: LocalDate): GroupProposal {
        val beyza = Member("beyza", "Beyza")
        val members = listOf(beyza, Member("ece", "Ece"), Member("mert", "Mert"), Member("can", "Can"), Member(SELF_ID, "Sen"))
        return GroupProposal(
            id = "friday-beach",
            groupName = "Kordon Ekibi",
            proposer = beyza,
            title = "Cuma denize gidelim!",
            placeName = "Çeşme · Ilıca Plajı",
            point = CESME_ILICA,
            start = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).atTime(LocalTime.of(13, 0)),
            members = members,
            votes = mapOf("beyza" to VoteChoice.YES, "ece" to VoteChoice.YES, "mert" to VoteChoice.YES, "can" to VoteChoice.NO),
            selfId = SELF_ID
        )
    }

    fun reports(): List<CommunityReport> = listOf(
        CommunityReport("r1", ReportType.PUDDLE, "Alsancak · Kıbrıs Şehitleri Cd.", KORDON, minutesAgo = 12, confirmations = 9),
        CommunityReport("r2", ReportType.SLIPPERY_ROAD, "Karşıyaka · Girne Bulvarı", KARSIYAKA, minutesAgo = 7, confirmations = 5),
        CommunityReport("r3", ReportType.ROADWORK, "Bornova · Ankara Caddesi", BORNOVA, minutesAgo = 40, confirmations = 4)
    )
}
