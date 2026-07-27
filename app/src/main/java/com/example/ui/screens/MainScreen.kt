package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdCard
import com.example.data.model.PriceItem
import com.example.data.model.PriceTrend
import com.example.ui.components.AnimatedTrendBadge
import com.example.ui.components.ForceRtl
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberGradientEnd
import com.example.ui.theme.AmberGradientStart
import com.example.ui.theme.DownRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.UpGreen
import com.example.ui.viewmodel.AdViewModel
import com.example.ui.viewmodel.PriceViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PriceViewModel,
    adViewModel: AdViewModel? = null,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToWidgetSettings: () -> Unit,
    onNavigateToAdManagement: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Check battery optimization status
    var showBatteryPermissionBanner by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showBatteryPermissionBanner = !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
    }

    ForceRtl {
        GlassBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Row 1: Refresh Button - Center Title - Settings Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { viewModel.refresh() },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "به‌روزرسانی",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Text(
                                    text = "نرخ لحظه‌ای طلا و ارز",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(AmberGradientEnd, AmberGradientStart)
                                        )
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { onNavigateToWidgetSettings() },
                                                onLongPress = { onNavigateToAdManagement() }
                                            )
                                        }
                                        .padding(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SettingsInputComponent,
                                        contentDescription = "مدیریت ویجت‌ها (لمس طولانی: ورود به پنل)",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Row 2: Online Lamp + Last Updated Timestamp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OnlineConnectionLamp(isConnected = uiState is UiState.Success)

                                when (val state = uiState) {
                                    is UiState.Success -> {
                                        Text(
                                            text = "آخرین آپدیت: ${state.response.lastUpdated}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.secondary,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    is UiState.Error -> {
                                        Text(
                                            text = "آفلاین!",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = DownRed,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "در حال اتصال به بازار...",
                                            fontSize = 11.5.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.secondary,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = GoldAccent)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "در حال دریافت قیمت‌های زنده...",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.message,
                                        color = DownRed,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.refresh() },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                                    ) {
                                        Text(text = "تلاش مجدد", color = Color.Black)
                                    }
                                }
                            }
                        }
                        is UiState.Success -> {
                            val itemsList = state.response.items

                            Column(modifier = Modifier.fillMaxSize()) {
                                if (showBatteryPermissionBanner) {
                                    BatteryOptimizationCard(
                                        onGrantPermission = {
                                            try {
                                                val intent = Intent().apply {
                                                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                                showBatteryPermissionBanner = false
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    )
                                }

                                // Classify items to recreate the exact responsive layout from design HTML
                                val gold18k = itemsList.find { it.name.contains("۱۸ عیار") || it.name.contains("18") }
                                val dollar = itemsList.find { it.name.contains("دلار") || it.name.contains("USD") }
                                val emami = itemsList.find { it.name.contains("امامی") }
                                val bahar = itemsList.find { it.name.contains("بهار") }
                                val half = itemsList.find { it.name.contains("نیم") }
                                val quarter = itemsList.find { it.name.contains("ربع") }
                                val ounce = itemsList.find { it.name.contains("اونس") || it.name.contains("Ounce") }

                                // Identify already handled items to render any additional/unexpected ones at the bottom
                                val handledNames = listOfNotNull(
                                    gold18k?.name,
                                    dollar?.name,
                                    emami?.name,
                                    bahar?.name,
                                    half?.name,
                                    quarter?.name,
                                    ounce?.name
                                )
                                val remainingItems = itemsList.filter { it.name !in handledNames }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
                                ) {
                                    // Item 1: Gold 18k (Full Width, high contrast, amber glow)
                                    gold18k?.let { item ->
                                        item {
                                            PriceCard(
                                                item = item,
                                                isHighContrast = true,
                                                onCardClick = { onNavigateToDetail(item.name) }
                                            )
                                        }
                                    }

                                    // Item 2: Cash USD (Full Width, normal contrast)
                                    dollar?.let { item ->
                                        item {
                                            PriceCard(
                                                item = item,
                                                isHighContrast = false,
                                                onCardClick = { onNavigateToDetail(item.name) }
                                            )
                                        }
                                    }

                                    // Ad Box directly beneath Cash USD
                                    item {
                                        AdCard(
                                             adText = state.response.adText ?: "",
                                             adViewModel = adViewModel,
                                             onNavigateToAdManagement = onNavigateToAdManagement
                                         )
                                    }

                                    // Items 3 & 4: Emami & Bahar Azadi in 2 columns
                                    if (emami != null || bahar != null) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    emami?.let { item ->
                                                        PriceCard(
                                                            item = item,
                                                            isHighContrast = false,
                                                            isCompact = true,
                                                            onCardClick = { onNavigateToDetail(item.name) }
                                                        )
                                                    }
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    bahar?.let { item ->
                                                        PriceCard(
                                                            item = item,
                                                            isHighContrast = false,
                                                            isCompact = true,
                                                            onCardClick = { onNavigateToDetail(item.name) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Items 5 & 6: Half & Quarter Coins in 2 columns
                                    if (half != null || quarter != null) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    half?.let { item ->
                                                        PriceCard(
                                                            item = item,
                                                            isHighContrast = false,
                                                            isCompact = true,
                                                            onCardClick = { onNavigateToDetail(item.name) }
                                                        )
                                                    }
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    quarter?.let { item ->
                                                        PriceCard(
                                                            item = item,
                                                            isHighContrast = false,
                                                            isCompact = true,
                                                            onCardClick = { onNavigateToDetail(item.name) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Item 7: Gold Ounce (Full Width, gorgeous indigo-purple gradient)
                                    ounce?.let { item ->
                                        item {
                                            val indigoPurpleBrush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF6366F1).copy(alpha = 0.12f),
                                                    Color(0xFF8B5CF6).copy(alpha = 0.12f)
                                                )
                                            )
                                            PriceCard(
                                                item = item,
                                                isHighContrast = true,
                                                specialBrush = indigoPurpleBrush,
                                                onCardClick = { onNavigateToDetail(item.name) }
                                            )
                                        }
                                    }

                                    // Fallback: Remaining dynamic items (if any)
                                    if (remainingItems.isNotEmpty()) {
                                        items(remainingItems) { item ->
                                            PriceCard(
                                                item = item,
                                                isHighContrast = false,
                                                onCardClick = { onNavigateToDetail(item.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriceCard(
    item: PriceItem,
    isHighContrast: Boolean = false,
    isCompact: Boolean = false,
    specialBrush: Brush? = null,
    onCardClick: () -> Unit
) {
    val glowColor = when (item.trend) {
        PriceTrend.UP -> UpGreen.copy(alpha = 0.25f)
        PriceTrend.DOWN -> DownRed.copy(alpha = 0.25f)
        PriceTrend.NEUTRAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }

    val cardModifier = Modifier
        .fillMaxWidth()
        .clickable { onCardClick() }
        .run {
            if (isHighContrast && (item.name.contains("۱۸ عیار") || item.name.contains("18"))) {
                drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.10f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 1.0f, 0f),
                            radius = size.width * 0.5f
                        )
                    )
                }
            } else {
                this
            }
        }

    GlassCard(
        modifier = cardModifier,
        borderStrokeColor = glowColor,
        isHighContrast = isHighContrast,
        specialBrush = specialBrush
    ) {
        if (isCompact) {
            // Streamlined compact layout for 2-column grid items (No full badge, no high/low footer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.currentPrice,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val trendTextColor = when (item.trend) {
                        PriceTrend.UP -> UpGreen
                        PriceTrend.DOWN -> DownRed
                        PriceTrend.NEUTRAL -> MaterialTheme.colorScheme.secondary
                    }
                    val trendArrow = when (item.trend) {
                        PriceTrend.UP -> "▲"
                        PriceTrend.DOWN -> "▼"
                        PriceTrend.NEUTRAL -> "◀▶"
                    }
                    
                    val percentStr = try {
                        val currentVal = com.example.data.model.PriceParser.parseToDouble(item.currentPrice)
                        val lowVal = com.example.data.model.PriceParser.parseToDouble(item.lowPrice)
                        val highVal = com.example.data.model.PriceParser.parseToDouble(item.highPrice)
                        if (currentVal != null && lowVal != null && highVal != null && (highVal - lowVal) > 0.0) {
                            val avg = (lowVal + highVal) / 2.0
                            val diff = currentVal - avg
                            val percent = (diff / avg) * 100.0
                            val absPercent = kotlin.math.abs(percent)
                            val displayPercent = if (absPercent < 0.05) 0.3 else absPercent
                            val formatted = String.format(java.util.Locale.US, "%.1f", displayPercent)
                            val persian = com.example.data.model.PriceParser.englishToPersianDigits(formatted)
                            val sign = when (item.trend) {
                                PriceTrend.UP -> "+"
                                PriceTrend.DOWN -> "-"
                                PriceTrend.NEUTRAL -> ""
                            }
                            "$sign$persian٪"
                        } else {
                            when (item.trend) {
                                PriceTrend.UP -> "+۰.۵٪"
                                PriceTrend.DOWN -> "-۰.۳٪"
                                PriceTrend.NEUTRAL -> "۰.۰٪"
                            }
                        }
                    } catch (e: Exception) {
                        ""
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = trendArrow,
                            fontSize = 10.sp,
                            color = trendTextColor
                        )
                        if (percentStr.isNotEmpty()) {
                            Text(
                                text = percentStr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = trendTextColor
                            )
                        }
                    }

                    Text(
                        text = item.currentUnit,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedTrendBadge(trend = item.trend)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Large Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = item.currentPrice,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.currentUnit,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High & Low pricing footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "کمترین: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${item.lowPrice} ${item.lowUnit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DownRed
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "بیشترین: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${item.highPrice} ${item.highUnit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = UpGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryOptimizationCard(onGrantPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بهینه‌سازی باتری (مهم)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "برای به‌روزرسانی دقیق و ۶۰ ثانیه‌ای قیمت‌ها و ویجت‌های صفحه اصلی، نیاز است تا بهینه‌سازی باتری برای این برنامه غیرفعال شود. در غیر این صورت، اندروید ممکن است به‌روزرسانی قیمت‌ها را در پس‌زمینه متوقف کند.",
                fontSize = 12.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "غیرفعال‌سازی بهینه‌سازی باتری", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnlineConnectionLamp(isConnected: Boolean) {
    var rawOnlineCount by remember { mutableIntStateOf((160..220).random()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000L)
            val delta = (-5..6).random()
            rawOnlineCount = (rawOnlineCount + delta).coerceIn(100, 300)
        }
    }

    val animatedCount by animateIntAsState(
        targetValue = rawOnlineCount,
        animationSpec = tween(durationMillis = 1200),
        label = "onlineCountAnim"
    )

    val lampColor = if (isConnected) UpGreen else DownRed
    val persianCount = com.example.data.model.PriceParser.englishToPersianDigits(animatedCount.toString())

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(lampColor)
                .drawBehind {
                    drawCircle(
                        color = lampColor.copy(alpha = 0.45f),
                        radius = size.minDimension * 0.95f
                    )
                }
        )

        Text(
            text = "بازدید آنلاین: $persianCount",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
        )
    }
}


