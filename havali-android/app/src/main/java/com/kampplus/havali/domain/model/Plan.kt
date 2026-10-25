package com.kampplus.havali.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Takvimdeki bir plan (anımsatıcı).
 *
 * @param outdoor Açık havada mı; hava çakışması yalnızca açık hava planlarında aranır.
 * @param flexible Başka güne taşınabilir mi. Piknik esnektir, sabit saatli halı saha değildir.
 * @param distanceKm Evden plan yerine mesafe; çıkış saati tahmini için.
 * @param groupName Grup oylamasıyla oluştuysa grubun adı.
 */
data class Plan(
    val id: String,
    val title: String,
    val placeName: String,
    val point: GeoPoint,
    val start: LocalDateTime,
    val outdoor: Boolean,
    val flexible: Boolean,
    val mode: TravelMode,
    val distanceKm: Double,
    val groupName: String? = null,
    val done: Boolean = false
)

/** Havaya göre bir plan için yapılan öneri. */
sealed interface PlanSuggestion {
    val plan: Plan

    /** Plan esnek ve ileride kuru bir gün var: oraya taşınabilir. */
    data class MoveToDay(override val plan: Plan, val date: LocalDate, val maxTemperatureC: Int) : PlanSuggestion

    /** Taşınamıyorsa kapalı mekân alternatifi önerilir. */
    data class PreferIndoor(override val plan: Plan, val precipitationProbability: Int) : PlanSuggestion
}

/** Plan uygunluk puanının bileşenleri. */
enum class PlanFactor { RAIN, TRAFFIC, TEMPERATURE }

data class FactorRating(val factor: PlanFactor, val rating: Suitability)

/** 0-100 arası "plan uygunluğu" ve onu oluşturan etkenler. */
data class PlanScore(val value: Int, val factors: List<FactorRating>)

data class Member(val id: String, val name: String) {
    val initial: String get() = name.take(1).uppercase()
}

enum class VoteChoice { YES, NO }

enum class VoteOutcome { PENDING, ACCEPTED, DECLINED }

/**
 * Bir arkadaşın gruba açtığı plan önerisi ("Cuma denize gidelim!").
 *
 * Herkes oy verince karar kesinleşir; salt çoğunluk "evet" derse plan oluşur.
 */
data class GroupProposal(
    val id: String,
    val groupName: String,
    val proposer: Member,
    val title: String,
    val placeName: String,
    val point: GeoPoint,
    val start: LocalDateTime,
    val members: List<Member>,
    val votes: Map<String, VoteChoice>,
    val selfId: String
) {
    val yesCount: Int get() = votes.values.count { it == VoteChoice.YES }
    val noCount: Int get() = votes.values.count { it == VoteChoice.NO }
    val myVote: VoteChoice? get() = votes[selfId]

    val outcome: VoteOutcome
        get() = when {
            votes.size < members.size -> VoteOutcome.PENDING
            yesCount * 2 > members.size -> VoteOutcome.ACCEPTED
            else -> VoteOutcome.DECLINED
        }
}

/**
 * Çıkış hatırlatıcısı: bildirim [remindAt] anında düşer.
 * Metin domain'de değil, bildirim katmanında kaynak dosyasından oluşturulur.
 */
data class DepartureReminder(
    val planId: String,
    val planTitle: String,
    val placeName: String,
    val remindAt: LocalDateTime,
    val leaveAt: LocalTime
)

/** Aynı plan için daha rahat bir saat önerisi ("20:00 daha rahat: yağış %20, yol 24 dk"). */
data class BetterTime(val start: LocalDateTime, val precipitationProbability: Int, val travelMinutes: Int)
