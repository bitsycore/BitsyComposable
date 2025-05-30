package com.bitsycore.compokit.bubble

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bitsycore.compokit.common.thenIf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun StickyBubble(
	state: StickyBubbleState,
	modifier: Modifier,
	onClickAnimationSpec: AnimationSpec<Float> = spring(
		dampingRatio = 0.15f,
		stiffness = 200f
	),
	bubbleShadowElevation: Dp = 8.dp,
	bubbleShape: Shape = CircleShape,
	bubbleContent: @Composable BoxScope.() -> Unit,
) {
	val scope = rememberCoroutineScope()
	var isAnimating by remember { mutableStateOf(false) }
	val scale by animateFloatAsState(
		targetValue = if (isAnimating) 0.8f else 1f,
		animationSpec = onClickAnimationSpec
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.onSizeChanged { state.onParentSizeChanged(it) }
	) {

		// ---------------------------------
		// Bubble Content

		Box(
			modifier = Modifier
				.offset {
					IntOffset(
						state.currentOffset.value.x.roundToInt(),
						state.currentOffset.value.y.roundToInt()
					)
				}
				.scale(scale)
				.shadow(bubbleShadowElevation, bubbleShape, clip = false)
				.clip(bubbleShape)
				.then(modifier)
				.thenIf(state.isClickable()) {
					Modifier.clickable {
						if (!isAnimating) {
							state.onBubbleClick()
							isAnimating = true
							scope.launch {
								delay(100)
								isAnimating = false
							}
						}
					}
				}
				.pointerInput(Unit) {
					detectDragGestures(
						onDragStart = { state.onDragStart() },
						onDrag = { change, dragAmountDelta ->
							change.consume()
							state.onDrag(dragAmountDelta)
						},
						onDragEnd = { state.onDragEnd() },
					)
				}
				.onGloballyPositioned {
					state.bubbleSizePx = it.size
				},
			contentAlignment = Alignment.Center
		) {
			bubbleContent()
		}
	}
}


// ----------------------------------------------
// MARK: REMEMBER

@Composable
fun rememberStickyBubbleState(
	initialOffset: Offset = Offset(Float.POSITIVE_INFINITY, 100f),
	animationSpecBubble: AnimationSpec<Offset> = spring(
		dampingRatio = Spring.DampingRatioMediumBouncy,
		stiffness = Spring.StiffnessLow
	),
	onRemoved: () -> Unit = {},
): StickyBubbleState {
	val coroutineScope = rememberCoroutineScope()
	val density = LocalDensity.current
	return remember(initialOffset, animationSpecBubble, onRemoved, coroutineScope, density) {
		StickyBubbleState(
			initialOffsetValue = initialOffset,
			animationSpecBubble = animationSpecBubble,
			coroutineScope = coroutineScope
		)
	}
}