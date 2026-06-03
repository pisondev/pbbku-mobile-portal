package id.pbbku.mobileportal.ui.tutorial

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class TutorialStep(
    val targetId: String,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
)

data class TutorialVisibilityState(
    val visible: Boolean,
    val dismiss: () -> Unit,
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
fun rememberTutorialVisibilityState(
    pageKey: String,
    helpRequestId: Int,
): TutorialVisibilityState {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("pbbku_tutorial_state", Context.MODE_PRIVATE)
    }
    var visible by rememberSaveable(pageKey) { mutableStateOf(false) }
    var lastHandledHelpRequestId by rememberSaveable(pageKey) {
        mutableIntStateOf(helpRequestId)
    }

    LaunchedEffect(pageKey) {
        visible = false
    }
    LaunchedEffect(helpRequestId) {
        if (helpRequestId > lastHandledHelpRequestId) {
            lastHandledHelpRequestId = helpRequestId
            visible = true
        }
    }

    return TutorialVisibilityState(
        visible = visible,
        dismiss = {
            prefs.edit().putBoolean("seen_$pageKey", true).apply()
            visible = false
        },
    )
}

@Composable
fun TutorialOverlay(
    visible: Boolean,
    steps: List<TutorialStep>,
    targetState: TutorialTargetState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    if (!visible || steps.isEmpty()) return

    var currentIndex by rememberSaveable(visible, steps.size) { mutableIntStateOf(0) }
    val currentStep = steps[currentIndex.coerceIn(0, steps.lastIndex)]

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .navigationBarsPadding()
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
                isLastStep = currentIndex == steps.lastIndex,
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

@Composable
private fun TutorialActions(
    isLastStep: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
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
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                text = "Lewati",
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = if (isLastStep) "Selesai" else "Lanjut",
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
