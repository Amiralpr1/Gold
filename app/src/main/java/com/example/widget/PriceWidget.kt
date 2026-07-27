package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.data.model.JalaliCalendar
import com.example.data.model.PriceItem
import com.example.data.model.PriceParser
import com.example.data.model.PricesResponse
import com.example.data.model.PriceTrend
import com.example.data.repository.PriceRepository

import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items

class PriceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context, id)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, id: GlanceId) {
        val widgetId = try {
            androidx.glance.appwidget.GlanceAppWidgetManager(context).getAppWidgetId(id)
        } catch (e: Exception) {
            -1
        }

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val defaultAll = setOf(
            "طلای ۱۸ عیار",
            "دلار نقدی",
            "سکه امامی",
            "سکه بهار آزادی",
            "نیم سکه",
            "ربع سکه",
            "اونس طلا"
        )

        val selectedNames = prefs.getStringSet("selected_widget_items", null)
            ?: prefs.getStringSet("widget_$widgetId", null)
            ?: prefs.getStringSet("default_widget_items", null)
            ?: defaultAll

        val repository = PriceRepository(context)
        val pricesResponse = produceState(
            initialValue = PricesResponse(JalaliCalendar.getCurrentIranDateTime(), emptyList())
        ) {
            val fresh = repository.getCachedPrices()
            value = if (fresh.items.isNotEmpty()) fresh else repository.fetchFreshPrices()
        }.value

        val itemsToShow = pricesResponse.items.filter { it.name in selectedNames }

        // Minimal Frosted Glass Container
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xE60A0F1D))) // Translucent deep navy glass
                .padding(10.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "طلا و ارز (ایران)",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFD700)), // Gold Accent
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        text = PriceParser.englishToPersianDigits(pricesResponse.lastUpdated),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF94A3B8)),
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                if (itemsToShow.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ موردی انتخاب نشده است",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF94A3B8)),
                                fontSize = 11.sp
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        items(itemsToShow) { item ->
                            WidgetPriceRow(item)
                            Spacer(modifier = GlanceModifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetPriceRow(item: PriceItem) {
        val trendColor = when (item.trend) {
            PriceTrend.UP -> Color(0xFF10B981) // Green
            PriceTrend.DOWN -> Color(0xFFEF4444) // Red
            PriceTrend.NEUTRAL -> Color(0xFF94A3B8) // Slate
        }

        val trendSymbol = when (item.trend) {
            PriceTrend.UP -> "▲"
            PriceTrend.DOWN -> "▼"
            PriceTrend.NEUTRAL -> "●"
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(Color(0x33334155))) // Glass translucent row
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Price & Unit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${item.currentPrice} ${item.currentUnit}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF8FAFC)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Item Name
            Text(
                text = item.name,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFFFD700)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.width(6.dp))

            // Trend symbol indicator
            Text(
                text = trendSymbol,
                style = TextStyle(
                    color = ColorProvider(trendColor),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
