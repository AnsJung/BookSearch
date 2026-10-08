package com.example.booksearch.presentation.search

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 검색 결과를 가져오는 동안 표시되는 스켈레톤 UI
 *
 */
@Composable
fun SearchLoadingSkeleton(query: String) {
    val colors = MaterialTheme.colorScheme
    val shimmerProgress = rememberInfiniteTransition(label = "bookSkeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing)),
        label = "shimmerProgress",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface),
    ) {
        HorizontalDivider(thickness = 1.dp, color = colors.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‘$query’ 검색 중",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = colors.primary,
                strokeWidth = 2.dp,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.outline)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) {
            items(8) {
                LoadingBookItemSkeleton(shimmerProgress)
            }
        }
    }
}

@Composable
private fun LoadingBookItemSkeleton(shimmerProgress: State<Float>) {
    val colors = MaterialTheme.colorScheme
    val barShape = RoundedCornerShape(3.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 96.dp)
                    .shimmer(
                        progress = shimmerProgress,
                        baseColor = colors.surfaceVariant,
                        highlightColor = colors.surface,
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .shimmer(shimmerProgress, colors.surfaceVariant, colors.surface, barShape),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(10.dp)
                        .shimmer(shimmerProgress, colors.surfaceVariant, colors.surface, barShape),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(10.dp)
                        .shimmer(shimmerProgress, colors.surfaceVariant, colors.surface, barShape),
                )
            }
            Spacer(modifier = Modifier.size(48.dp))
        }
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(end = 40.dp),
            thickness = 1.dp,
            color = colors.outline,
        )
    }
}

/** 진행값을 그리기 단계에서 읽어 매 프레임 목록 전체가 재구성되지 않게 한다. */
private fun Modifier.shimmer(
    progress: State<Float>,
    baseColor: Color,
    highlightColor: Color,
    shape: Shape,
): Modifier = clip(shape).drawBehind {
    val bandWidth = size.width * 0.6f
    val startX = -bandWidth + (size.width + bandWidth) * progress.value
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(startX, 0f),
            end = Offset(startX + bandWidth, 0f),
        ),
    )
}
