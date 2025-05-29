package com.bitsycore.compose.kit.common

import androidx.compose.ui.Modifier

fun Modifier.thenIf(condition: Boolean, other: () -> Modifier): Modifier = if (condition) then(other()) else this