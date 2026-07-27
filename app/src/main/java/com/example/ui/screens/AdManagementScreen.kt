package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AdItem
import com.example.data.model.PriceParser
import com.example.data.model.StoryCtaLayer
import com.example.data.model.StoryImageLayer
import com.example.data.model.StorySubLink
import com.example.data.model.StoryTextLayer
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.UpGreen
import com.example.ui.viewmodel.AdViewModel
import com.example.ui.viewmodel.UserRole
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

// Gallery Persistence Helpers
fun saveUriToAppGallery(context: Context, uri: Uri): String {
    return try {
        val galleryDir = File(context.filesDir, "saved_gallery")
        if (!galleryDir.exists()) galleryDir.mkdirs()
        val file = File(galleryDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        uri.toString()
    }
}

fun getSavedUserImages(context: Context): List<String> {
    val prefs = context.getSharedPreferences("user_gallery_prefs", Context.MODE_PRIVATE)
    val set = prefs.getStringSet("saved_images", emptySet()) ?: emptySet()
    return set.toList()
}

fun addSavedUserImage(context: Context, imageUriStr: String) {
    val prefs = context.getSharedPreferences("user_gallery_prefs", Context.MODE_PRIVATE)
    val current = prefs.getStringSet("saved_images", emptySet())?.toMutableSet() ?: mutableSetOf()
    current.add(imageUriStr)
    prefs.edit().putStringSet("saved_images", current).apply()
}

// Active Selection Target Enum
sealed class SelectedElement {
    object Title : SelectedElement()
    object Subtitle : SelectedElement()
    object Content : SelectedElement()
    object Overlay : SelectedElement()
    object MainCta : SelectedElement()
    object MainBackground : SelectedElement()
    data class ExtraImage(val id: String) : SelectedElement()
    data class ExtraCta(val id: String) : SelectedElement()
    data class ExtraText(val id: String) : SelectedElement()
}

enum class StudioTab {
    TEXTS, IMAGES, LINKS, LAYERS, ADMIN
}

// Helper to map font strings to distinct typography styles in Compose
fun getPersianFontSpecs(fontName: String): Pair<FontFamily, FontWeight> {
    return when {
        fontName.contains("شبنم") -> FontFamily.SansSerif to FontWeight.Bold
        fontName.contains("صمیم") -> FontFamily.Cursive to FontWeight.Normal
        fontName.contains("لاله‌زار") -> FontFamily.Serif to FontWeight.Black
        fontName.contains("یکان") -> FontFamily.Monospace to FontWeight.SemiBold
        fontName.contains("ایران‌سنس") -> FontFamily.SansSerif to FontWeight.Medium
        fontName.contains("تیتر") -> FontFamily.Serif to FontWeight.ExtraBold
        fontName.contains("نستعلیق") -> FontFamily.Cursive to FontWeight.Bold
        fontName.contains("کودک") -> FontFamily.Default to FontWeight.Bold
        fontName.contains("دست‌نویس") -> FontFamily.Cursive to FontWeight.Normal
        else -> FontFamily.SansSerif to FontWeight.Bold
    }
}

@Composable
fun Modifier.storyElementTransformable(
    elementKey: String,
    onSelect: () -> Unit,
    onTransform: (panX: Float, panY: Float, zoom: Float, rotationDegrees: Float) -> Unit
): Modifier {
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnTransform by rememberUpdatedState(onTransform)

    return this
        .pointerInput(elementKey) {
            detectTapGestures(
                onTap = {
                    currentOnSelect()
                }
            )
        }
        .pointerInput(elementKey) {
            detectTransformGestures(panZoomLock = false) { _, pan, zoom, rotation ->
                currentOnSelect()
                currentOnTransform(pan.x, pan.y, zoom, rotation)
            }
        }
}

fun getOrderedLayerKeys(ad: AdItem): List<String> {
    val defaultOrder = listOf("bg", "title", "content", "overlay", "main_cta") +
            ad.extraImages.map { "extra_image_${it.id}" } +
            ad.extraTexts.map { "extra_text_${it.id}" } +
            ad.extraCtas.map { "extra_cta_${it.id}" }

    if (ad.layerOrder.isEmpty()) return defaultOrder

    val currentSet = defaultOrder.toSet()
    val ordered = ad.layerOrder.filter { it in currentSet }.toMutableList()
    defaultOrder.forEach { key ->
        if (key !in ordered) {
            ordered.add(key)
        }
    }
    return ordered
}

private fun updateFontSize(
    sel: SelectedElement,
    valSize: Float,
    currentAd: AdItem,
    adViewModel: AdViewModel
) {
    when (sel) {
        SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleFontSize = valSize.toInt()))
        SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(titleFontSize = valSize.toInt()))
        SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentFontSize = valSize.toInt()))
        SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(customOverlaySize = valSize.toInt()))
        SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(ctaScale = (valSize / 11f).coerceIn(0.5f, 2.5f)))
        is SelectedElement.ExtraCta -> {
            val list = currentAd.extraCtas.map {
                if (it.id == sel.id) it.copy(scale = (valSize / 11f).coerceIn(0.5f, 2.5f)) else it
            }
            adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
        }
        is SelectedElement.ExtraText -> {
            val list = currentAd.extraTexts.map {
                if (it.id == sel.id) it.copy(fontSize = valSize.toInt()) else it
            }
            adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
        }
        else -> {}
    }
}

private fun applyTextColor(
    sel: SelectedElement,
    hex: String,
    currentAd: AdItem,
    adViewModel: AdViewModel
) {
    when (sel) {
        SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleColorHex = hex))
        SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(subtitleColorHex = hex))
        SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentColorHex = hex))
        SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(customOverlayColor = hex))
        SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(ctaTextColorHex = hex))
        is SelectedElement.ExtraCta -> {
            val list = currentAd.extraCtas.map {
                if (it.id == sel.id) it.copy(textColorHex = hex) else it
            }
            adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
        }
        is SelectedElement.ExtraText -> {
            val list = currentAd.extraTexts.map {
                if (it.id == sel.id) it.copy(colorHex = hex) else it
            }
            adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
        }
        else -> {}
    }
}

private fun applyBgColor(
    sel: SelectedElement,
    hex: String,
    currentAd: AdItem,
    adViewModel: AdViewModel
) {
    when (sel) {
        SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(accentColorHex = hex))
        is SelectedElement.ExtraCta -> {
            val list = currentAd.extraCtas.map {
                if (it.id == sel.id) it.copy(colorHex = hex) else it
            }
            adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
        }
        SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleFrameColorHex = hex, titleHasFrame = true))
        SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentFrameColorHex = hex, contentHasFrame = true))
        is SelectedElement.ExtraText -> {
            val list = currentAd.extraTexts.map {
                if (it.id == sel.id) it.copy(frameColorHex = hex, frameStyle = "SOLID") else it
            }
            adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
        }
        else -> {}
    }
}

private fun deleteSelectedElement(
    sel: SelectedElement,
    currentAd: AdItem,
    adViewModel: AdViewModel,
    onSuccess: (SelectedElement) -> Unit
) {
    when (sel) {
        SelectedElement.Title -> {
            adViewModel.updateEditingAd(currentAd.copy(title = ""))
            onSuccess(SelectedElement.Content)
        }
        SelectedElement.Subtitle -> {
            adViewModel.updateEditingAd(currentAd.copy(subtitle = ""))
            onSuccess(SelectedElement.Title)
        }
        SelectedElement.Content -> {
            adViewModel.updateEditingAd(currentAd.copy(content = ""))
            onSuccess(SelectedElement.Title)
        }
        SelectedElement.Overlay -> {
            adViewModel.updateEditingAd(currentAd.copy(customOverlayText = ""))
            onSuccess(SelectedElement.Title)
        }
        SelectedElement.MainCta -> {
            adViewModel.updateEditingAd(currentAd.copy(ctaText = ""))
            onSuccess(SelectedElement.Title)
        }
        is SelectedElement.ExtraCta -> {
            val list = currentAd.extraCtas.filter { it.id != sel.id }
            adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
            onSuccess(SelectedElement.Title)
        }
        is SelectedElement.ExtraText -> {
            val list = currentAd.extraTexts.filter { it.id != sel.id }
            adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
            onSuccess(SelectedElement.Title)
        }
        is SelectedElement.ExtraImage -> {
            val list = currentAd.extraImages.filter { it.id != sel.id }
            adViewModel.updateEditingAd(currentAd.copy(extraImages = list))
            onSuccess(SelectedElement.Title)
        }
        else -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomColorPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colorGroups = listOf(
        "نئون و درخشان ⚡" to listOf("#00E5FF", "#FF007F", "#76FF03", "#FFEA00", "#00E676", "#D500F9", "#FF1744", "#00B0FF"),
        "گرم و طلایی 🌟" to listOf("#FFD700", "#F59E0B", "#FF8000", "#FF3D00", "#D4AF37", "#B78103", "#FF6F00", "#E65100"),
        "پاستلی و ملایم 🌸" to listOf("#E0F7FA", "#F3E5F5", "#FFF8E1", "#E8F5E9", "#FFEBEE", "#FFF3E0", "#F0F4C3", "#E1BEE7"),
        "کلاسیک و تیره 🖤" to listOf("#FFFFFF", "#E2E8F0", "#94A3B8", "#475569", "#1E293B", "#0F172A", "#1A1A1A", "#000000")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                colorGroups.forEach { (groupName, colorHexes) ->
                    Text(groupName, fontSize = 11.5.sp, color = Color.LightGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        colorHexes.forEach { hex ->
                            val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
                            Surface(
                                shape = CircleShape,
                                color = c,
                                border = BorderStroke(2.dp, GoldAccent.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable {
                                        onColorSelected(hex)
                                        onDismiss()
                                    }
                            ) {}
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AccordionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, if (isExpanded) GoldAccent else Color.White.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdManagementScreen(
    adViewModel: AdViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userRole by adViewModel.userRole.collectAsState()
    val allAds by adViewModel.allAds.collectAsState()
    val editingAd by adViewModel.editingAd.collectAsState()
    val loginError by adViewModel.loginError.collectAsState()

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var selectedItem by remember<androidx.compose.runtime.MutableState<SelectedElement>> {
        mutableStateOf(SelectedElement.Content)
    }
    var activeStudioTab by remember { mutableStateOf(StudioTab.TEXTS) }

    var activeTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var fontCategoryFilter by remember { mutableStateOf("همه") }
    var customHexInput by remember { mutableStateOf("#FFD700") }
    var customBgHexInput by remember { mutableStateOf("#F59E0B") }

    var activeColorToast by remember { mutableStateOf<String?>(null) }
    var multiLinkDialogCta by remember { mutableStateOf<StoryCtaLayer?>(null) }

    // Add Image Launcher
    val addExtraImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editingAd?.let { ad ->
                val savedUriStr = saveUriToAppGallery(context, it)
                addSavedUserImage(context, savedUriStr)
                val newLayer = StoryImageLayer(
                    id = UUID.randomUUID().toString(),
                    url = savedUriStr,
                    offsetX = 0f,
                    offsetY = 0f,
                    scale = 1.0f,
                    rotation = 0f,
                    opacity = 1.0f
                )
                val updatedImages = ad.extraImages + newLayer
                adViewModel.updateEditingAd(ad.copy(extraImages = updatedImages))
                selectedItem = SelectedElement.ExtraImage(newLayer.id)
                Toast.makeText(context, "عکس جدید به گالری و استوری اضافه شد", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Replace Main Background Launcher
    val mainBgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editingAd?.let { ad ->
                val savedUriStr = saveUriToAppGallery(context, it)
                addSavedUserImage(context, savedUriStr)
                adViewModel.updateEditingAd(ad.copy(bgImageUrl = savedUriStr))
                Toast.makeText(context, "تصویر پس‌زمینه اصلی تغییر یافت", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1610375461246-83df859d849d?auto=format&fit=crop&w=600&q=80" to "🪙 شمش طلا",
        "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?auto=format&fit=crop&w=600&q=80" to "💰 سکه طلا",
        "https://images.unsplash.com/photo-1580519542036-c47de6196ba5?auto=format&fit=crop&w=600&q=80" to "💵 دلار",
        "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=600&q=80" to "💎 الماس",
        "https://images.unsplash.com/photo-1589758438368-0ad531db3366?auto=format&fit=crop&w=600&q=80" to "🥇 مدال",
        "https://images.unsplash.com/photo-1557804506-669a67965ba0?auto=format&fit=crop&w=600&q=80" to "📢 بنر تبلیغاتی",
        "https://images.unsplash.com/photo-1560518883-ce09059eeffa?auto=format&fit=crop&w=600&q=80" to "🏢 ملک و املاک",
        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80" to "🚗 خودرو لوکس",
        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80" to "⌚ ساعت لاکچری",
        "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=600&q=80" to "🛍️ پیشنهاد ویژه",
        "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&w=600&q=80" to "📈 اسکناس و مالی",
        "https://images.unsplash.com/photo-1512428559087-560fa5ceab42?auto=format&fit=crop&w=600&q=80" to "📱 موبایل هوشمند"
    )

    val colorPalette = listOf(
        "#F59E0B" to "طلایی",
        "#10B981" to "زمردی",
        "#3B82F6" to "آبی نئون",
        "#8B5CF6" to "بنفش",
        "#EC4899" to "رزگلد",
        "#EF4444" to "قرمز",
        "#FFFFFF" to "سفید",
        "#000000" to "مشکی",
        "#FFD700" to "طلایی درخشان",
        "#00E5FF" to "فیروزه‌ای",
        "#76FF03" to "سبز فسفری",
        "#FF3D00" to "نارنجی آتشین"
    )

    val popularPersianFonts = listOf(
        "وزیرمتن (استاندارد)",
        "شبنم (مدرن)",
        "صمیم (نرم)",
        "لاله‌زار (تیتر)",
        "یکان (کلاسیک)",
        "ایران‌سنس",
        "تیتر (برجسته)",
        "نستعلیق (هنری)",
        "کودک (فانتزی)",
        "دست‌نویس"
    )

    val frameStyles = listOf(
        "NONE" to "بدون کادر 🚫",
        "GOLD_BORDER" to "کادر طلایی nEON 🌟",
        "SOLID" to "کادر مشکی مات 🔲",
        "GLASS" to "کادر گلاس بلور 💎",
        "NEON" to "کادر نیون آکوآ ⚡",
        "DARK_ROUND" to "کادر گرد تیره 🟣",
        "DASHED" to "کادر خط‌چین 📐"
    )

    val presetEmojis = listOf(
        "✨", "🪙", "💎", "💵", "👑", "🏆", "📍", "📞", "🌐", "🥇", "🚀", "📢", "💍", "📱", "💰",
        "🔥", "🎯", "📈", "🛍️", "⚡", "🔑", "🌟", "🏷️", "🎁", "🔴", "📊", "💳", "🏢", "🚗", "🏡", "📲", "⭐️"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // APP HEADER WITH PERSISTENT SAVE & PUBLISH BUTTON
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ادیتور حرفه‌ای استوری",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.5.sp,
                        color = GoldAccent
                    )
                    if (editingAd != null && userRole != UserRole.NONE) {
                        Text(
                            text = PriceParser.englishToPersianDigits("${editingAd?.title} (${editingAd?.remainingDays} روز اعتبار)"),
                            fontSize = 10.sp,
                            color = UpGreen
                        )
                    }
                }

                if (userRole != UserRole.NONE) {
                    // SAVE & PUBLISH BUTTON
                    Button(
                        onClick = {
                            adViewModel.saveEditingAd()
                            Toast.makeText(context, "✅ تنظیمات با موفقیت ذخیره و منتشر شد!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "ذخیره", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "ذخیره و انتشار", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }
        }

        if (userRole == UserRole.NONE) {
            // LOGIN SCREEN
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldAccent.copy(alpha = 0.18f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = "ورود", tint = GoldAccent, modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "ورود به ادیتور استوری آگهی", fontWeight = FontWeight.Bold, fontSize = 17.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("نام کاربری") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("رمز عبور") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent)
                        )

                        if (!loginError.isNullOrEmpty()) {
                            Text(text = loginError ?: "", color = Color.Red, fontSize = 11.5.sp, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (usernameInput.isNotBlank() && passwordInput.isNotBlank()) {
                                    adViewModel.loginWithCredentials(usernameInput, passwordInput)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "ورود به ادیتور", fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "💡 اکانت‌های سریع جهت تست:", fontSize = 11.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val demoAccounts = listOf(
                            "👑 پنل مدیریت" to ("admin" to "7788"),
                            "✨ زرین گلد" to ("zarrin" to "1001"),
                            "🪙 سکه پارسیان" to ("parsian" to "1002"),
                            "💵 صرافی آریا" to ("aria" to "1003")
                        )

                        demoAccounts.forEach { (label, creds) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.06f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        usernameInput = creds.first
                                        passwordInput = creds.second
                                        adViewModel.loginWithCredentials(creds.first, creds.second)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = label, fontSize = 11.5.sp)
                                    Text(text = "${creds.first} / ${creds.second}", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // LOGGED IN STUDIO EDITOR
            val currentAd = editingAd

            if (currentAd != null) {
                val parsedAccentColor = remember(currentAd.accentColorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(currentAd.accentColorHex))
                    } catch (e: Exception) {
                        GoldAccent
                    }
                }

                // Helper gesture transformer for any element
                val transformElement: (sel: SelectedElement, panX: Float, panY: Float, zoom: Float, rotationDegrees: Float) -> Unit = { targetSel, panX, panY, zoom, rotationDegrees ->
                    selectedItem = targetSel
                    if (panX == -9999f) {
                        // Reset transforms to center
                        when (targetSel) {
                            SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleOffsetX = 0f, titleOffsetY = 0f, titleScale = 1f, titleRotation = 0f))
                            SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(titleOffsetX = 0f, titleOffsetY = 0f, subtitleScale = 1f, subtitleRotation = 0f))
                            SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentOffsetX = 0f, contentOffsetY = 0f, contentScale = 1f, contentRotation = 0f))
                            SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(overlayOffsetX = 0f, overlayOffsetY = 0f, overlayRotation = 0))
                            SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(ctaOffsetX = 0f, ctaOffsetY = 0f, ctaScale = 1f, ctaRotation = 0f))
                            SelectedElement.MainBackground -> adViewModel.updateEditingAd(currentAd.copy(bgOffsetX = 0f, bgOffsetY = 0f, bgScale = 1f, cropRotation = 0))
                            is SelectedElement.ExtraImage -> {
                                val updated = currentAd.extraImages.map { if (it.id == targetSel.id) it.copy(offsetX = 0f, offsetY = 0f, scale = 1f, rotation = 0f) else it }
                                adViewModel.updateEditingAd(currentAd.copy(extraImages = updated))
                            }
                            is SelectedElement.ExtraCta -> {
                                val updated = currentAd.extraCtas.map { if (it.id == targetSel.id) it.copy(offsetX = 0f, offsetY = 0f, scale = 1f, rotation = 0f) else it }
                                adViewModel.updateEditingAd(currentAd.copy(extraCtas = updated))
                            }
                            is SelectedElement.ExtraText -> {
                                val updated = currentAd.extraTexts.map { if (it.id == targetSel.id) it.copy(offsetX = 0f, offsetY = 0f, scale = 1f, rotation = 0f) else it }
                                adViewModel.updateEditingAd(currentAd.copy(extraTexts = updated))
                            }
                        }
                    } else {
                        when (targetSel) {
                            SelectedElement.Title -> {
                                adViewModel.updateEditingAd(
                                    currentAd.copy(
                                        titleOffsetX = currentAd.titleOffsetX + panX,
                                        titleOffsetY = currentAd.titleOffsetY + panY,
                                        titleScale = (currentAd.titleScale * zoom).coerceIn(0.1f, 10f),
                                        titleRotation = currentAd.titleRotation + rotationDegrees
                                    )
                                )
                            }
                            SelectedElement.Subtitle -> {
                                adViewModel.updateEditingAd(
                                    currentAd.copy(
                                        titleOffsetX = currentAd.titleOffsetX + panX,
                                        titleOffsetY = currentAd.titleOffsetY + panY,
                                        subtitleScale = (currentAd.subtitleScale * zoom).coerceIn(0.1f, 10f),
                                        subtitleRotation = currentAd.subtitleRotation + rotationDegrees
                                    )
                                )
                            }
                            SelectedElement.Content -> {
                                adViewModel.updateEditingAd(
                                    currentAd.copy(
                                        contentOffsetX = currentAd.contentOffsetX + panX,
                                        contentOffsetY = currentAd.contentOffsetY + panY,
                                        contentScale = (currentAd.contentScale * zoom).coerceIn(0.1f, 10f),
                                        contentRotation = currentAd.contentRotation + rotationDegrees
                                    )
                                )
                            }
                            SelectedElement.Overlay -> {
                                adViewModel.updateEditingAd(
                                    currentAd.copy(
                                        overlayOffsetX = currentAd.overlayOffsetX + panX,
                                        overlayOffsetY = currentAd.overlayOffsetY + panY,
                                        overlayRotation = (currentAd.overlayRotation + rotationDegrees).toInt()
                                    )
                                )
                            }
                            SelectedElement.MainCta -> {
                                adViewModel.updateEditingAd(
                                    currentAd.copy(
                                        ctaOffsetX = currentAd.ctaOffsetX + panX,
                                        ctaOffsetY = currentAd.ctaOffsetY + panY,
                                        ctaScale = (currentAd.ctaScale * zoom).coerceIn(0.1f, 10f),
                                        ctaRotation = currentAd.ctaRotation + rotationDegrees
                                    )
                                )
                            }
                            SelectedElement.MainBackground -> {
                                adViewModel.updateEditingAd(
                                    currentAd.copy(
                                        bgOffsetX = currentAd.bgOffsetX + panX,
                                        bgOffsetY = currentAd.bgOffsetY + panY,
                                        bgScale = (currentAd.bgScale * zoom).coerceIn(0.1f, 10f),
                                        cropRotation = (currentAd.cropRotation + rotationDegrees).toInt()
                                    )
                                )
                            }
                            is SelectedElement.ExtraImage -> {
                                val updated = currentAd.extraImages.map {
                                    if (it.id == targetSel.id) {
                                        it.copy(
                                            offsetX = it.offsetX + panX,
                                            offsetY = it.offsetY + panY,
                                            scale = (it.scale * zoom).coerceIn(0.1f, 10f),
                                            rotation = it.rotation + rotationDegrees
                                        )
                                    } else it
                                }
                                adViewModel.updateEditingAd(currentAd.copy(extraImages = updated))
                            }
                            is SelectedElement.ExtraCta -> {
                                val updated = currentAd.extraCtas.map {
                                    if (it.id == targetSel.id) {
                                        it.copy(
                                            offsetX = it.offsetX + panX,
                                            offsetY = it.offsetY + panY,
                                            scale = (it.scale * zoom).coerceIn(0.1f, 10f),
                                            rotation = it.rotation + rotationDegrees
                                        )
                                    } else it
                                }
                                adViewModel.updateEditingAd(currentAd.copy(extraCtas = updated))
                            }
                            is SelectedElement.ExtraText -> {
                                val updated = currentAd.extraTexts.map {
                                    if (it.id == targetSel.id) {
                                        it.copy(
                                            offsetX = it.offsetX + panX,
                                            offsetY = it.offsetY + panY,
                                            scale = (it.scale * zoom).coerceIn(0.1f, 10f),
                                            rotation = it.rotation + rotationDegrees
                                        )
                                    } else it
                                }
                                adViewModel.updateEditingAd(currentAd.copy(extraTexts = updated))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    // 1. LIVE HIGH-PRECISION INSTAGRAM CANVAS WITH UNLIMITED GESTURE MOVEMENT (PAN, ZOOM, ROTATE)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.88f)),
                        border = BorderStroke(2.dp, parsedAccentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { selectedItem = SelectedElement.MainBackground }
                        ) {
                            val layerKeys = remember(currentAd.layerOrder, currentAd.extraImages, currentAd.extraTexts, currentAd.extraCtas) {
                                getOrderedLayerKeys(currentAd)
                            }

                            layerKeys.forEach { layerKey ->
                                when {
                                    layerKey == "bg" -> {
                                        AsyncImage(
                                            model = currentAd.bgImageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    translationX = currentAd.bgOffsetX
                                                    translationY = currentAd.bgOffsetY
                                                    scaleX = currentAd.bgScale
                                                    scaleY = currentAd.bgScale
                                                    rotationZ = currentAd.cropRotation.toFloat()
                                                }
                                                .border(
                                                    width = if (selectedItem == SelectedElement.MainBackground) 2.dp else 0.dp,
                                                    color = if (selectedItem == SelectedElement.MainBackground) GoldAccent else Color.Transparent
                                                )
                                                .storyElementTransformable(
                                                    elementKey = "bg",
                                                    onSelect = { selectedItem = SelectedElement.MainBackground },
                                                    onTransform = { panX, panY, zoom, rotation ->
                                                        transformElement(SelectedElement.MainBackground, panX, panY, zoom, rotation)
                                                    }
                                                ),
                                            alpha = currentAd.bgOpacity
                                        )
                                    }

                                    layerKey == "title" -> {
                                        val (titleFontFamily, titleFontWeight) = remember(currentAd.titleFont) { getPersianFontSpecs(currentAd.titleFont) }
                                        val titleCol = remember(currentAd.titleColorHex) {
                                            try { Color(android.graphics.Color.parseColor(currentAd.titleColorHex)) } catch (e: Exception) { Color.White }
                                        }
                                        val isTitleSelected = selectedItem == SelectedElement.Title || selectedItem == SelectedElement.Subtitle

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .graphicsLayer {
                                                    translationX = currentAd.titleOffsetX
                                                    translationY = currentAd.titleOffsetY + 8f
                                                    scaleX = currentAd.titleScale
                                                    scaleY = currentAd.titleScale
                                                    rotationZ = currentAd.titleRotation
                                                }
                                                .storyElementTransformable(
                                                    elementKey = "title",
                                                    onSelect = { selectedItem = SelectedElement.Title },
                                                    onTransform = { panX, panY, zoom, rotation ->
                                                        transformElement(SelectedElement.Title, panX, panY, zoom, rotation)
                                                    }
                                                )
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (currentAd.titleHasFrame) try { Color(android.graphics.Color.parseColor(currentAd.titleFrameColorHex)) } catch (e: Exception) { Color.Black.copy(alpha = 0.5f) }
                                                    else if (isTitleSelected) Color.Black.copy(alpha = 0.4f) else Color.Transparent
                                                )
                                                .border(
                                                    width = if (isTitleSelected) 1.5.dp else 0.dp,
                                                    color = GoldAccent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = parsedAccentColor.copy(alpha = 0.25f),
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.WorkspacePremium,
                                                                contentDescription = null,
                                                                tint = parsedAccentColor,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = PriceParser.englishToPersianDigits(currentAd.title),
                                                        fontWeight = titleFontWeight,
                                                        fontFamily = titleFontFamily,
                                                        fontSize = currentAd.titleFontSize.sp,
                                                        color = titleCol
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = parsedAccentColor.copy(alpha = 0.25f),
                                                    border = BorderStroke(1.dp, parsedAccentColor.copy(alpha = 0.6f))
                                                ) {
                                                    Text(
                                                        text = PriceParser.englishToPersianDigits(currentAd.subtitle),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = parsedAccentColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    layerKey == "content" -> {
                                        val (contentFontFamily, contentFontWeight) = remember(currentAd.contentFont) { getPersianFontSpecs(currentAd.contentFont) }
                                        val contentCol = remember(currentAd.contentColorHex) {
                                            try { Color(android.graphics.Color.parseColor(currentAd.contentColorHex)) } catch (e: Exception) { Color.White }
                                        }
                                        val isContentSelected = selectedItem == SelectedElement.Content

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .graphicsLayer {
                                                    translationX = currentAd.contentOffsetX
                                                    translationY = currentAd.contentOffsetY
                                                    scaleX = currentAd.contentScale
                                                    scaleY = currentAd.contentScale
                                                    rotationZ = currentAd.contentRotation
                                                }
                                                .storyElementTransformable(
                                                    elementKey = "content",
                                                    onSelect = { selectedItem = SelectedElement.Content },
                                                    onTransform = { panX, panY, zoom, rotation ->
                                                        transformElement(SelectedElement.Content, panX, panY, zoom, rotation)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (currentAd.contentHasFrame) try { Color(android.graphics.Color.parseColor(currentAd.contentFrameColorHex)) } catch (e: Exception) { Color.Black.copy(alpha = 0.5f) }
                                                    else if (isContentSelected) Color.Black.copy(alpha = 0.45f) else Color.Transparent
                                                )
                                                .border(
                                                    width = if (isContentSelected) 1.5.dp else 0.dp,
                                                    color = GoldAccent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = PriceParser.englishToPersianDigits(currentAd.content),
                                                fontSize = currentAd.contentFontSize.sp,
                                                fontFamily = contentFontFamily,
                                                fontWeight = contentFontWeight,
                                                color = contentCol,
                                                lineHeight = (currentAd.contentFontSize + 5).sp,
                                                textAlign = when (currentAd.contentAlign) {
                                                    "CENTER" -> TextAlign.Center
                                                    "LEFT" -> TextAlign.Left
                                                    else -> TextAlign.Right
                                                }
                                            )
                                        }
                                    }

                                    layerKey == "overlay" -> {
                                        if (currentAd.customOverlayText.isNotBlank()) {
                                            val (overlayFontFamily, overlayFontWeight) = remember(currentAd.overlayFont) { getPersianFontSpecs(currentAd.overlayFont) }
                                            val overlayCol = remember(currentAd.customOverlayColor) {
                                                try { Color(android.graphics.Color.parseColor(currentAd.customOverlayColor)) } catch (e: Exception) { GoldAccent }
                                            }
                                            val isOverlaySelected = selectedItem == SelectedElement.Overlay

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .graphicsLayer {
                                                        translationX = currentAd.overlayOffsetX
                                                        translationY = currentAd.overlayOffsetY - 40f
                                                        scaleX = 1f
                                                        scaleY = 1f
                                                        rotationZ = currentAd.overlayRotation.toFloat()
                                                        alpha = currentAd.overlayOpacity
                                                    }
                                                    .storyElementTransformable(
                                                        elementKey = "overlay",
                                                        onSelect = { selectedItem = SelectedElement.Overlay },
                                                        onTransform = { panX, panY, zoom, rotation ->
                                                            transformElement(SelectedElement.Overlay, panX, panY, zoom, rotation)
                                                        }
                                                    )
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (currentAd.overlayHasFrame) Color.Black.copy(alpha = 0.65f) else Color.Transparent
                                                    )
                                                    .border(
                                                        width = if (isOverlaySelected) 2.dp else 1.dp,
                                                        color = if (isOverlaySelected) GoldAccent else overlayCol,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = PriceParser.englishToPersianDigits(currentAd.customOverlayText),
                                                    fontSize = currentAd.customOverlaySize.sp,
                                                    fontFamily = overlayFontFamily,
                                                    fontWeight = overlayFontWeight,
                                                    color = overlayCol
                                                )
                                            }
                                        }
                                    }

                                    layerKey == "main_cta" -> {
                                        if (currentAd.ctaText.isNotBlank()) {
                                            val isMainCtaSelected = selectedItem == SelectedElement.MainCta
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .graphicsLayer {
                                                        translationX = currentAd.ctaOffsetX
                                                        translationY = currentAd.ctaOffsetY - 10f
                                                        scaleX = currentAd.ctaScale
                                                        scaleY = currentAd.ctaScale
                                                        rotationZ = currentAd.ctaRotation
                                                    }
                                                    .storyElementTransformable(
                                                        elementKey = "main_cta",
                                                        onSelect = { selectedItem = SelectedElement.MainCta },
                                                        onTransform = { panX, panY, zoom, rotation ->
                                                            transformElement(SelectedElement.MainCta, panX, panY, zoom, rotation)
                                                        }
                                                    )
                                                    .padding(4.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = parsedAccentColor,
                                                    border = BorderStroke(
                                                        width = if (isMainCtaSelected) 2.dp else 0.dp,
                                                        color = GoldAccent
                                                    )
                                                ) {
                                                    Text(
                                                        text = PriceParser.englishToPersianDigits(currentAd.ctaText),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    layerKey.startsWith("extra_text_") -> {
                                        val textId = layerKey.removePrefix("extra_text_")
                                        val textLayer = currentAd.extraTexts.find { it.id == textId }
                                        if (textLayer != null) {
                                            val isSelected = selectedItem == SelectedElement.ExtraText(textLayer.id)
                                            val (layerFont, layerWeight) = remember(textLayer.fontName) { getPersianFontSpecs(textLayer.fontName) }
                                            val layerCol = remember(textLayer.colorHex) {
                                                try { Color(android.graphics.Color.parseColor(textLayer.colorHex)) } catch (e: Exception) { Color.White }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .graphicsLayer {
                                                        translationX = textLayer.offsetX
                                                        translationY = textLayer.offsetY
                                                        scaleX = textLayer.scale
                                                        scaleY = textLayer.scale
                                                        rotationZ = textLayer.rotation
                                                        alpha = textLayer.opacity
                                                    }
                                                    .storyElementTransformable(
                                                        elementKey = "extra_text_${textLayer.id}",
                                                        onSelect = { selectedItem = SelectedElement.ExtraText(textLayer.id) },
                                                        onTransform = { panX, panY, zoom, rotation ->
                                                            transformElement(SelectedElement.ExtraText(textLayer.id), panX, panY, zoom, rotation)
                                                        }
                                                    )
                                                    .padding(4.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = when (textLayer.frameStyle) {
                                                        "SOLID" -> Color.Black.copy(alpha = 0.7f)
                                                        "GLASS" -> Color.White.copy(alpha = 0.18f)
                                                        "NEON" -> Color.Black.copy(alpha = 0.8f)
                                                        "DARK_ROUND" -> Color.Black.copy(alpha = 0.85f)
                                                        else -> Color.Transparent
                                                    },
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else if (textLayer.frameStyle != "NONE") 1.dp else 0.dp,
                                                        color = if (isSelected) GoldAccent else when (textLayer.frameStyle) {
                                                            "GOLD_BORDER" -> GoldAccent
                                                            "NEON" -> Color(0xFF00E5FF)
                                                            else -> layerCol.copy(alpha = 0.5f)
                                                        }
                                                    )
                                                ) {
                                                    Text(
                                                        text = PriceParser.englishToPersianDigits(textLayer.text),
                                                        fontSize = textLayer.fontSize.sp,
                                                        fontFamily = layerFont,
                                                        fontWeight = layerWeight,
                                                        color = layerCol,
                                                        textAlign = when (textLayer.align) {
                                                            "CENTER" -> TextAlign.Center
                                                            "LEFT" -> TextAlign.Left
                                                            else -> TextAlign.Right
                                                        },
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    layerKey.startsWith("extra_image_") -> {
                                        val imgId = layerKey.removePrefix("extra_image_")
                                        val imgLayer = currentAd.extraImages.find { it.id == imgId }
                                        if (imgLayer != null) {
                                            val isSelected = selectedItem == SelectedElement.ExtraImage(imgLayer.id)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .graphicsLayer {
                                                        translationX = imgLayer.offsetX
                                                        translationY = imgLayer.offsetY
                                                        scaleX = imgLayer.scale
                                                        scaleY = imgLayer.scale
                                                        rotationZ = imgLayer.rotation
                                                        alpha = imgLayer.opacity
                                                    }
                                                    .storyElementTransformable(
                                                        elementKey = "extra_image_${imgLayer.id}",
                                                        onSelect = { selectedItem = SelectedElement.ExtraImage(imgLayer.id) },
                                                        onTransform = { panX, panY, zoom, rotation ->
                                                            transformElement(SelectedElement.ExtraImage(imgLayer.id), panX, panY, zoom, rotation)
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isSelected) 2.dp else 0.dp,
                                                        color = if (isSelected) GoldAccent else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                            ) {
                                                AsyncImage(
                                                    model = imgLayer.url,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .size(85.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                )
                                            }
                                        }
                                    }

                                    layerKey.startsWith("extra_cta_") -> {
                                        val ctaId = layerKey.removePrefix("extra_cta_")
                                        val ctaLayer = currentAd.extraCtas.find { it.id == ctaId }
                                        if (ctaLayer != null) {
                                            val isSelected = selectedItem == SelectedElement.ExtraCta(ctaLayer.id)
                                            val ctaBg = remember(ctaLayer.colorHex) {
                                                try { Color(android.graphics.Color.parseColor(ctaLayer.colorHex)) } catch (e: Exception) { GoldAccent }
                                            }
                                            val ctaTxtCol = remember(ctaLayer.textColorHex) {
                                                try { Color(android.graphics.Color.parseColor(ctaLayer.textColorHex)) } catch (e: Exception) { Color.Black }
                                            }
                                            val (ctaFontFamily, ctaFontWeight) = remember(ctaLayer.fontName) { getPersianFontSpecs(ctaLayer.fontName) }

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .graphicsLayer {
                                                        translationX = ctaLayer.offsetX
                                                        translationY = ctaLayer.offsetY
                                                        scaleX = ctaLayer.scale
                                                        scaleY = ctaLayer.scale
                                                        rotationZ = ctaLayer.rotation
                                                    }
                                                    .storyElementTransformable(
                                                        elementKey = "extra_cta_${ctaLayer.id}",
                                                        onSelect = { selectedItem = SelectedElement.ExtraCta(ctaLayer.id) },
                                                        onTransform = { panX, panY, zoom, rotation ->
                                                            transformElement(SelectedElement.ExtraCta(ctaLayer.id), panX, panY, zoom, rotation)
                                                        }
                                                    )
                                                    .padding(4.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = ctaBg,
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 0.dp,
                                                        color = GoldAccent
                                                    )
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                                                        if (ctaLayer.subLinks.isNotEmpty()) {
                                                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = ctaTxtCol, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                        }
                                                        Text(
                                                            text = PriceParser.englishToPersianDigits(ctaLayer.text),
                                                            fontSize = 11.sp,
                                                            fontFamily = ctaFontFamily,
                                                            fontWeight = ctaFontWeight,
                                                            color = ctaTxtCol
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ACTIVE ITEM TAG ON CANVAS
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = when (selectedItem) {
                                        SelectedElement.Title -> "📍 عنوان"
                                        SelectedElement.Subtitle -> "📍 زیرعنوان"
                                        SelectedElement.Content -> "📍 توضیحات"
                                        SelectedElement.Overlay -> "📍 برچسب"
                                        SelectedElement.MainCta -> "📍 دکمه اصلی"
                                        SelectedElement.MainBackground -> "📍 پس‌زمینه"
                                        is SelectedElement.ExtraImage -> "📍 تصویر جزیی"
                                        is SelectedElement.ExtraCta -> "📍 دکمه لینک"
                                        is SelectedElement.ExtraText -> "📍 متن اضافه"
                                    },
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // DIRECT CONTROLLER BAR (D-PAD) FOR EXACT POSITIONING, ZOOM & ROTATION
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // D-Pad Directional Arrows
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("جهت:", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { transformElement(selectedItem, -15f, 0f, 1f, 0f) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "چپ", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { transformElement(selectedItem, 0f, -15f, 1f, 0f) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "بالا", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { transformElement(selectedItem, 0f, 15f, 1f, 0f) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "پایین", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { transformElement(selectedItem, 15f, 0f, 1f, 0f) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "راست", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Zoom In / Zoom Out Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = GoldAccent.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable { transformElement(selectedItem, 0f, 0f, 1.2f, 0f) }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Text("🔍+", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { transformElement(selectedItem, 0f, 0f, 0.83f, 0f) }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Text("🔍-", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { transformElement(selectedItem, 0f, 0f, 1f, 15f) }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Text("🔄", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Red.copy(alpha = 0.3f),
                                    modifier = Modifier.clickable { transformElement(selectedItem, -9999f, 0f, 1f, 0f) }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Text("🎯 مرکز", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. STUDIO NAVIGATION TABS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val tabs = listOf(
                            StudioTab.TEXTS to ("متن‌ها 📝"),
                            StudioTab.IMAGES to ("عکس‌ها 🖼️"),
                            StudioTab.LINKS to ("لینک‌ها 🔗"),
                            StudioTab.LAYERS to ("لایه‌ها 🥞"),
                            StudioTab.ADMIN to ("مدیریت ⚙️")
                        )

                        tabs.forEach { (tab, label) ->
                            val isSelected = activeStudioTab == tab
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = GoldAccent
                                ),
                                modifier = Modifier.clickable { activeStudioTab = tab }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Color Notification Toast Banner
                    AnimatedVisibility(visible = activeColorToast != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "✨ ${activeColorToast ?: ""}",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // 3. TAB CONTENT CONTROL PANELS
                    val tabScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(tabScrollState)
                            .padding(bottom = 24.dp)
                    ) {
                        when (activeStudioTab) {
                            StudioTab.TEXTS -> {
                                var isLayerSectionExpanded by remember { mutableStateOf(true) }
                                var isFontSectionExpanded by remember { mutableStateOf(false) }
                                var isColorSectionExpanded by remember { mutableStateOf(false) }
                                var isFrameSectionExpanded by remember { mutableStateOf(false) }
                                var isTransformSectionExpanded by remember { mutableStateOf(false) }

                                var showCustomColorPickerForText by remember { mutableStateOf(false) }
                                var showCustomColorPickerForBg by remember { mutableStateOf(false) }

                                if (showCustomColorPickerForText) {
                                    CustomColorPickerDialog(
                                        title = "انتخاب رنگ متن و آیکون 🎨",
                                        onDismiss = { showCustomColorPickerForText = false },
                                        onColorSelected = { selectedHex ->
                                            applyTextColor(selectedItem, selectedHex, currentAd, adViewModel)
                                            Toast.makeText(context, "رنگ متن تغییر یافت ✨", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                if (showCustomColorPickerForBg) {
                                    CustomColorPickerDialog(
                                        title = "انتخاب رنگ کادر و پس‌زمینه 🖌️",
                                        onDismiss = { showCustomColorPickerForBg = false },
                                        onColorSelected = { selectedHex ->
                                            applyBgColor(selectedItem, selectedHex, currentAd, adViewModel)
                                            Toast.makeText(context, "رنگ کادر / دکمه تغییر یافت ✨", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 1. SECTION 1: EDIT TEXT & LAYERS
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            AccordionHeader(
                                                title = "۱. لایه و متن (ویرایش کلامی و ایموجی)",
                                                icon = Icons.Default.TextFields,
                                                isExpanded = isLayerSectionExpanded,
                                                onToggle = { isLayerSectionExpanded = !isLayerSectionExpanded }
                                            )

                                            if (isLayerSectionExpanded) {
                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            val newTextLayer = StoryTextLayer(
                                                                id = UUID.randomUUID().toString(),
                                                                text = "متن جدید ✨",
                                                                fontName = "وزیرمتن (استاندارد)",
                                                                colorHex = "#FFFFFF",
                                                                fontSize = 14,
                                                                align = "RIGHT",
                                                                frameStyle = "NONE",
                                                                offsetX = 0f,
                                                                offsetY = 0f
                                                            )
                                                            val updatedList = currentAd.extraTexts + newTextLayer
                                                            adViewModel.updateEditingAd(currentAd.copy(extraTexts = updatedList))
                                                            selectedItem = SelectedElement.ExtraText(newTextLayer.id)
                                                            Toast.makeText(context, "متن جدید اضافه شد", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = "افزودن متن ➕", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            deleteSelectedElement(selectedItem, currentAd, adViewModel) { nextSel ->
                                                                selectedItem = nextSel
                                                                Toast.makeText(context, "لایه انتخابی حذف گردید 🗑️", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = "حذف لایه 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text(text = "💡 انتخاب لایه جهت ویرایش:", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))

                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    item {
                                                        val isSel = selectedItem == SelectedElement.Title
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.Title }
                                                        ) {
                                                            Text("عنوان اصلی", fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                    item {
                                                        val isSel = selectedItem == SelectedElement.Subtitle
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.Subtitle }
                                                        ) {
                                                            Text("زیرعنوان", fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                    item {
                                                        val isSel = selectedItem == SelectedElement.Content
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.Content }
                                                        ) {
                                                            Text("توضیحات اصلی", fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                    item {
                                                        val isSel = selectedItem == SelectedElement.Overlay
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.Overlay }
                                                        ) {
                                                            Text("برچسب اورلی", fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                    item {
                                                        val isSel = selectedItem == SelectedElement.MainCta
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.MainCta }
                                                        ) {
                                                            Text("🔘 دکمه اصلی", fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                    items(currentAd.extraCtas) { ctaLayer ->
                                                        val isSel = selectedItem == SelectedElement.ExtraCta(ctaLayer.id)
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.ExtraCta(ctaLayer.id) }
                                                        ) {
                                                            Text("🔘 " + ctaLayer.text.take(8), fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                    items(currentAd.extraTexts) { textLayer ->
                                                        val isSel = selectedItem == SelectedElement.ExtraText(textLayer.id)
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSel) GoldAccent else Color.White.copy(alpha = 0.1f),
                                                            modifier = Modifier.clickable { selectedItem = SelectedElement.ExtraText(textLayer.id) }
                                                        ) {
                                                            Text("📝 " + textLayer.text.take(8), fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                val currentText = when (val sel = selectedItem) {
                                                    SelectedElement.Title -> currentAd.title
                                                    SelectedElement.Subtitle -> currentAd.subtitle
                                                    SelectedElement.Content -> currentAd.content
                                                    SelectedElement.Overlay -> currentAd.customOverlayText
                                                    SelectedElement.MainCta -> currentAd.ctaText
                                                    is SelectedElement.ExtraCta -> currentAd.extraCtas.find { it.id == sel.id }?.text ?: ""
                                                    is SelectedElement.ExtraText -> currentAd.extraTexts.find { it.id == sel.id }?.text ?: ""
                                                    else -> currentAd.content
                                                }

                                                androidx.compose.runtime.LaunchedEffect(currentText, selectedItem) {
                                                    if (activeTextFieldValue.text != currentText) {
                                                        activeTextFieldValue = TextFieldValue(text = currentText, selection = TextRange(currentText.length))
                                                    }
                                                }

                                                val targetLabel = when (selectedItem) {
                                                    SelectedElement.Title -> "عنوان اصلی"
                                                    SelectedElement.Subtitle -> "زیرعنوان"
                                                    SelectedElement.Content -> "توضیحات اصلی"
                                                    SelectedElement.Overlay -> "برچسب اورلی"
                                                    SelectedElement.MainCta -> "متن دکمه اصلی"
                                                    is SelectedElement.ExtraCta -> "متن دکمه جانبی"
                                                    is SelectedElement.ExtraText -> "متن لایه جدید"
                                                    else -> "متن"
                                                }

                                                OutlinedTextField(
                                                    value = activeTextFieldValue,
                                                    onValueChange = { newVal ->
                                                        activeTextFieldValue = newVal
                                                        val updatedText = newVal.text
                                                        when (val sel = selectedItem) {
                                                            SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(title = updatedText))
                                                            SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(subtitle = updatedText))
                                                            SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(content = updatedText))
                                                            SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(customOverlayText = updatedText))
                                                            SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(ctaText = updatedText))
                                                            is SelectedElement.ExtraCta -> {
                                                                val list = currentAd.extraCtas.map {
                                                                    if (it.id == sel.id) it.copy(text = updatedText) else it
                                                                }
                                                                adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
                                                            }
                                                            is SelectedElement.ExtraText -> {
                                                                val list = currentAd.extraTexts.map {
                                                                    if (it.id == sel.id) it.copy(text = updatedText) else it
                                                                }
                                                                adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
                                                            }
                                                            else -> {}
                                                        }
                                                    },
                                                    label = { Text("ویرایش $targetLabel") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent)
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                    Text("ایموجی‌های ترند (درج در موقعیت نشانگر):", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))

                                                FlowRow(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    presetEmojis.forEach { emoji ->
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color.White.copy(alpha = 0.08f),
                                                            modifier = Modifier.clickable {
                                                                val text = activeTextFieldValue.text
                                                                val cursorPos = activeTextFieldValue.selection.start.coerceIn(0, text.length)
                                                                val updatedText = text.substring(0, cursorPos) + emoji + text.substring(cursorPos)
                                                                val newCursor = cursorPos + emoji.length
                                                                activeTextFieldValue = TextFieldValue(text = updatedText, selection = TextRange(newCursor))

                                                                when (val sel = selectedItem) {
                                                                    SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(title = updatedText))
                                                                    SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(subtitle = updatedText))
                                                                    SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(content = updatedText))
                                                                    SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(customOverlayText = updatedText))
                                                                    SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(ctaText = updatedText))
                                                                    is SelectedElement.ExtraCta -> {
                                                                        val list = currentAd.extraCtas.map {
                                                                            if (it.id == sel.id) it.copy(text = updatedText) else it
                                                                        }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
                                                                    }
                                                                    is SelectedElement.ExtraText -> {
                                                                        val list = currentAd.extraTexts.map {
                                                                            if (it.id == sel.id) it.copy(text = updatedText) else it
                                                                        }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
                                                                    }
                                                                    else -> {}
                                                                }
                                                            }
                                                        ) {
                                                            Text(text = emoji, fontSize = 17.sp, modifier = Modifier.padding(5.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. SECTION 2: FONT & SIZE
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            AccordionHeader(
                                                title = "۲. تنظیمات فونت و ابعاد متن/دکمه",
                                                icon = Icons.Default.FormatSize,
                                                isExpanded = isFontSectionExpanded,
                                                onToggle = { isFontSectionExpanded = !isFontSectionExpanded }
                                            )

                                            if (isFontSectionExpanded) {
                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("سایز فونت / مقیاس:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    val currentSizeValue: Float = when (val sel = selectedItem) {
                                                        SelectedElement.Title -> currentAd.titleFontSize.toFloat()
                                                        SelectedElement.Subtitle -> currentAd.titleFontSize.toFloat()
                                                        SelectedElement.Content -> currentAd.contentFontSize.toFloat()
                                                        SelectedElement.Overlay -> currentAd.customOverlaySize.toFloat()
                                                        SelectedElement.MainCta -> (11f * currentAd.ctaScale)
                                                        is SelectedElement.ExtraCta -> {
                                                            val cta = currentAd.extraCtas.find { it.id == sel.id }
                                                            (11f * (cta?.scale ?: 1f))
                                                        }
                                                        is SelectedElement.ExtraText -> {
                                                            val txt = currentAd.extraTexts.find { it.id == sel.id }
                                                            (txt?.fontSize?.toFloat() ?: 14f)
                                                        }
                                                        else -> 12f
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            val newSize = (currentSizeValue - 1f).coerceAtLeast(8f)
                                                            updateFontSize(selectedItem, newSize, currentAd, adViewModel)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                                    }

                                                    Slider(
                                                        value = currentSizeValue,
                                                        onValueChange = { valSize ->
                                                            updateFontSize(selectedItem, valSize, currentAd, adViewModel)
                                                        },
                                                        valueRange = 8f..36f,
                                                        colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent),
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    IconButton(
                                                        onClick = {
                                                            val newSize = (currentSizeValue + 1f).coerceAtMost(36f)
                                                            updateFontSize(selectedItem, newSize, currentAd, adViewModel)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                                    }

                                                    Text("${currentSizeValue.toInt()} sp", fontSize = 11.sp, color = GoldAccent, modifier = Modifier.padding(start = 4.dp))
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text("انتخاب نوع فونت (اعمال مستقیم رو تمامی لایه‌ها و دکمه‌ها):", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(6.dp))

                                                val fontCategories = listOf(
                                                    "همه" to popularPersianFonts,
                                                    "رسمی و خوانا" to listOf("وزیرمتن (استاندارد)", "ایران‌یکان (مدرن)", "شبنم (رسمی)", "ساحل (خوانا)"),
                                                    "تیتر و بولد" to listOf("لاله‌زار (تیتر بولد)", "تیتر (کلاسیک)", "یکان بوم (تبلیغاتی)", "اردیبهشت"),
                                                    "فانتزی و دست‌نویس" to listOf("دست‌نویس (صمیمانه)", "ایران‌نستعلیق (هنری)", "کودک (فانتزی)", "پرستو")
                                                )

                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    items(fontCategories) { (catName, _) ->
                                                        val isCatSel = fontCategoryFilter == catName
                                                        Surface(
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = if (isCatSel) GoldAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                            border = BorderStroke(1.dp, if (isCatSel) GoldAccent else Color.Transparent),
                                                            modifier = Modifier.clickable { fontCategoryFilter = catName }
                                                        ) {
                                                            Text(text = catName, fontSize = 10.5.sp, color = if (isCatSel) GoldAccent else Color.LightGray, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                val activeFontList = fontCategories.find { it.first == fontCategoryFilter }?.second ?: popularPersianFonts

                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    items(activeFontList) { fName ->
                                                        val currentFont = when (val sel = selectedItem) {
                                                            SelectedElement.Title -> currentAd.titleFont
                                                            SelectedElement.Subtitle -> currentAd.subtitleFont
                                                            SelectedElement.Content -> currentAd.contentFont
                                                            SelectedElement.Overlay -> currentAd.overlayFont
                                                            SelectedElement.MainCta -> currentAd.ctaFont
                                                            is SelectedElement.ExtraCta -> currentAd.extraCtas.find { it.id == sel.id }?.fontName ?: "وزیرمتن (استاندارد)"
                                                            is SelectedElement.ExtraText -> currentAd.extraTexts.find { it.id == sel.id }?.fontName ?: "وزیرمتن (استاندارد)"
                                                            else -> currentAd.contentFont
                                                        }
                                                        val isSelected = currentFont == fName
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.08f),
                                                            modifier = Modifier.clickable {
                                                                when (val sel = selectedItem) {
                                                                    SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleFont = fName))
                                                                    SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(subtitleFont = fName))
                                                                    SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentFont = fName))
                                                                    SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(overlayFont = fName))
                                                                    SelectedElement.MainCta -> adViewModel.updateEditingAd(currentAd.copy(ctaFont = fName))
                                                                    is SelectedElement.ExtraCta -> {
                                                                        val list = currentAd.extraCtas.map {
                                                                            if (it.id == sel.id) it.copy(fontName = fName) else it
                                                                        }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
                                                                    }
                                                                    is SelectedElement.ExtraText -> {
                                                                        val list = currentAd.extraTexts.map {
                                                                            if (it.id == sel.id) it.copy(fontName = fName) else it
                                                                        }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
                                                                    }
                                                                    else -> {}
                                                                }
                                                            }
                                                        ) {
                                                            Text(text = fName, fontSize = 11.sp, color = if (isSelected) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 3. SECTION 3: COLOR PALETTE & CIRCLE COLOR PICKER
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            AccordionHeader(
                                                title = "۳. پالت رنگ (متن، پس‌زمینه و دکمه‌ها)",
                                                icon = Icons.Default.Palette,
                                                isExpanded = isColorSectionExpanded,
                                                onToggle = { isColorSectionExpanded = !isColorSectionExpanded }
                                            )

                                            if (isColorSectionExpanded) {
                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text("رنگ متن / آیکون:", fontSize = 11.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    LazyRow(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        items(colorPalette) { (hex, name) ->
                                                            val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = parsedColor,
                                                                border = BorderStroke(2.dp, GoldAccent),
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clickable {
                                                                        applyTextColor(selectedItem, hex, currentAd, adViewModel)
                                                                    }
                                                            ) {}
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color.Transparent,
                                                        border = BorderStroke(2.dp, GoldAccent),
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clickable { showCustomColorPickerForText = true }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("🌈", fontSize = 16.sp)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Text("رنگ کادر / پس‌زمینه (دکمه‌ها و کادر متن):", fontSize = 11.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    LazyRow(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        items(colorPalette) { (hex, name) ->
                                                            val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Black }
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = parsedColor,
                                                                border = BorderStroke(1.5.dp, Color.White),
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clickable {
                                                                        applyBgColor(selectedItem, hex, currentAd, adViewModel)
                                                                    }
                                                            ) {}
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color.Transparent,
                                                        border = BorderStroke(2.dp, GoldAccent),
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clickable { showCustomColorPickerForBg = true }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("🌈", fontSize = 16.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 4. SECTION 4: FRAME STYLES & BORDERS
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            AccordionHeader(
                                                title = "۴. استایل کادر، حاشیه و خطوط دور (نقطه‌چین/راه راه)",
                                                icon = Icons.Default.FormatColorFill,
                                                isExpanded = isFrameSectionExpanded,
                                                onToggle = { isFrameSectionExpanded = !isFrameSectionExpanded }
                                            )

                                            if (isFrameSectionExpanded) {
                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text("انتخاب استایل کادر و خط دور لایه انتخابی:", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(6.dp))

                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    frameStyles.forEach { (frameKey, frameLabel) ->
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color.White.copy(alpha = 0.08f),
                                                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                                                            modifier = Modifier.clickable {
                                                                when (val sel = selectedItem) {
                                                                    SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleHasFrame = (frameKey != "NONE")))
                                                                    SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentHasFrame = (frameKey != "NONE")))
                                                                    SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(overlayHasFrame = (frameKey != "NONE")))
                                                                    is SelectedElement.ExtraText -> {
                                                                        val list = currentAd.extraTexts.map {
                                                                            if (it.id == sel.id) it.copy(frameStyle = frameKey) else it
                                                                        }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
                                                                    }
                                                                    else -> {}
                                                                }
                                                                Toast.makeText(context, "استایل $frameLabel اعمال شد", Toast.LENGTH_SHORT).show()
                                                            }
                                                        ) {
                                                            Text(text = frameLabel, fontSize = 10.5.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 5. SECTION 5: ALIGNMENT & TRANSFORM
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            AccordionHeader(
                                                title = "۵. چیدمان و تنظیم موقعیت لایه",
                                                icon = Icons.Default.FormatAlignCenter,
                                                isExpanded = isTransformSectionExpanded,
                                                onToggle = { isTransformSectionExpanded = !isTransformSectionExpanded }
                                            )

                                            if (isTransformSectionExpanded) {
                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("چیدمان متن:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        val aligns = listOf("RIGHT" to " راست‌چین ➡️", "CENTER" to " وسط‌چین ↔️", "LEFT" to " چپ‌چین ⬅️")
                                                        aligns.forEach { (alignKey, alignLabel) ->
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = Color.White.copy(alpha = 0.08f),
                                                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
                                                                modifier = Modifier.clickable {
                                                                    when (val sel = selectedItem) {
                                                                        SelectedElement.Title -> adViewModel.updateEditingAd(currentAd.copy(titleAlign = alignKey))
                                                                        SelectedElement.Subtitle -> adViewModel.updateEditingAd(currentAd.copy(subtitleAlign = alignKey))
                                                                        SelectedElement.Content -> adViewModel.updateEditingAd(currentAd.copy(contentAlign = alignKey))
                                                                        SelectedElement.Overlay -> adViewModel.updateEditingAd(currentAd.copy(overlayAlign = alignKey))
                                                                        is SelectedElement.ExtraText -> {
                                                                            val list = currentAd.extraTexts.map {
                                                                                if (it.id == sel.id) it.copy(align = alignKey) else it
                                                                            }
                                                                            adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
                                                                        }
                                                                        else -> {}
                                                                    }
                                                                }
                                                            ) {
                                                                Text(alignLabel, fontSize = 10.5.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            StudioTab.IMAGES -> {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Button(
                                                onClick = { addExtraImageLauncher.launch("image/*") },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("افزودن عکس جدید 🖼️", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { mainBgLauncher.launch("image/*") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("پس‌زمینه اصلی 🎨", fontSize = 11.sp, color = Color.White)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // OPACITY CONTROL SLIDER FOR SELECTED IMAGE
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("شفافیت تصویر انتخابی:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))

                                            val currentImgOpacity = when (val sel = selectedItem) {
                                                SelectedElement.MainBackground -> currentAd.bgOpacity
                                                is SelectedElement.ExtraImage -> currentAd.extraImages.find { it.id == sel.id }?.opacity ?: 1.0f
                                                else -> 1.0f
                                            }

                                            Slider(
                                                value = currentImgOpacity,
                                                onValueChange = { valAlpha ->
                                                    when (val sel = selectedItem) {
                                                        SelectedElement.MainBackground -> adViewModel.updateEditingAd(currentAd.copy(bgOpacity = valAlpha))
                                                        is SelectedElement.ExtraImage -> {
                                                            val updatedList = currentAd.extraImages.map {
                                                                if (it.id == sel.id) it.copy(opacity = valAlpha) else it
                                                            }
                                                            adViewModel.updateEditingAd(currentAd.copy(extraImages = updatedList))
                                                        }
                                                        else -> {}
                                                    }
                                                },
                                                valueRange = 0.05f..1.0f,
                                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text("گالری تصاویر پیشنهادی کامل:", fontSize = 11.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            presetImages.forEach { (url, label) ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.White.copy(alpha = 0.08f),
                                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
                                                    modifier = Modifier.width(105.dp).height(85.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.BottomCenter) {
                                                        AsyncImage(model = url, contentDescription = label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color.Black.copy(alpha = 0.82f))
                                                                .padding(2.dp)
                                                        ) {
                                                            Text(text = label, fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                            Row(
                                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Text(
                                                                    text = "🎨 بک‌گراند",
                                                                    fontSize = 8.sp,
                                                                    color = GoldAccent,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.clickable {
                                                                        adViewModel.updateEditingAd(currentAd.copy(bgImageUrl = url))
                                                                        selectedItem = SelectedElement.MainBackground
                                                                        Toast.makeText(context, "پس‌زمینه تغییر کرد به $label", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                )
                                                                Text(
                                                                    text = "🖼️ لایه",
                                                                    fontSize = 8.sp,
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.clickable {
                                                                        val newLayer = StoryImageLayer(
                                                                            id = UUID.randomUUID().toString(),
                                                                            url = url,
                                                                            offsetX = 0f,
                                                                            offsetY = 0f,
                                                                            scale = 1.0f
                                                                        )
                                                                        val updatedImages = currentAd.extraImages + newLayer
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraImages = updatedImages))
                                                                        selectedItem = SelectedElement.ExtraImage(newLayer.id)
                                                                        Toast.makeText(context, "تصویر $label اضافه شد", Toast.LENGTH_SHORT).show()
                                                                    }
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

                            StudioTab.LINKS -> {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Button(
                                            onClick = {
                                                val newCta = StoryCtaLayer(
                                                    id = UUID.randomUUID().toString(),
                                                    text = "📞 تماس با ما",
                                                    url = "tel:09120000000",
                                                    subLinks = listOf(
                                                        StorySubLink(title = "تماس تلفنی", url = "tel:09120000000", iconType = "PHONE"),
                                                        StorySubLink(title = "ارتباط در واتساپ", url = "https://wa.me/989120000000", iconType = "WHATSAPP")
                                                    )
                                                )
                                                val updatedCtas = currentAd.extraCtas + newCta
                                                adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedCtas))
                                                selectedItem = SelectedElement.ExtraCta(newCta.id)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("افزودن دکمه چند لینکه جدید 🔗", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text("تنظیمات دکمه اصلی آگهی:", fontSize = 11.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = currentAd.ctaText,
                                            onValueChange = { adViewModel.updateEditingAd(currentAd.copy(ctaText = it)) },
                                            label = { Text("عنوان دکمه اصلی آگهی") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = currentAd.ctaUrl,
                                            onValueChange = { adViewModel.updateEditingAd(currentAd.copy(ctaUrl = it)) },
                                            label = { Text("لینک/شماره اصلی (tel: یا https://)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text("ویرایش دکمه‌های چند لینکه و زیر‌لینک‌ها:", fontSize = 11.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        currentAd.extraCtas.forEach { ctaLayer ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("دکمه: ${ctaLayer.text}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldAccent)
                                                        IconButton(
                                                            onClick = {
                                                                val updatedList = currentAd.extraCtas.filter { it.id != ctaLayer.id }
                                                                adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف دکمه", tint = Color.Red, modifier = Modifier.size(18.dp))
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    OutlinedTextField(
                                                        value = ctaLayer.text,
                                                        onValueChange = { newTxt ->
                                                            val updatedList = currentAd.extraCtas.map { if (it.id == ctaLayer.id) it.copy(text = newTxt) else it }
                                                            adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                        },
                                                        label = { Text("عنوان روی دکمه") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    OutlinedTextField(
                                                        value = ctaLayer.url,
                                                        onValueChange = { newUrl ->
                                                            val updatedList = currentAd.extraCtas.map { if (it.id == ctaLayer.id) it.copy(url = newUrl) else it }
                                                            adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                        },
                                                        label = { Text("لینک/شماره پیش‌فرض دکمه") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text("زیر‌لینک‌های متصل به این دکمه:", fontSize = 11.sp, color = GoldAccent)
                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    ctaLayer.subLinks.forEachIndexed { subIdx, sub ->
                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                                        ) {
                                                            Column(modifier = Modifier.padding(6.dp)) {
                                                                Row(
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.fillMaxWidth()
                                                                ) {
                                                                    Text("زیرلینک ${subIdx + 1}", fontSize = 10.5.sp, color = Color.LightGray)
                                                                    IconButton(
                                                                        onClick = {
                                                                            val newSubs = ctaLayer.subLinks.filterIndexed { i, _ -> i != subIdx }
                                                                            val updatedList = currentAd.extraCtas.map { if (it.id == ctaLayer.id) it.copy(subLinks = newSubs) else it }
                                                                            adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                                        },
                                                                        modifier = Modifier.size(20.dp)
                                                                    ) {
                                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                                                    }
                                                                }
                                                                OutlinedTextField(
                                                                    value = sub.title,
                                                                    onValueChange = { newTitle ->
                                                                        val newSubs = ctaLayer.subLinks.mapIndexed { i, s -> if (i == subIdx) s.copy(title = newTitle) else s }
                                                                        val updatedList = currentAd.extraCtas.map { if (it.id == ctaLayer.id) it.copy(subLinks = newSubs) else it }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                                    },
                                                                    label = { Text("عنوان (مثلاً: پشتیبانی تلگرام)") },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                OutlinedTextField(
                                                                    value = sub.url,
                                                                    onValueChange = { newSubUrl ->
                                                                        val newSubs = ctaLayer.subLinks.mapIndexed { i, s -> if (i == subIdx) s.copy(url = newSubUrl) else s }
                                                                        val updatedList = currentAd.extraCtas.map { if (it.id == ctaLayer.id) it.copy(subLinks = newSubs) else it }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                                    },
                                                                    label = { Text("آدرس لینک یا شماره") },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Button(
                                                        onClick = {
                                                            val newSub = StorySubLink(title = "لینک جدید", url = "https://", iconType = "WEBSITE")
                                                            val newSubs = ctaLayer.subLinks + newSub
                                                            val updatedList = currentAd.extraCtas.map { if (it.id == ctaLayer.id) it.copy(subLinks = newSubs) else it }
                                                            adViewModel.updateEditingAd(currentAd.copy(extraCtas = updatedList))
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("افزودن لینک زیرمجموعه جدید ➕", fontSize = 10.5.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            StudioTab.LAYERS -> {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("مدیریت لایه‌های استوری (ترتیب و چیدمان فتوشاپ):", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("💡 لایه‌های بالاتر روی لایه‌های پایین‌تر قرار می‌گیرند.", fontSize = 10.5.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        val orderedKeys = remember(currentAd.layerOrder, currentAd.extraImages, currentAd.extraTexts, currentAd.extraCtas) {
                                            getOrderedLayerKeys(currentAd)
                                        }

                                        // Display list from Topmost (last index) to Bottommost (index 0)
                                        val displayList = remember(orderedKeys) {
                                            orderedKeys.reversed()
                                        }

                                        fun moveLayerUpInZ(key: String) {
                                            val list = orderedKeys.toMutableList()
                                            val idx = list.indexOf(key)
                                            if (idx < list.size - 1 && idx >= 0) {
                                                val temp = list[idx]
                                                list[idx] = list[idx + 1]
                                                list[idx + 1] = temp
                                                adViewModel.updateEditingAd(currentAd.copy(layerOrder = list))
                                            }
                                        }

                                        fun moveLayerDownInZ(key: String) {
                                            val list = orderedKeys.toMutableList()
                                            val idx = list.indexOf(key)
                                            if (idx > 0) {
                                                val temp = list[idx]
                                                list[idx] = list[idx - 1]
                                                list[idx - 1] = temp
                                                adViewModel.updateEditingAd(currentAd.copy(layerOrder = list))
                                            }
                                        }

                                        displayList.forEachIndexed { displayIdx, key ->
                                            val (title, selTarget, isDeletable) = when {
                                                key == "bg" -> Triple("🖼️ تصویر پس‌زمینه اصلی", SelectedElement.MainBackground, false)
                                                key == "title" -> Triple("👑 عنوان و زیرعنوان اصلی", SelectedElement.Title, false)
                                                key == "content" -> Triple("📝 متن توضیحات اصلی", SelectedElement.Content, false)
                                                key == "overlay" -> Triple("🏷️ برچسب استیکر", SelectedElement.Overlay, false)
                                                key == "main_cta" -> Triple("🔘 دکمه اصلی آگهی", SelectedElement.MainCta, false)
                                                key.startsWith("extra_text_") -> {
                                                    val textId = key.removePrefix("extra_text_")
                                                    val item = currentAd.extraTexts.find { it.id == textId }
                                                    Triple("📝 متن: ${item?.text?.take(12) ?: ""}", SelectedElement.ExtraText(textId), true)
                                                }
                                                key.startsWith("extra_image_") -> {
                                                    val imgId = key.removePrefix("extra_image_")
                                                    Triple("🖼️ عکس جزیی", SelectedElement.ExtraImage(imgId), true)
                                                }
                                                key.startsWith("extra_cta_") -> {
                                                    val ctaId = key.removePrefix("extra_cta_")
                                                    val item = currentAd.extraCtas.find { it.id == ctaId }
                                                    Triple("🔘 دکمه: ${item?.text ?: ""}", SelectedElement.ExtraCta(ctaId), true)
                                                }
                                                else -> Triple("لایه $key", SelectedElement.MainBackground, false)
                                            }

                                            val isSelected = selectedItem == selTarget

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) GoldAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                                                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.Transparent),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clickable { selectedItem = selTarget }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = if (isSelected) GoldAccent else Color.Gray, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(title, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(
                                                            onClick = { moveLayerUpInZ(key) },
                                                            enabled = displayIdx > 0,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowUpward,
                                                                contentDescription = "جلوتر",
                                                                tint = if (displayIdx > 0) GoldAccent else Color.Gray.copy(alpha = 0.3f),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = { moveLayerDownInZ(key) },
                                                            enabled = displayIdx < displayList.size - 1,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowDownward,
                                                                contentDescription = "عقب‌تر",
                                                                tint = if (displayIdx < displayList.size - 1) GoldAccent else Color.Gray.copy(alpha = 0.3f),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        if (isDeletable) {
                                                            IconButton(
                                                                onClick = {
                                                                    if (key.startsWith("extra_text_")) {
                                                                        val textId = key.removePrefix("extra_text_")
                                                                        val list = currentAd.extraTexts.filter { it.id != textId }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraTexts = list))
                                                                    } else if (key.startsWith("extra_image_")) {
                                                                        val imgId = key.removePrefix("extra_image_")
                                                                        val list = currentAd.extraImages.filter { it.id != imgId }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraImages = list))
                                                                    } else if (key.startsWith("extra_cta_")) {
                                                                        val ctaId = key.removePrefix("extra_cta_")
                                                                        val list = currentAd.extraCtas.filter { it.id != ctaId }
                                                                        adViewModel.updateEditingAd(currentAd.copy(extraCtas = list))
                                                                    }
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            StudioTab.ADMIN -> {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("تنظیمات حساب و آگهی‌ها:", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("وضعیت انتشار آگهی در اپلیکیشن:", fontSize = 11.5.sp)
                                            Switch(
                                                checked = currentAd.isActive,
                                                onCheckedChange = { adViewModel.updateEditingAd(currentAd.copy(isActive = it)) },
                                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (userRole == UserRole.ADMIN) {
                                            Text("اعتبار باقی‌مانده (روز): ${PriceParser.englishToPersianDigits(currentAd.remainingDays.toString())}", fontSize = 11.sp)
                                            Slider(
                                                value = currentAd.remainingDays.toFloat(),
                                                onValueChange = { adViewModel.updateEditingAd(currentAd.copy(remainingDays = it.toInt())) },
                                                valueRange = 1f..365f,
                                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                                            )
                                        } else {
                                            Text(
                                                "اعتبار باقی‌مانده اشتراک شما: ${PriceParser.englishToPersianDigits(currentAd.remainingDays.toString())} روز (تنظیم فقط توسط مدیریت سیستم)",
                                                fontSize = 11.sp,
                                                color = Color.LightGray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = { adViewModel.logout() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("خروج از اکانت", fontWeight = FontWeight.Bold, color = Color.White)
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
