package com.bitsycore.compose.kit.bubble

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class StickyBubbleState(
	initialOffsetValue: Offset,
	clickable: Boolean = true,
	private val animationSpecBubble: AnimationSpec<Offset>,
	private val coroutineScope: CoroutineScope,
	private val dragOffsetForClick: Int = 10,
) {

	enum class Status {
		COLLAPSED,
		EXPANDED,
		HIDDEN,
	}

	internal enum class DragEvent {
		DRAG_START, DRAGGING, DRAG_END
	}

	// ----------------------------------------------
	// MARK: INTERNAL

	private var dragAmount = Offset.Companion.Zero
	private val offsetAnimatable = Animatable(initialOffsetValue, Offset.Companion.VectorConverter)
	internal var parentSize by mutableStateOf(IntSize.Companion.Zero)
	internal var bubbleSizePx by mutableStateOf(IntSize.Companion.Zero)

	// ----------------------------------------------
	// MARK: PUBLIC
	val isClickable = MutableStateFlow(clickable)

	val currentOffset: State<Offset> = offsetAnimatable.asState()
	val currentStatus = MutableStateFlow(Status.COLLAPSED)

	private val _isDragging = MutableStateFlow(false)
	val isDragging = _isDragging.asStateFlow()

	private val _dragEvent = MutableSharedFlow<DragEvent>(999)
	internal val dragEvent = _dragEvent.asSharedFlow()

	// ----------------------------------------------
	// MARK: PUBLIC METHODS

	fun onParentSizeChanged(newParentSize: IntSize) {
		parentSize = newParentSize
		if (newParentSize.width <= 0 && newParentSize.height <= 0) return
		coroutineScope.launch {
			offsetAnimatable.snapTo(
				Offset(
					offsetAnimatable.value.x.coerceIn(0f, (newParentSize.width - bubbleSizePx.width.toFloat())),
					offsetAnimatable.value.y.coerceIn(0f, (newParentSize.height - bubbleSizePx.height).toFloat())
				)
			)
			onDragEnd()
		}
	}

	// ----------------------------------------------
	// Actions

	fun onBubbleClick() {
		if (abs(dragAmount.x) < dragOffsetForClick && abs(dragAmount.y) < dragOffsetForClick) {
			currentStatus.update { if (it == Status.EXPANDED) Status.COLLAPSED else if (it == Status.COLLAPSED) Status.EXPANDED else it }
		}
	}

	fun collapseExpandedView() {
		currentStatus.value = Status.COLLAPSED
		dragAmount = Offset.Companion.Zero
	}

	internal fun onDragStart() {
		if (currentStatus.value == Status.COLLAPSED) {
			_dragEvent.tryEmit(DragEvent.DRAG_START)
			_isDragging.value = true
		}
	}

	internal fun onDrag(dragAmountDelta: Offset) {
		if (currentStatus.value == Status.COLLAPSED && isDragging.value) {
			_dragEvent.tryEmit(DragEvent.DRAGGING)
			dragAmount += dragAmountDelta
			coroutineScope.launch {
				val newX = (offsetAnimatable.value.x + dragAmountDelta.x)
					.coerceIn(0f, parentSize.width.toFloat() - bubbleSizePx.width)
				val newY = (offsetAnimatable.value.y + dragAmountDelta.y)
					.coerceIn(0f, parentSize.height.toFloat() - bubbleSizePx.height)
				offsetAnimatable.snapTo(Offset(newX, newY))
			}
		}
	}

	internal fun onDragEnd() {
		if (currentStatus.value == Status.COLLAPSED) {
			_dragEvent.tryEmit(DragEvent.DRAG_END)
			_isDragging.value = false
			coroutineScope.launch {
				val targetX = if (offsetAnimatable.value.x + bubbleSizePx.width / 2 < parentSize.width.toFloat() / 2) {
					0f // Snap to left
				} else {
					parentSize.width.toFloat() - bubbleSizePx.width // Snap to right
				}
				if (parentSize.width.toFloat() > 0) {
					// Ensure parentSize is initialized
					offsetAnimatable.animateTo(
						Offset(
							targetX,
							offsetAnimatable.value.y.coerceIn(0f, parentSize.height.toFloat() - bubbleSizePx.height)
						),
						animationSpec = animationSpecBubble
					)

				}
			}
			dragAmount = Offset.Companion.Zero
		}
	}

	// ----------------------------------------------
	// MARK: INTERNAL METHODS

	fun hideBubble() {
		currentStatus.update { Status.HIDDEN }
	}

	fun showBubble() {
		currentStatus.update { Status.COLLAPSED }
	}
}


@Composable
fun StickyBubbleState.isExpanded(): Boolean = this.currentStatus.collectAsState().value == StickyBubbleState.Status.EXPANDED

@Composable
fun StickyBubbleState.isCollapsed(): Boolean = this.currentStatus.collectAsState().value == StickyBubbleState.Status.COLLAPSED

@Composable
fun StickyBubbleState.isHidden(): Boolean = this.currentStatus.collectAsState().value == StickyBubbleState.Status.HIDDEN

@Composable
fun StickyBubbleState.isDragging(): Boolean = this.isDragging.collectAsState().value

@Composable
fun StickyBubbleState.isClickable(): Boolean = this.isClickable.collectAsState().value