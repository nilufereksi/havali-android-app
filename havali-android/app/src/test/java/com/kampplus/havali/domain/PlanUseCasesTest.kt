package com.kampplus.havali.domain

import com.kampplus.havali.data.memory.DemoSeed
import com.kampplus.havali.data.memory.InMemoryGroupVoteRepository
import com.kampplus.havali.data.memory.InMemoryPlanRepository
import com.kampplus.havali.domain.model.PlanSuggestion
import com.kampplus.havali.domain.model.VoteChoice
import com.kampplus.havali.domain.model.VoteOutcome
import com.kampplus.havali.domain.usecase.plan.CastVoteUseCase
import com.kampplus.havali.domain.usecase.plan.DetectPlanConflictsUseCase
import com.kampplus.havali.testing.TestWeather
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanUseCasesTest {

    private val today = LocalDate.of(2026, 10, 25)

    @Test
    fun `son oy evet ise plan olusur ve takvime eklenir`() {
        val plans = InMemoryPlanRepository(emptyList())
        val castVote = CastVoteUseCase(InMemoryGroupVoteRepository(DemoSeed.proposal(today)), plans)

        val result = castVote(VoteChoice.YES)

        assertEquals(VoteOutcome.ACCEPTED, result.outcome)
        assertEquals(4, result.yesCount)
        assertEquals("Kordon Ekibi", plans.plans.value.single().groupName)
    }

    @Test
    fun `hayir oyu karari degistirmez ama plan kullaniciya eklenmez`() {
        val plans = InMemoryPlanRepository(emptyList())
        val castVote = CastVoteUseCase(InMemoryGroupVoteRepository(DemoSeed.proposal(today)), plans)

        val result = castVote(VoteChoice.NO)

        assertEquals(VoteOutcome.ACCEPTED, result.outcome)
        assertTrue(plans.plans.value.isEmpty())
    }

    @Test
    fun `oyu geri almak oylamayi bekleyen duruma dondurur`() {
        val votes = InMemoryGroupVoteRepository(DemoSeed.proposal(today))
        votes.vote(VoteChoice.YES)

        votes.clearMyVote()

        assertNull(votes.proposal.value.myVote)
        assertEquals(VoteOutcome.PENDING, votes.proposal.value.outcome)
    }

    @Test
    fun `esnek piknik kuru gune, sabit hali saha kapali mekana yonlendirilir`() {
        val seed = DemoSeed.plans(today.atTime(9, 0))
        val days = listOf(
            TestWeather.day(today.plusDays(3), 12.0, 16.0, rain = 80),
            TestWeather.day(today.plusDays(4), 11.0, 15.0, rain = 80),
            TestWeather.day(today.plusDays(5), 15.0, 24.0, rain = 5)
        )

        val suggestions = DetectPlanConflictsUseCase()(seed, days)

        val football = suggestions.single { it.plan.id == "football" }
        val picnic = suggestions.single { it.plan.id == "picnic" } as PlanSuggestion.MoveToDay
        assertTrue(football is PlanSuggestion.PreferIndoor)
        assertEquals(today.plusDays(5), picnic.date)
        assertEquals(24, picnic.maxTemperatureC)
    }

    @Test
    fun `demo bulusmasi uygulama hangi saatte acilirsa acilsin ileride kalir`() {
        val morning = DemoSeed.plans(today.atTime(9, 0)).single { it.id == "cem" }
        val evening = DemoSeed.plans(today.atTime(20, 30)).single { it.id == "cem" }
        val night = DemoSeed.plans(today.atTime(23, 10)).single { it.id == "cem" }

        assertEquals(today.atTime(18, 0), morning.start)
        assertEquals(today.atTime(22, 0), evening.start)
        assertEquals(today.plusDays(1).atTime(18, 0), night.start)
    }
}
