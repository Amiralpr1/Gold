package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PriceTrend
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DownRed
import com.example.ui.theme.LightCream
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.UpGreen

@Composable
fun ForceRtl(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderStrokeColor: Color? = null,
    isHighContrast: Boolean = false,
    specialBrush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DeepNavy
    
    // Determine background color/brush
    val defaultBgColor = if (isDark) {
        if (isHighContrast) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.05f)
    } else {
        if (isHighContrast) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.75f)
    }

    // Determine border stroke
    val defaultBorderColor = if (isDark) {
        if (isHighContrast) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
    } else {
        if (isHighContrast) Color.Black.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.05f)
    }

    val finalBorderColor = borderStrokeColor ?: defaultBorderColor
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.1f)
    val cardShape = RoundedCornerShape(24.dp) // 24dp matches Tailwind's rounded-3xl

    val cardModifier = modifier
        .shadow(
            elevation = if (isHighContrast) 12.dp else 6.dp,
            shape = cardShape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(cardShape)
        .run {
            if (specialBrush != null) {
                background(specialBrush)
            } else {
                background(defaultBgColor)
            }
        }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, finalBorderColor),
            content = content
        )
    } else {
        Card(
            modifier = cardModifier,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, finalBorderColor),
            content = content
        )
    }
}

@Composable
fun GlassBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DeepNavy
    
    val bgModifier = if (isDark) {
        // Build a gorgeous, modern radial glowing atmosphere on top of the #05070A canvas!
        // To do this simply and cleanly, we use a vertical gradient that begins extremely dark slate-blue
        // and transitions to pure dark, plus we can use drawBehind to add the radial glowing elements.
        Modifier
            .fillMaxSize()
            .background(Color(0xFF05070A))
            .drawBehind {
                // Drawing an elegant top-right radial glow (warm gold/amber)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.06f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                // Drawing an elegant bottom-left radial glow (subtle indigo/purple)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.05f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )
            }
    } else {
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LightCream,
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    }

    Box(
        modifier = bgModifier,
        content = content
    )
}

@Composable
fun AnimatedTrendBadge(trend: PriceTrend) {
    val (color, icon, label) = when (trend) {
        PriceTrend.UP -> Triple(UpGreen, Icons.Default.ArrowUpward, "نزدیک سقف")
        PriceTrend.DOWN -> Triple(DownRed, Icons.Default.ArrowDownward, "نزدیک کف")
        PriceTrend.NEUTRAL -> Triple(NeutralGray, Icons.Default.TrendingFlat, "ثابت")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeAlpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f * alpha))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
