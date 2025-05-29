package com.bitsycore.compose.kit.bubble

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize

@Composable
fun BoxScope.BubbleRemoveZone(
	bubbleState: StickyBubbleState,
	modifier: Modifier = Modifier,
	enterTransition: EnterTransition = fadeIn(),
	exitTransition: ExitTransition = fadeOut(),
	onRemove: () -> Unit = { bubbleState.hideBubble() },
	content: @Composable (Boolean, Rect) -> Unit
) {
	var removeZoneBounds by remember { mutableStateOf(Rect.Zero) }
	val bubbleRect by remember {
		derivedStateOf {
			Rect(
				bubbleState.currentOffset.value,
				bubbleState.bubbleSizePx.toSize()
			)
		}
	}
	val bubbleInRemoveZoneBounds by remember {
		derivedStateOf { removeZoneBounds.overlaps(bubbleRect) }
	}

	LaunchedEffect(bubbleState) {
		bubbleState.dragEvent.collect {
			if (it == StickyBubbleState.DragEvent.DRAG_END && bubbleInRemoveZoneBounds) {
				onRemove()
			}
		}
	}

	AnimatedVisibility(
		visible = bubbleState.isDragging() && bubbleState.isCollapsed(),
		enter = enterTransition,
		exit = exitTransition,
		modifier = modifier
	) {
		Box(
			modifier = Modifier
				.wrapContentSize()
				.onGloballyPositioned { layoutCoordinates ->
					val localBounds = layoutCoordinates.boundsInRoot()
					removeZoneBounds = Rect(
						localBounds.left,
						localBounds.top,
						localBounds.right,
						localBounds.bottom
					)
				}
		) {
			content(bubbleInRemoveZoneBounds, removeZoneBounds)
		}

		// ------------------------------------
		// DEBUG

		val bounds = removeZoneBounds
		if (bounds == Rect.Zero) {
			Canvas(modifier = Modifier.matchParentSize()) {
				drawRect(
					color = Color.Magenta.copy(alpha = 0.3f),
					topLeft = Offset(bounds.left, bounds.top),
					size = Size(bounds.width, bounds.height),
					style = Stroke(width = 4f)
				)
			}
		}
	}
}