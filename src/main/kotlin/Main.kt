import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
fun App() = MaterialTheme {
	Box(modifier = Modifier.fillMaxSize()) {
		val bubbleState = rememberStickyBubbleState()

		Text(
			"Main App Content Behind Bubble",
			modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
		)

		BubbleExitZone(bubbleState)

		StickyBubble(
			state = bubbleState,
			bubbleAutoHandleVisibility = false,
			bubbleShape = CircleShape,
		) {
			BubbleContent()
		}

		AnimatedVisibility(
			bubbleState.isExpanded(),
			enter = fadeIn() + scaleIn(),
			exit = scaleOut() + fadeOut(),
		) {
			ExpandedContent { bubbleState.collapseExpandedView() }
		}

		if (bubbleState.isHidden()) {
			Button(onClick = { bubbleState.showBubble() }, modifier = Modifier.align(Alignment.Center)) {
				Text("Show Bubble Again")
			}
		}
	}
}

@Composable
private fun BoxScope.BubbleExitZone(bubbleState: StickyBubbleState, removeZoneHeight: Dp = 100.dp) {
	val removeZoneHeightPx by derivedStateOf { with(bubbleState.density) { removeZoneHeight.toPx() } }
	val isInRemoveZone by derivedStateOf {
		bubbleState.currentOffset.value.y >
				if (bubbleState.parentSize.height > 0) {
					bubbleState.parentSize.height - removeZoneHeightPx - bubbleState.bubbleSizePx.height / 2
				} else {
					Float.MAX_VALUE
				}
	}

	LaunchedEffect(bubbleState) {
		bubbleState.dragEvent.collect {
			if (it == StickyBubbleState.DragEvent.DRAG_END) {
				if (isInRemoveZone) {
					bubbleState.hideBubble()
				}
			}
		}
	}

	AnimatedVisibility(
		visible = bubbleState.isDragging() && bubbleState.isCollapsed(),
		enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
		exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
		modifier = Modifier.align(Alignment.BottomCenter)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(100.dp)
				.background(
					if (isInRemoveZone) MaterialTheme.colors.error.copy(alpha = 0.7f)
					else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
				),
			contentAlignment = Alignment.Center
		) {
			Icon(
				Icons.Filled.Delete,
				contentDescription = "Remove Bubble",
				tint = if (isInRemoveZone) Color.White else MaterialTheme.colors.error,
				modifier = Modifier.size(48.dp)
			)
		}
	}
}


@Composable
private fun BubbleContent() {
	Icon(
		Icons.Filled.Done,
		contentDescription = "Bubble Icon",
		tint = Color.Red,
		modifier = Modifier.background(color = Color.Black, shape = CircleShape).size(48.dp)
	)
}

@Composable
private fun ExpandedContent(collapse: () -> Unit) {
	Box(modifier = Modifier.fillMaxSize().clickable(onClick = collapse, indication = null, interactionSource = null))
	Surface(Modifier.padding(64.dp), color = MaterialTheme.colors.surface, shape = RoundedCornerShape(16.dp), elevation = 8.dp) {
		Column(
			modifier = Modifier.padding(16.dp).fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text("Expanded View", style = MaterialTheme.typography.h5)
			Spacer(Modifier.height(16.dp))
			Text("This is the content that appears when the bubble is expanded. You can put anything here!")
			Spacer(Modifier.weight(1f))
			Row {
				Button(onClick = {
					println("Action clicked!")
				}) {
					Text("Some Action")
				}
				Spacer(Modifier.width(8.dp))
				Button(onClick = collapse) {
					Text("Collapse")
				}
			}
		}
	}
}

fun main() = application {
	Window(onCloseRequest = ::exitApplication) {
		App()
	}
}
