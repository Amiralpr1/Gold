package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WidgetConfigScreen(
                        widgetId = widgetId,
                        onConfigFinished = { selectedItems ->
                            saveWidgetConfig(this@WidgetConfigActivity, widgetId, selectedItems)
                            finishWithSuccess(widgetId)
                        }
                    )
                }
            }
        }
    }

    private fun finishWithSuccess(id: Int) {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun WidgetConfigScreen(widgetId: Int, onConfigFinished: (Set<String>) -> Unit) {
    val items = listOf(
        "طلای ۱۸ عیار",
        "دلار نقدی",
        "سکه امامی",
        "سکه بهار آزادی",
        "نیم سکه",
        "ربع سکه",
        "اونس طلا"
    )

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    
    val initialSelection = remember {
        prefs.getStringSet("widget_$widgetId", null) ?: setOf("طلای ۱۸ عیار", "دلار نقدی")
    }

    val selectedItems = remember { mutableStateOf(initialSelection) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepNavy, Color(0xFF131525))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "تنظیمات ویجت طلا و ارز",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مواردی که می‌خواهید در این ویجت نمایش داده شوند را انتخاب کنید:",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                items.forEach { item ->
                    val isChecked = selectedItems.value.contains(item)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val currentSet = selectedItems.value.toMutableSet()
                                if (checked) {
                                    currentSet.add(item)
                                } else {
                                    currentSet.remove(item)
                                }
                                selectedItems.value = currentSet
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GoldAccent,
                                uncheckedColor = Color(0xFF475569),
                                checkmarkColor = DeepNavy
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item,
                            color = Color(0xFFF1F5F9),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onConfigFinished(selectedItems.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = DeepNavy
                )
            ) {
                Text(
                    text = "ذخیره و ایجاد ویجت",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun saveWidgetConfig(context: Context, id: Int, items: Set<String>) {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()
        .putStringSet("selected_widget_items", items)
        .putStringSet("default_widget_items", items)
    if (id > 0) {
        editor.putStringSet("widget_$id", items)
    }
    editor.apply()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(PriceWidget::class.java)
            for (glanceId in glanceIds) {
                PriceWidget().update(context, glanceId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
