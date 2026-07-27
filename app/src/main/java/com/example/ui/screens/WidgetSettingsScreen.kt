package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ForceRtl
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldAccent
import com.example.widget.saveWidgetConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val allItems = listOf(
        "طلای ۱۸ عیار",
        "دلار نقدی",
        "سکه امامی",
        "سکه بهار آزادی",
        "نیم سکه",
        "ربع سکه",
        "اونس طلا"
    )

    val prefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }

    var selectedItems by remember {
        mutableStateOf(
            prefs.getStringSet("selected_widget_items", null)
                ?: prefs.getStringSet("default_widget_items", null)
                ?: allItems.toSet()
        )
    }

    ForceRtl {
        GlassBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "تنظیمات ویجت",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "انتخاب قیمت‌های ویجت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مواردی که می‌خواهید روی ویجت صفحه اصلی نمایش داده شوند را تیک بزنید:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                            ) {
                                allItems.forEachIndexed { index, item ->
                                    val isChecked = selectedItems.contains(item)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val currentSet = selectedItems.toMutableSet()
                                                if (isChecked) {
                                                    currentSet.remove(item)
                                                } else {
                                                    currentSet.add(item)
                                                }
                                                selectedItems = currentSet
                                                saveWidgetConfig(context, -1, currentSet)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val currentSet = selectedItems.toMutableSet()
                                                if (checked) {
                                                    currentSet.add(item)
                                                } else {
                                                    currentSet.remove(item)
                                                }
                                                selectedItems = currentSet
                                                saveWidgetConfig(context, -1, currentSet)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = GoldAccent,
                                                checkmarkColor = MaterialTheme.colorScheme.background
                                            )
                                        )
                                    }

                                    if (index < allItems.size - 1) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            saveWidgetConfig(context, -1, selectedItems)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ذخیره و اعمال تنظیمات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
