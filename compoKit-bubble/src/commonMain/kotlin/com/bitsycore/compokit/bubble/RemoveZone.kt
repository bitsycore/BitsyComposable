package com.bitsycore.compokit.bubble

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.unit.toSize

@Composable
fun BoxScope.BubbleRemoveZone(
	bubbleState: StickyBubbleState,
	onRemove: () -> Unit = { bubbleState.hideBubble() },
	debugDraw: Boolean = false,
	content: @Composable (Boolean, Rect, (LayoutCoordinates) -> Unit) -> Unit
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

	val onGloballyPositionedRemoveZone: (LayoutCoordinates) -> Unit = { layoutCoordinates: LayoutCoordinates ->
		val rootBounds = layoutCoordinates.boundsInRoot()
		removeZoneBounds = Rect(
			left = rootBounds.left,
			top = rootBounds.top,
			right = rootBounds.right,
			bottom = rootBounds.bottom
		)
	}

	content(bubbleInRemoveZoneBounds, removeZoneBounds, onGloballyPositionedRemoveZone)

	// ------------------------------------
	// DEBUG
	val bounds = removeZoneBounds
	if (debugDraw && bounds != Rect.Zero) {
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