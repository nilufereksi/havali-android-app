package com.kampplus.havali.presentation.plans

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kampplus.havali.R
import com.kampplus.havali.domain.model.GroupProposal
import com.kampplus.havali.domain.model.Member
import com.kampplus.havali.domain.model.VoteChoice
import com.kampplus.havali.presentation.common.Format
import com.kampplus.havali.presentation.common.icon
import com.kampplus.havali.presentation.common.labelRes
import com.kampplus.havali.presentation.components.HavaliCard
import com.kampplus.havali.presentation.components.HavaliIcons
import com.kampplus.havali.presentation.components.Pill
import com.kampplus.havali.presentation.theme.HavaliTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Grup oylaması: oy verilmediyse kaydırılabilir kart, verildiyse sonuç kartı.
 * İkisi arasındaki geçiş AnimatedContent ile yumuşaktır.
 */
@Composable
fun VoteSection(proposal: ProposalUi, onEvent: (PlansEvent) -> Unit, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = proposal.result,
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith (slideOutHorizontally { -it } + fadeOut())
        },
        label = "vote",
        modifier = modifier
    ) { result ->
        when (result) {
            null -> SwipeVoteCard(proposal, onVote = { onEvent(PlansEvent.Vote(it)) })
            else -> VoteResultCard(proposal, result, onChangeVote = { onEvent(PlansEvent.ChangeVote) })
        }
    }
}

/**
 * Sağa kaydır: varım, sola kaydır: yokum.
 * Sürükleme sırasında kart parmağı izler ve hafifçe döner; eşik geçilirse kart ekrandan
 * uçar ve oy kaydedilir, geçilmezse yaylı animasyonla yerine döner.
 * Kaydıramayanlar için aynı işi yapan iki düğme ve erişilebilirlik eylemleri vardır.
 */
@Composable
private fun SwipeVoteCard(proposal: ProposalUi, onVote: (VoteChoice) -> Unit) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val yesLabel = stringResource(R.string.vote_yes)
    val noLabel = stringResource(R.string.vote_no)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val width = constraints.maxWidth.toFloat()
        val threshold = width * SWIPE_THRESHOLD
        val voteWithAnimation: (VoteChoice) -> Unit = { choice -> scope.launch { offsetX.flyOut(choice, width, onVote) } }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .graphicsLayer { rotationZ = offsetX.value / width * MAX_TILT_DEGREES }
                    .pointerInput(width) {
                        detectHorizontalDragGestures(
                            onDragEnd = { scope.settle(offsetX, threshold, width, onVote) },
                            onDragCancel = { scope.launch { offsetX.animateTo(0f, bouncy()) } }
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                        }
                    }
                    .semantics {
                        customActions = listOf(
                            CustomAccessibilityAction(yesLabel) {
                                voteWithAnimation(VoteChoice.YES)
                                true
                            },
                            CustomAccessibilityAction(noLabel) {
                                voteWithAnimation(VoteChoice.NO)
                                true
                            }
                        )
                    }
            ) {
                ProposalCard(proposal)
                VoteStamp(
                    text = yesLabel,
                    color = MaterialTheme.colorScheme.primary,
                    progress = offsetX.value / threshold,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                VoteStamp(
                    text = noLabel,
                    color = HavaliTheme.colors.onDangerTint,
                    progress = -offsetX.value / threshold,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            Text(
                text = stringResource(R.string.vote_swipe_hint),
                style = MaterialTheme.typography.bodySmall,
                color = HavaliTheme.colors.mutedOnBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { voteWithAnimation(VoteChoice.NO) }, modifier = Modifier.weight(1f)) { Text(noLabel) }
                Button(onClick = { voteWithAnimation(VoteChoice.YES) }, modifier = Modifier.weight(1f)) { Text(yesLabel) }
            }
        }
    }
}

private fun CoroutineScope.settle(
    offset: Animatable<Float, AnimationVector1D>,
    threshold: Float,
    width: Float,
    onVote: (VoteChoice) -> Unit
) {
    val choice = when {
        offset.value > threshold -> VoteChoice.YES
        offset.value < -threshold -> VoteChoice.NO
        else -> null
    }
    launch { choice?.let { offset.flyOut(it, width, onVote) } ?: offset.animateTo(0f, bouncy()) }
}

private suspend fun Animatable<Float, AnimationVector1D>.flyOut(choice: VoteChoice, width: Float, onVote: (VoteChoice) -> Unit) {
    val direction = when (choice) {
        VoteChoice.YES -> 1f
        VoteChoice.NO -> -1f
    }
    animateTo(direction * width * FLY_OUT_FACTOR, tween(durationMillis = FLY_OUT_MS))
    onVote(choice)
}

private fun bouncy() = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)

@Composable
private fun VoteStamp(text: String, color: Color, progress: Float, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.headlineSmall,
        color = color,
        modifier = modifier
            .padding(18.dp)
            .alpha(progress.coerceIn(0f, 1f))
            .graphicsLayer { rotationZ = -12f }
            .border(3.dp, color, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun ProposalCard(ui: ProposalUi) {
    val proposal = ui.proposal
    HavaliCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(proposal.proposer, color = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.vote_proposed_by, proposal.proposer.name), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.just_now),
                    style = MaterialTheme.typography.bodySmall,
                    color = HavaliTheme.colors.mutedOnSurface
                )
            }
            ui.day?.let { day ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(day.kind.icon, contentDescription = null, tint = HavaliTheme.colors.onInfoTint, modifier = Modifier.size(22.dp))
                    Column {
                        Text(text = Format.degrees(day.maxTemperatureC), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(day.kind.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = HavaliTheme.colors.mutedOnSurface
                        )
                    }
                }
            }
        }
        Text(text = proposal.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(
                R.string.vote_when_where,
                Format.dayLong(proposal.start.toLocalDate()),
                Format.date(proposal.start.toLocalDate()),
                Format.time(proposal.start.toLocalTime()),
                proposal.placeName
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = HavaliTheme.colors.mutedOnSurface
        )
        ui.day?.let { day ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(
                    text = stringResource(R.string.vote_rain_chance, day.precipitationProbability),
                    container = HavaliTheme.colors.infoTint,
                    content = HavaliTheme.colors.onInfoTint
                )
                Pill(
                    text = stringResource(R.string.vote_uv, day.uvIndexMax.roundToInt()),
                    container = HavaliTheme.colors.accentSoft,
                    content = HavaliTheme.colors.onAccentSoft
                )
            }
        }
        MemberVotes(proposal)
    }
}

@Composable
private fun MemberVotes(proposal: GroupProposal) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        proposal.members.forEach { member ->
            Avatar(member, color = voteColor(proposal.votes[member.id]), label = voteMark(proposal, member))
        }
        Text(
            text = stringResource(R.string.vote_counts, proposal.yesCount, proposal.noCount),
            style = MaterialTheme.typography.bodySmall,
            color = HavaliTheme.colors.mutedOnSurface,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun voteColor(vote: VoteChoice?): Color = when (vote) {
    VoteChoice.YES -> MaterialTheme.colorScheme.primary
    VoteChoice.NO -> HavaliTheme.colors.dangerTint
    null -> HavaliTheme.colors.infoTint
}

private fun voteMark(proposal: GroupProposal, member: Member): String = when (member.id) {
    proposal.selfId -> "?"
    else -> member.initial
}

@Composable
private fun Avatar(member: Member, color: Color, label: String = member.initial) {
    Box(modifier = Modifier.size(32.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = HavaliTheme.colors.deep)
    }
}

/** Oy sonrası: karar, sayılar ve "Oyumu değiştir". Plan oluştuysa konfeti yağar. */
@Composable
private fun VoteResultCard(ui: ProposalUi, result: VoteResult, onChangeVote: () -> Unit) {
    val proposal = ui.proposal
    Box {
        HavaliCard(container = resultContainer(result)) {
            Text(text = stringResource(result.titleRes), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(result.bodyRes, proposal.proposer.name, proposal.yesCount),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(
                    text = stringResource(R.string.vote_yes_count, proposal.yesCount),
                    container = HavaliTheme.colors.deep,
                    content = Color.White
                )
                Pill(
                    text = stringResource(R.string.vote_no_count, proposal.noCount),
                    container = HavaliTheme.colors.dangerTint,
                    content = HavaliTheme.colors.onDangerTint
                )
            }
            Button(
                onClick = onChangeVote,
                colors = ButtonDefaults.buttonColors(containerColor = HavaliTheme.colors.deep, contentColor = Color.White)
            ) {
                Icon(HavaliIcons.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.vote_change), modifier = Modifier.padding(start = 6.dp))
            }
        }
        if (result == VoteResult.JOINED) Confetti(modifier = Modifier.matchParentSize())
    }
}

@Composable
private fun resultContainer(result: VoteResult): Color = when (result) {
    VoteResult.JOINED -> MaterialTheme.colorScheme.primary
    VoteResult.SKIPPED, VoteResult.WAITING -> MaterialTheme.colorScheme.surface
    VoteResult.CANCELLED -> HavaliTheme.colors.infoTint
}

private val VoteResult.titleRes: Int
    get() = when (this) {
        VoteResult.JOINED -> R.string.vote_result_joined_title
        VoteResult.SKIPPED -> R.string.vote_result_skipped_title
        VoteResult.CANCELLED -> R.string.vote_result_cancelled_title
        VoteResult.WAITING -> R.string.vote_result_waiting_title
    }

private val VoteResult.bodyRes: Int
    get() = when (this) {
        VoteResult.JOINED -> R.string.vote_result_joined_body
        VoteResult.SKIPPED -> R.string.vote_result_skipped_body
        VoteResult.CANCELLED -> R.string.vote_result_cancelled_body
        VoteResult.WAITING -> R.string.vote_result_waiting_body
    }

/** Kısa bir konfeti yağmuru: tek bir Animatable 0'dan 1'e gider, parçacıklar onunla düşer. */
@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(durationMillis = CONFETTI_MS)) }
    val colors = listOf(HavaliTheme.colors.deep, Color.White, HavaliTheme.colors.map.rain, HavaliTheme.colors.dangerTint)
    Canvas(modifier = modifier) {
        val t = progress.value
        repeat(CONFETTI_PIECES) { index ->
            val x = size.width * ((index * GOLDEN_RATIO) % 1f)
            val y = size.height * (t * (0.8f + (index % 5) * 0.1f)) - 20f
            drawCircle(colors[index % colors.size].copy(alpha = 1f - t), radius = 5f + (index % 3) * 2f, center = Offset(x, y))
        }
    }
}

private const val SWIPE_THRESHOLD = 0.3f
private const val MAX_TILT_DEGREES = 12f
private const val FLY_OUT_FACTOR = 1.5f
private const val FLY_OUT_MS = 260
private const val CONFETTI_MS = 1600
private const val CONFETTI_PIECES = 36
private const val GOLDEN_RATIO = 0.618034f
