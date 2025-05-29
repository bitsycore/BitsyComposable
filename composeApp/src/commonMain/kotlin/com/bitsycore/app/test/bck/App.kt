package com.bitsycore.app.test.bck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bitsycomposekit.composeapp.generated.resources.Res
import bitsycomposekit.composeapp.generated.resources.baseline_close_24
import bitsycomposekit.composeapp.generated.resources.fake_app
import com.bitsycore.compose.kit.bubble.BubbleRemoveZone
import com.bitsycore.compose.kit.bubble.StickyBubble
import com.bitsycore.compose.kit.bubble.isExpanded
import com.bitsycore.compose.kit.bubble.isHidden
import com.bitsycore.compose.kit.bubble.rememberStickyBubbleState
import org.jetbrains.compose.resources.painterResource

@Composable
fun App() = MaterialTheme {
	Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
		val bubbleState = rememberStickyBubbleState()

		Image(
			painter = painterResource(Res.drawable.fake_app),
			contentDescription = "Background Image",
			contentScale = ContentScale.FillBounds,
			modifier = Modifier
				.fillMaxSize()
		)

		Text(
			"Main App Content Behind Bubble",
			modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
		)

		BubbleRemoveZone(
			bubbleState = bubbleState,
			modifier = Modifier.align(Alignment.Center),
			content = { inRemoveZone, _ ->
				// Use inRemove zone to animate icon color change between two colors
				val iconColor by animateColorAsState(if (inRemoveZone) Color.Red else Color.White)
				Icon(
					painter = painterResource(Res.drawable.baseline_close_24),
					contentDescription = "Remove Bubble",
					tint = Color.Black,
					modifier = Modifier.size(96.dp).background(iconColor, CircleShape)
				)
			}
		)

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

		AnimatedVisibility(
			visible = !bubbleState.isHidden(),
			enter = scaleIn() + fadeIn(),
			exit = scaleOut() + fadeOut(),
		) {
			StickyBubble(
				state = bubbleState,
				modifier = Modifier.background(
					color = MaterialTheme.colorScheme.primaryContainer,
					shape = CircleShape
				),
				bubbleShape = CircleShape,
			) {
				Icon(
					painter = painterResource(Res.drawable.baseline_close_24),
					contentDescription = "Bubble Icon",
					tint = Color.Red,
					modifier = Modifier.padding(16.dp).size(48.dp)
				)
			}
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
private fun ExpandedContent(collapse: () -> Unit) {
	Box(modifier = Modifier.fillMaxSize().clickable(onClick = collapse, indication = null, interactionSource = null))
	Surface(Modifier.padding(64.dp), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), shadowElevation = 8.dp) {
		Column(
			modifier = Modifier.padding(16.dp).fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text("Expanded View", style = MaterialTheme.typography.headlineSmall)
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