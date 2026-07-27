package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdCard
import com.example.ui.components.AnimatedTrendBadge
import com.example.ui.components.ForceRtl
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.DownRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.UpGreen
import com.example.ui.viewmodel.PriceViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemName: String,
    viewModel: PriceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ForceRtl {
        GlassBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "جزئیات قیمت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "بازگشت",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = GoldAccent
                            )
                        }
                        is UiState.Error -> {
                            Text(
                                text = "خطا در بارگذاری اطلاعات.",
                                color = DownRed,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is UiState.Success -> {
                            val item = state.response.items.find { it.name == itemName }
                            
                            if (item == null) {
                                Text(
                                    text = "اطلاعاتی یافت نشد.",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // Title
                                    Text(
                                        text = item.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    AnimatedTrendBadge(trend = item.trend)

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Main glass price display
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "قیمت فعلی بازار",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = item.currentPrice,
                                                    fontSize = 36.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item.currentUnit,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.padding(bottom = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // High & Low pricing card
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp)
                                        ) {
                                            // High price row
                                            DetailPriceRow(
                                                label = "بیشترین قیمت امروز",
                                                price = item.highPrice,
                                                unit = item.highUnit,
                                                color = UpGreen
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Low price row
                                            DetailPriceRow(
                                                label = "کمترین قیمت امروز",
                                                price = item.lowPrice,
                                                unit = item.lowUnit,
                                                color = DownRed
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Update timestamp info
                                    Text(
                                        text = "آخرین زمان به‌روزرسانی در بازار: ${state.response.lastUpdated}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Interactive Slide Show Ad Card
                                    AdCard(adText = state.response.adText ?: "")

                                    Spacer(modifier = Modifier.height(24.dp))
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
fun DetailPriceRow(
    label: String,
    price: String,
    unit: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = price,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
