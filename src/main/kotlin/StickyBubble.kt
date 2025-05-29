import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StickyBubble(
	state: StickyBubbleState,
	bubbleShape: Shape = CircleShape,
	bubbleAutoHandleVisibility: Boolean = true,
	bubbleContent: @Composable BoxScope.() -> Unit,
) {
	val scope = rememberCoroutineScope()
	var isAnimating by remember { mutableStateOf(false) }
	val scale by animateFloatAsState(
		targetValue = if (isAnimating) 0.8f else 1f,
		animationSpec = spring(
			dampingRatio = 0.15f,
			stiffness = 200f
		)
	)


	if (bubbleAutoHandleVisibility && state.currentStatus == StickyBubbleState.Status.HIDDEN) return

	BoxWithConstraints(
		modifier = Modifier
			.fillMaxSize()
			.onSizeChanged { state.onParentSizeChanged(it) }
	) {

		// ---------------------------------
		// Bubble Content

		// Bubble content is only shown when the status is collapsed &&
		// constrain are available &&
		// and bubbleAutoHandleVisibility is true, otherwise the user is expected to handle it itself (allow using AnimatedVisibility)
		if (constraints.maxWidth > 0 && !bubbleAutoHandleVisibility || state.currentStatus == StickyBubbleState.Status.COLLAPSED) {
			Box(
				modifier = Modifier
					.offset {
						IntOffset(
							state.currentOffset.value.x.roundToInt(),
							state.currentOffset.value.y.roundToInt()
						)
					}
					.wrapContentSize()
					.scale(scale)
					.clip(bubbleShape)
					.thenIf(state.currentStatus == StickyBubbleState.Status.COLLAPSED) {
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
}

class StickyBubbleState(
	private val initialOffsetValue: Offset,
	internal val density: Density,
	private val animationSpecBubble: AnimationSpec<Offset>,
	private val coroutineScope: CoroutineScope,
	private val onRemovedCallback: () -> Unit
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

	private var dragAmount = Offset.Zero
	private val offsetAnimatable = Animatable(initialOffsetValue, Offset.VectorConverter)
	internal var parentSize by mutableStateOf(IntSize.Zero)
	internal var bubbleSizePx by mutableStateOf(IntSize.Zero)

	// ----------------------------------------------
	// MARK: PUBLIC

	val currentOffset: State<Offset> = offsetAnimatable.asState()
	var currentStatus by mutableStateOf(Status.COLLAPSED)
		internal set

	private val _isDragging = MutableStateFlow(false)
	val isDragging = _isDragging.asStateFlow()

	private val _dragEvent = MutableSharedFlow<DragEvent>(999)
	internal val dragEvent = _dragEvent.asSharedFlow()

	// ----------------------------------------------
	// MARK: PUBLIC METHODS

	fun onParentSizeChanged(newParentSize: IntSize) {
		val oldParentSize = parentSize
		parentSize = newParentSize
		if (newParentSize.width > 0 && newParentSize.height > 0) {
			coroutineScope.launch {
				if (oldParentSize == IntSize.Zero && offsetAnimatable.value.x == Float.MAX_VALUE) {
					// First composition with valid size
					val initialX = newParentSize.width - bubbleSizePx.width - with(density) { 16.dp.toPx() }
					val initialY =
						offsetAnimatable.value.y.coerceIn(0f, (newParentSize.height - bubbleSizePx.height).toFloat())
					offsetAnimatable.snapTo(Offset(initialX, initialY))
				} else {
					offsetAnimatable.snapTo(
						Offset(
							offsetAnimatable.value.x.coerceIn(0f, (newParentSize.width - bubbleSizePx.width).toFloat()),
							offsetAnimatable.value.y.coerceIn(
								0f, (newParentSize.height - bubbleSizePx.height).toFloat()
							)
						)
					)
				}
			}
		}
	}

	// ----------------------------------------------
	// Actions

	fun onBubbleClick() {
		if (abs(dragAmount.x) < 5 && abs(dragAmount.y) < 5 && currentStatus == Status.COLLAPSED) {
			currentStatus = Status.EXPANDED
		}
	}

	fun collapseExpandedView() {
		currentStatus = Status.COLLAPSED
		dragAmount = Offset.Zero
	}

	internal fun onDragStart() {
		if (currentStatus == Status.COLLAPSED) {
			_dragEvent.tryEmit(DragEvent.DRAG_START)
			_isDragging.value = true
		}
	}

	internal fun onDrag(dragAmountDelta: Offset) {
		if (currentStatus == Status.COLLAPSED && isDragging.value) {
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
		if (currentStatus == Status.COLLAPSED) {
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
			dragAmount = Offset.Zero
		}
	}

	// ----------------------------------------------
	// MARK: INTERNAL METHODS

	fun hideBubble() {
		if (currentStatus != Status.HIDDEN) {
			currentStatus = Status.HIDDEN
			onRemovedCallback()
		}
	}

	fun showBubble() {
		if (currentStatus == Status.HIDDEN) {
			currentStatus = Status.COLLAPSED
			dragAmount = Offset.Zero
			coroutineScope.launch {
				offsetAnimatable.snapTo(initialOffsetValue)
				onParentSizeChanged(parentSize)
			}
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
			coroutineScope = coroutineScope,
			density = density,
			onRemovedCallback = onRemoved
		)
	}
}

@Composable
fun StickyBubbleState.isExpanded(): Boolean = this.currentStatus == StickyBubbleState.Status.EXPANDED

@Composable
fun StickyBubbleState.isCollapsed(): Boolean = this.currentStatus == StickyBubbleState.Status.COLLAPSED

@Composable
fun StickyBubbleState.isHidden(): Boolean = this.currentStatus == StickyBubbleState.Status.HIDDEN

@Composable
fun StickyBubbleState.isDragging(): Boolean = this.isDragging.collectAsState().value

fun Modifier.thenIf(condition: Boolean, other: () -> Modifier): Modifier = if (condition) then(other()) else this