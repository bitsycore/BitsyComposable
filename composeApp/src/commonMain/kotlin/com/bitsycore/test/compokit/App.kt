package com.bitsycore.test.compokit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitsycore.compokit.bubble.BubbleRemoveZone
import com.bitsycore.compokit.bubble.StickyBubble
import com.bitsycore.compokit.bubble.StickyBubbleState
import com.bitsycore.compokit.bubble.isCollapsed
import com.bitsycore.compokit.bubble.isDragging
import com.bitsycore.compokit.bubble.isExpanded
import com.bitsycore.compokit.bubble.isHidden
import com.bitsycore.compokit.bubble.rememberStickyBubbleState
import org.jetbrains.compose.resources.painterResource
import projectcompokit.composeapp.generated.resources.Res
import projectcompokit.composeapp.generated.resources.baseline_close_24
import projectcompokit.composeapp.generated.resources.bitsycore_logo
import kotlin.math.sin

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun App(paddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues()) =
	BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
		MainContent(paddingValues)
	}

@Composable
private fun BoxWithConstraintsScope.MainContent(paddingValues: PaddingValues) {
	// -------------------------------------
	// Sticky Bubble State
	val bubbleState = rememberStickyBubbleState(
		initialOffset = Offset(
			Float.POSITIVE_INFINITY,
			with(LocalDensity.current) { maxHeight.toPx() } / 4f
		)
	)

	// -------------------------------------
	// Dummy content to fill the screen
	ScreenContent(paddingValues, bubbleState)

	// -------------------------------------
	// Zone for removing the bubble
	val zoneMode = "2".toInt()
	when (zoneMode) {
		1 -> CircleRemoveZone(bubbleState)
		2 -> BottomRemoveZone(bubbleState)
	}
	// -------------------------------------
	// Bubble
	AnimatedVisibility(
		visible = !bubbleState.isHidden(),
		enter = scaleIn() + fadeIn(),
		exit = scaleOut() + fadeOut(),
	) {
		StickyBubble(
			state = bubbleState,
			modifier = Modifier.background(
				color = MaterialTheme.colorScheme.primary,
				shape = CircleShape
			),
			bubbleShape = CircleShape,
		) {
			Icon(
				painter = painterResource(Res.drawable.bitsycore_logo),
				contentDescription = "Bubble Icon",
				tint = MaterialTheme.colorScheme.onPrimary,
				modifier = Modifier.size(80.dp)
			)
		}
	}

	// -------------------------------------
	// Dim BG
	AnimatedVisibility(
		bubbleState.isExpanded(),
		enter = fadeIn(),
		exit = fadeOut(),
	) {
		Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))
	}

	// -------------------------------------
	// Bubble expanded content
	AnimatedVisibility(
		bubbleState.isExpanded(),
		enter = fadeIn() + scaleIn(),
		exit = scaleOut() + fadeOut(),
	) {
		BubbleExpandedContent(bubbleState)
	}
}

// -------------------------------------
// MARK: SCREEN CONTENT
// -------------------------------------

@Composable
private fun ScreenContent(paddingValues: PaddingValues, bubbleState: StickyBubbleState) {
	val bottomPadding = paddingValues.calculateBottomPadding()
	val topPadding = paddingValues.calculateTopPadding()
	Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
		Spacer(modifier = Modifier.height(topPadding))

		Text("Main Content", Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
		repeat(10) {
			AnimatedVisibility(bubbleState.isHidden()) {
				Button(onClick = { bubbleState.showBubble() }, Modifier.fillMaxWidth().padding(top = 32.dp)) {
					Text("Show Bubble Again")
				}
			}
			Spacer(Modifier.height(32.dp))
			Text(
				"Those guys are inside you building a piece of shit Ethan!! They're inside you building a monument to compromise!! Fuck them. Fuck those people. Fuck this whole thing Ethan.",
				Modifier.fillMaxWidth()
			)
			Spacer(Modifier.height(8.dp))
			Text("Nobody exists on purpose. Nobody belongs anywhere. We're all going to die. Come watch TV.", Modifier.fillMaxWidth())
			Spacer(Modifier.height(8.dp))
			Text(
				"You're missing the point Morty. Why would he drive a smaller toaster with wheels? I mean, does your car look like a smaller version of your house? No.",
				Modifier.fillMaxWidth()
			)
			Spacer(Modifier.height(32.dp))
			Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
				Button(onClick = {}) {
					Text("Action")
				}
				Spacer(Modifier.size(8.dp))
				Button(onClick = {}) {
					Text("Action")
				}
			}
		}
		Spacer(modifier = Modifier.height(bottomPadding))
	}
}

// -------------------------------------
// MARK: REMOVE ZONES
// -------------------------------------

@Composable
private fun BoxScope.BottomRemoveZone(bubbleState: StickyBubbleState) = BubbleRemoveZone(bubbleState) { inRemoveZone, _, onGloPoReZo ->
	AnimatedVisibility(
		visible = bubbleState.isDragging() && bubbleState.isCollapsed(),
		modifier = Modifier.align(Alignment.BottomCenter),
		enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
		exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(100.dp)
				.onGloballyPositioned(onGloPoReZo)
				.background(
					if (inRemoveZone) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
					else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
				),
			contentAlignment = Alignment.Center
		) {
			Icon(
				painter = painterResource(Res.drawable.baseline_close_24),
				contentDescription = "Remove Bubble",
				tint = if (inRemoveZone) Color.White else MaterialTheme.colorScheme.error,
				modifier = Modifier.size(48.dp)
			)
		}
	}
}

@Composable
private fun BoxScope.CircleRemoveZone(bubbleState: StickyBubbleState) {
	BubbleRemoveZone(
		bubbleState = bubbleState,
		content = { inRemoveZone, _, onGloPoReZo ->
			val alpha by animateFloatAsState(if (bubbleState.isDragging()) 1.0f else 0.0f, animationSpec = tween(500))
			// Use inRemove zone to animate icon color change between two colors
			val infiniteAngle = rememberInfiniteTransition(label = "breathing")
			val angle by infiniteAngle.animateFloat(
				initialValue = 0f,
				targetValue = (2 * Math.PI).toFloat(),
				animationSpec = infiniteRepeatable(
					animation = tween(durationMillis = 2000, easing = LinearEasing)
				),
				label = "angle"
			)
			val scale by remember {
				derivedStateOf {
					1f + 0.2f * sin(angle)
				}
			}
			val iconColor by animateColorAsState(if (inRemoveZone) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
				Icon(
					painter = painterResource(Res.drawable.baseline_close_24),
					contentDescription = "Remove Bubble",
					tint = MaterialTheme.colorScheme.surface,
					modifier = Modifier
						.align(Alignment.BottomCenter)
						.padding(bottom = 80.dp)
						.size(80.dp)
						.scale(scale)
						.alpha(0.75f * alpha)
						.background(iconColor, CircleShape)
						.onGloballyPositioned(onGloPoReZo)
				)
		}
	)
}

// -------------------------------------
// MARK: EXPANDED CONTENT
// -------------------------------------

@Composable
private fun BubbleExpandedContent(bubbleState: StickyBubbleState) {
	Box(
		modifier = Modifier.fillMaxSize().clickable(onClick = { bubbleState.collapseExpandedView() }, indication = null, interactionSource = null)
	) {
		Surface(
			Modifier.padding(16.dp).align(Alignment.Center).clickable(onClick = {}, indication = null, interactionSource = null),
			color = MaterialTheme.colorScheme.surface,
			shape = RoundedCornerShape(16.dp),
			shadowElevation = 8.dp
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text("Expanded View", style = MaterialTheme.typography.headlineSmall)
				Spacer(Modifier.height(16.dp))
				Text("Wow, I really Cronenberged up the whole place, huh Morty? Just a bunch a Cronenbergs walkin' around.")
				Spacer(Modifier.size(16.dp))
				Button(onClick = { -> bubbleState.collapseExpandedView() }) {
					Text("Collapse")
				}
			}
		}
	}
}