package id.pbbku.mobileportal.ui.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class TutorialStep(
    val targetId: String,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
)

@Stable
class TutorialTargetState {
    internal val targets = mutableStateMapOf<String, Rect>()

    fun updateTarget(id: String, rect: Rect) {
        targets[id] = rect
    }
}

@Composable
fun rememberTutorialTargetState(): TutorialTargetState {
    return remember { TutorialTargetState() }
}

fun Modifier.tutorialTarget(
    state: TutorialTargetState,
    id: String,
): Modifier {
    return onGloballyPositioned { coordinates ->
        state.updateTarget(id, coordinates.boundsInRoot())
    }
}

@Composable
fun TutorialOverlay(
    visible: Boolean,
    steps: List<TutorialStep>,
    targetState: TutorialTargetState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onStepAction: (TutorialStep) -> Unit = {},
) {
    if (!visible || steps.isEmpty()) return

    var currentIndex by rememberSaveable(steps.size) { mutableIntStateOf(0) }
    val currentStep = steps[currentIndex.coerceIn(0, steps.lastIndex)]
    val targetRect = targetState.targets[currentStep.targetId]

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val alignToTop = targetRect?.let { it.center.y > constraints.maxHeight * 0.56f } == true
        val cardAlignment = if (alignToTop) Alignment.TopCenter else Alignment.BottomCenter
        TutorialScrim(targetRect = targetRect)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = cardAlignment,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .then(if (alignToTop) Modifier.statusBarsPadding() else Modifier.navigationBarsPadding())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Langkah ${currentIndex + 1} dari ${steps.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = currentStep.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TutorialActions(
                        actionLabel = currentStep.actionLabel,
                        isLastStep = currentIndex == steps.lastIndex,
                        onAction = { onStepAction(currentStep) },
                        onSkip = onDismiss,
                        onNext = {
                            if (currentIndex == steps.lastIndex) {
                                onDismiss()
                            } else {
                                currentIndex += 1
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialActions(
    actionLabel: String?,
    isLastStep: Boolean,
    onAction: () -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actionLabel?.let { label ->
            Button(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
            ) {
                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
            ) {
                Text(
                    text = "Skip",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
            ) {
                Text(
                    text = if (isLastStep) "Selesai" else "Next",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TutorialScrim(targetRect: Rect?) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            },
    ) {
        drawRect(Color.Black.copy(alpha = 0.68f))
        targetRect?.let { rect ->
            val padding = 8.dp.toPx()
            val left = (rect.left - padding).coerceAtLeast(0f)
            val top = (rect.top - padding).coerceAtLeast(0f)
            val right = (rect.right + padding).coerceAtMost(size.width)
            val bottom = (rect.bottom + padding).coerceAtMost(size.height)
            val highlightSize = Size(right - left, bottom - top)
            val corner = 14.dp.toPx()
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = highlightSize,
                cornerRadius = CornerRadius(corner, corner),
                blendMode = BlendMode.Clear,
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(left, top),
                size = highlightSize,
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
