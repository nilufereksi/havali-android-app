package com.kampplus.havali.data.memory

import com.kampplus.havali.domain.model.CommunityReport
import com.kampplus.havali.domain.model.GeoPoint
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.Plan
import com.kampplus.havali.domain.model.ReportType
import com.kampplus.havali.domain.model.VoteChoice
import com.kampplus.havali.domain.repository.CommunityReportRepository
import com.kampplus.havali.domain.repository.GroupVoteRepository
import com.kampplus.havali.domain.repository.PlanRepository
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/*
 * Bellekte tutulan repository'ler.
 *
 * Planlar, grup oyları ve topluluk raporları gerçek bir üründe sunucuda olurdu.
 * Hackathon kapsamında arayüz aynı kalsın diye StateFlow ile bellekte tutulur:
 * `update {}` atomiktir, değişiklik anında bütün ekranlara yansır (tek doğruluk kaynağı).
 */

class InMemoryPlanRepository(seed: List<Plan>) : PlanRepository {

    private val state = MutableStateFlow(seed.sortedBy { it.start })
    override val plans: StateFlow<List<Plan>> = state.asStateFlow()

    /** Aynı kimlikli plan zaten varsa (ör. oy iki kez verildi) yenisiyle değiştirilir. */
    override fun add(plan: Plan) = state.update { current ->
        (current.filterNot { it.id == plan.id } + plan).sortedBy { it.start }
    }

    override fun move(planId: String, newStart: LocalDateTime) = state.update { current ->
        current.map { plan -> plan.takeUnless { it.id == planId } ?: plan.copy(start = newStart) }.sortedBy { it.start }
    }
}

class InMemoryGroupVoteRepository(seed: GroupProposal) : GroupVoteRepository {

    private val state = MutableStateFlow(seed)
    override val proposal: StateFlow<GroupProposal> = state.asStateFlow()

    override fun vote(choice: VoteChoice): GroupProposal = state.updateAndGet { it.copy(votes = it.votes + (it.selfId to choice)) }

    override fun clearMyVote() = state.update { it.copy(votes = it.votes - it.selfId) }
}

class InMemoryCommunityReportRepository(seed: List<CommunityReport>) : CommunityReportRepository {

    private val state = MutableStateFlow(seed)
    private val nextId = AtomicInteger(seed.size + 1)
    override val reports: StateFlow<List<CommunityReport>> = state.asStateFlow()

    override fun toggleConfirm(reportId: String) = state.update { current ->
        current.map { report -> report.takeUnless { it.id == reportId } ?: report.toggled() }
    }

    override fun dismiss(reportId: String) = state.update { current -> current.filterNot { it.id == reportId } }

    override fun add(type: ReportType, placeName: String, point: GeoPoint) = state.update { current ->
        val report = CommunityReport(
            id = "r${nextId.getAndIncrement()}",
            type = type,
            placeName = placeName,
            point = point,
            minutesAgo = 0,
            confirmations = 1,
            confirmedByMe = true,
            mine = true
        )
        listOf(report) + current
    }

    /** "Hâlâ var" onayını açıp kapatır; sayaç buna göre bir artar veya azalır. */
    private fun CommunityReport.toggled(): CommunityReport {
        val delta = when (confirmedByMe) {
            true -> -1
            false -> 1
        }
        return copy(confirmedByMe = !confirmedByMe, confirmations = confirmations + delta)
    }
}
