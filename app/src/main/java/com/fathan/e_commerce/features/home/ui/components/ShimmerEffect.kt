package com.fathan.e_commerce.features.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Shimmer color palette — adjust to your theme
private val ShimmerLight = Color(0xFFEEEEEE)
private val ShimmerMiddle = Color(0xFFDDDDDD)
private val ShimmerDark = Color(0xFFCCCCCC)

/** Returns an animated linear gradient brush that moves horizontally */
@Composable
private fun shimmerBrush(widthPx: Float = 1000f): Brush {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = widthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val colors = listOf(ShimmerLight, ShimmerMiddle, ShimmerLight)
    return Brush.linearGradient(
        colors,
        start = Offset(x = translateAnim - widthPx, y = 0f),
        end = Offset(x = translateAnim, y = 0f)
    )
}

/** A single shimmer tab pill */
@Composable
fun ShimmerTabItem(width: Dp = 92.dp, height: Dp = 36.dp) {
    val brush = shimmerBrush()
    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(end = 8.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(brush)
        )
    }
}

/** Row of shimmer tabs (used when categories are loading) */
@Composable
fun ShimmerTabsRow(count: Int = 6) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) { ShimmerTabItem() }
    }
}

/** Single shimmer product card */
@Composable
fun ShimmerProductCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // image block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // title placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // price placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // rating / meta placeholder
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/** Render rows of shimmer cards (2 columns) — used in HomeScreen while loading */
@Composable
fun ShimmerProductGridRows(rows: Int = 3) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        repeat(rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerProductCard(modifier = Modifier.weight(1f))
                ShimmerProductCard(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
