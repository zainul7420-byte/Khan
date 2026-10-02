package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.threeD.ProjectedDimension
import kotlin.math.roundToInt

@Composable
fun DimensionBadge(
    dimension: ProjectedDimension,
    modifier: Modifier = Modifier
) {
    if (!dimension.isVisible) return

    val density = LocalDensity.current
    val xOffsetDp = with(density) { dimension.labelScreen.x.toDp() }
    val yOffsetDp = with(density) { dimension.labelScreen.y.toDp() }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (dimension.labelScreen.x - 50).roundToInt(),
                    (dimension.labelScreen.y - 14).roundToInt()
                )
            }
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2563EB), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = dimension.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF1E293B)
            )
        )
    }
}
