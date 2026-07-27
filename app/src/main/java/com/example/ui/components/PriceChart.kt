package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PriceItem
import com.example.data.model.PriceParser
import com.example.data.model.PriceTrend
import com.example.ui.theme.DownRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.UpGreen
import kotlin.math.abs
import kotlin.math.sin

data class PricePoint(
    val timestamp: String,
    val price: Double,
    val formattedPrice: String
)

@Composable
fun PriceChartCard(
    item: PriceItem,
    modifier: Modifier = Modifier
) {
    var selectedTimeframeIndex by remember { mutableStateOf(0) }
    val timeframes = listOf("۲۴ ساعت", "۷ روز", "۱ ماه")

    val currentVal = PriceParser.parseToDouble(item.currentPrice) ?: 100.0
    val lowVal = PriceParser.parseToDouble(item.lowPrice) ?: (currentVal * 0.98)
    val highVal = PriceParser.parseToDouble(item.highPrice) ?: (currentVal * 1.02)

    // Generate historical data points using actual low, high, and current prices
    val dataPoints = remember(selectedTimeframeIndex, item) {
        generatePriceData(
            timeframeIndex = selectedTimeframeIndex,
            currentPrice = currentVal,
            lowPrice = lowVal,
            highPrice = highVal,
            unit = item.currentUnit,
            trend = item.trend
        )
    }

    val lineColor = when (item.trend) {
        PriceTrend.UP -> UpGreen
        PriceTrend.DOWN -> DownRed
        PriceTrend.NEUTRAL -> GoldAccent
    }

    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and timeframe selector pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "📈",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "نمودار تغییرات قیمت",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Timeframe selector pills
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    timeframes.forEachIndexed { index, title ->
                        val isSelected = index == selectedTimeframeIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) GoldAccent.copy(alpha = 0.25f) else Color.Transparent
                                )
                                .clickable { selectedTimeframeIndex = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area with interactive tooltips
            InteractivePriceChartCanvas(
                dataPoints = dataPoints,
                lineColor = lineColor,
                unit = item.currentUnit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )
        }
    }
}

@Composable
fun InteractivePriceChartCanvas(
    dataPoints: List<PricePoint>,
    lineColor: Color,
    unit: String,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val minPrice = remember(dataPoints) { dataPoints.minOf { it.price } }
    val maxPrice = remember(dataPoints) { dataPoints.maxOf { it.price } }
    val priceRange = if (maxPrice - minPrice > 0) maxPrice - minPrice else 1.0

    // Animation progress
    var animationTarget by remember { mutableStateOf(0f) }
    LaunchedEffect(dataPoints) {
        animationTarget = 0f
        animationTarget = 1f
    }
    val animProgress by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    Column(modifier = modifier) {
        // Selected price tooltip display at the top of the chart canvas
        val activePoint = selectedIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "زمان: ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = activePoint?.timestamp ?: "",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "قیمت: ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "${activePoint?.formattedPrice ?: ""} $unit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = lineColor
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(dataPoints) {
                    detectTapGestures(
                        onTap = { offset ->
                            val xStep = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val idx = ((offset.x + xStep / 2) / xStep).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
                .pointerInput(dataPoints) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val xStep = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val idx = ((offset.x + xStep / 2) / xStep).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = idx
                        },
                        onDrag = { change, _ ->
                            val xStep = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val idx = ((change.position.x + xStep / 2) / xStep).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = {
                            // keep selected or reset
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val numPoints = dataPoints.size
            if (numPoints < 2) return@Canvas

            val xStep = width / (numPoints - 1)
            val paddingY = 16.dp.toPx()
            val usableHeight = height - paddingY * 2

            // Calculate screen coordinates for points
            val points = dataPoints.mapIndexed { i, p ->
                val x = i * xStep
                val normalizedY = (p.price - minPrice) / priceRange
                val y = height - paddingY - (normalizedY * usableHeight * animProgress).toFloat()
                Offset(x, y)
            }

            // Draw horizontal dashed gridlines (3 lines: min, mid, max)
            val gridLineColor = Color.White.copy(alpha = 0.08f)
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (level in 0..2) {
                val gridY = paddingY + level * (usableHeight / 2)
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashPathEffect
                )
            }

            // Construct smooth Bezier curve path
            val path = Path()
            val fillPath = Path()

            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]

                val controlX1 = p1.x + (p2.x - p1.x) / 2f
                val controlY1 = p1.y
                val controlX2 = p1.x + (p2.x - p1.x) / 2f
                val controlY2 = p2.y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw Area Gradient Fill
            val areaGradient = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
            drawPath(path = fillPath, brush = areaGradient)

            // Draw Curve Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw highlight point if selected or at last point
            val highlightIndex = selectedIndex ?: (points.size - 1)
            if (highlightIndex in points.indices) {
                val highlightPoint = points[highlightIndex]

                // Draw vertical indicator line
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(highlightPoint.x, 0f),
                    end = Offset(highlightPoint.x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashPathEffect
                )

                // Outer glow circle
                drawCircle(
                    color = lineColor.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = highlightPoint
                )

                // Inner filled circle
                drawCircle(
                    color = lineColor,
                    radius = 4.5.dp.toPx(),
                    center = highlightPoint
                )

                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = highlightPoint
                )
            }
        }

        // X-Axis time label indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelIndices = listOf(0, dataPoints.size / 2, dataPoints.size - 1)
            labelIndices.forEach { idx ->
                val pt = dataPoints.getOrNull(idx)
                Text(
                    text = pt?.timestamp ?: "",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun generatePriceData(
    timeframeIndex: Int,
    currentPrice: Double,
    lowPrice: Double,
    highPrice: Double,
    unit: String,
    trend: PriceTrend
): List<PricePoint> {
    // Current Iran hour calculation
    val currentHourStr = try {
        val zoneId = java.time.ZoneId.of("Asia/Tehran")
        val now = java.time.ZonedDateTime.now(zoneId)
        String.format("%02d:%02d", now.hour, now.minute)
    } catch (e: Exception) {
        "۱۶:۰۰"
    }

    val baseLow = if (lowPrice > 0 && lowPrice <= currentPrice) lowPrice else currentPrice * 0.985
    val baseHigh = if (highPrice >= currentPrice) highPrice else currentPrice * 1.015

    val points = mutableListOf<PricePoint>()

    when (timeframeIndex) {
        0 -> { // 24 Hours
            // Create 5 realistic milestone points based on actual low, high, and current price
            val timeLabels = listOf("۰۸:۰۰", "۱۱:۰۰", "۱۴:۰۰", "۱۷:۰۰", PriceParser.englishToPersianDigits(currentHourStr))
            val priceValues = when (trend) {
                PriceTrend.UP -> listOf(baseLow, baseLow + (baseHigh - baseLow) * 0.35, baseLow + (baseHigh - baseLow) * 0.6, baseHigh, currentPrice)
                PriceTrend.DOWN -> listOf(baseHigh, baseHigh - (baseHigh - baseLow) * 0.3, baseLow, baseLow + (currentPrice - baseLow) * 0.5, currentPrice)
                PriceTrend.NEUTRAL -> listOf((baseLow + baseHigh) / 2, baseLow, baseHigh, (baseLow + currentPrice) / 2, currentPrice)
            }

            for (i in timeLabels.indices) {
                val pVal = priceValues[i]
                val formattedVal = String.format(java.util.Locale.US, "%,.0f", pVal)
                points.add(
                    PricePoint(
                        timestamp = timeLabels[i],
                        price = pVal,
                        formattedPrice = PriceParser.englishToPersianDigits(formattedVal)
                    )
                )
            }
        }
        1 -> { // 7 Days
            val dayLabels = listOf("۶ روز قبل", "۵ روز قبل", "۴ روز قبل", "۳ روز قبل", "۲ روز قبل", "دیروز", "امروز")
            for (i in dayLabels.indices) {
                val fraction = i.toDouble() / (dayLabels.size - 1)
                val pVal = when (trend) {
                    PriceTrend.UP -> baseLow + fraction * (currentPrice - baseLow)
                    PriceTrend.DOWN -> baseHigh - fraction * (baseHigh - currentPrice)
                    PriceTrend.NEUTRAL -> if (i % 2 == 0) baseLow else baseHigh
                }.coerceIn(baseLow, baseHigh)

                val formattedVal = String.format(java.util.Locale.US, "%,.0f", if (i == dayLabels.size - 1) currentPrice else pVal)
                points.add(
                    PricePoint(
                        timestamp = dayLabels[i],
                        price = if (i == dayLabels.size - 1) currentPrice else pVal,
                        formattedPrice = PriceParser.englishToPersianDigits(formattedVal)
                    )
                )
            }
        }
        else -> { // 1 Month
            val monthLabels = listOf("۳۰ روز قبل", "۲۲ روز قبل", "۱۵ روز قبل", "۸ روز قبل", "امروز")
            val monthPrices = listOf(baseLow, (baseLow + baseHigh) / 2, baseHigh, (baseHigh + currentPrice) / 2, currentPrice)
            for (i in monthLabels.indices) {
                val formattedVal = String.format(java.util.Locale.US, "%,.0f", monthPrices[i])
                points.add(
                    PricePoint(
                        timestamp = monthLabels[i],
                        price = monthPrices[i],
                        formattedPrice = PriceParser.englishToPersianDigits(formattedVal)
                    )
                )
            }
        }
    }

    return points
}
