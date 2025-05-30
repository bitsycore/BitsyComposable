package com.bitsycore.app.test.bck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bitsycomposekit.composeapp.generated.resources.Res
import bitsycomposekit.composeapp.generated.resources.baseline_close_24
import bitsycomposekit.composeapp.generated.resources.bitsycore_logo
import com.bitsycore.compose.kit.bubble.BubbleRemoveZone
import com.bitsycore.compose.kit.bubble.StickyBubble
import com.bitsycore.compose.kit.bubble.StickyBubbleState
import com.bitsycore.compose.kit.bubble.isExpanded
import com.bitsycore.compose.kit.bubble.isHidden
import com.bitsycore.compose.kit.bubble.rememberStickyBubbleState
import org.jetbrains.compose.resources.painterResource

@Composable
fun App(paddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues()) {
	val bubbleState = rememberStickyBubbleState()
	val bottomPadding = paddingValues.calculateBottomPadding()
	val topPadding = paddingValues.calculateTopPadding()
	Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
		Box(modifier = Modifier.fillMaxSize()) {

			// -------------------------------------
			// MARK: SCREEN CONTENT

			ScreenContent(topPadding, bottomPadding, bubbleState)

			// -------------------------------------
			// MARK: REMOVE ZONE
			val zoneMode = "1".toInt()
			when (zoneMode) {
				1 -> CircleRemoveZone(bubbleState)
				2 -> BottomRemoveZone(bubbleState)
			}

			// -------------------------------------
			// MARK: BUBBLE

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
		}
	}

	// -------------------------------------
	// MARK: DIM BACKGROUND

	AnimatedVisibility(
		bubbleState.isExpanded(),
		enter = fadeIn(),
		exit = fadeOut(),
	) {
		Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
	}

	// -------------------------------------
	// MARK: BUBBLE EXPANDED CONTENT

	AnimatedVisibility(
		bubbleState.isExpanded(),
		enter = fadeIn() + scaleIn(),
		exit = scaleOut() + fadeOut(),
	) {
		BubbleExpandedContent(bubbleState)
	}

	Spacer(modifier = Modifier.height(bottomPadding))
}

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

@Composable
private fun BoxScope.BottomRemoveZone(bubbleState: StickyBubbleState) {
	BubbleRemoveZone(
		bubbleState = bubbleState,
		modifier = Modifier.align(Alignment.BottomCenter),
		enterTransition = slideInVertically(initialOffsetY = { it }) + fadeIn(),
		exitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
		content = { inRemoveZone, _ ->
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(100.dp)
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
	)
}

@Composable
private fun BoxScope.CircleRemoveZone(bubbleState: StickyBubbleState) {
	BubbleRemoveZone(
		bubbleState = bubbleState,
		modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
		content = { inRemoveZone, _ ->
			// Use inRemove zone to animate icon color change between two colors
			val iconColor by animateColorAsState(if (inRemoveZone) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
			Icon(
				painter = painterResource(Res.drawable.baseline_close_24),
				contentDescription = "Remove Bubble",
				tint = MaterialTheme.colorScheme.surface,
				modifier = Modifier.size(80.dp).alpha(0.5f).background(iconColor, CircleShape)
			)
		}
	)
}

@Composable
private fun ScreenContent(topPadding: Dp, bottomPadding: Dp, bubbleState: StickyBubbleState) {
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