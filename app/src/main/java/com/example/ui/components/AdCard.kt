package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.StoryCtaLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AdItem
import com.example.data.model.PriceParser
import com.example.data.repository.AdRepository
import com.example.ui.screens.getOrderedLayerKeys
import com.example.ui.screens.getPersianFontSpecs
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.AdViewModel

@Composable
fun AdCard(
    adText: String = "",
    adViewModel: AdViewModel? = null,
    onNavigateToAdManagement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { AdRepository(context) }

    val viewModelAdsState = adViewModel?.allAds?.collectAsState()
    val repoAdsState = repository.adsFlow.collectAsState()
    val rawAds = viewModelAdsState?.value ?: repoAdsState.value

    val itemsToDisplay = remember(rawAds, adText) {
        val activeItems = rawAds.filter { it.isActive }
        if (activeItems.isNotEmpty()) activeItems else repository.getDefaultAds()
    }

    val pagerState = rememberPagerState(pageCount = { itemsToDisplay.size })

    // Automatic swipe loop every 6 seconds
    LaunchedEffect(pagerState, itemsToDisplay.size) {
        while (true) {
            kotlinx.coroutines.delay(6000L)
            if (itemsToDisplay.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % itemsToDisplay.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 800)
                )
            }
        }
    }

    val currentAd = itemsToDisplay.getOrNull(pagerState.currentPage)
    val parsedAccent = remember(currentAd?.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(currentAd?.accentColorHex ?: "#F59E0B")) }
        catch (e: Exception) { GoldAccent }
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        specialBrush = Brush.linearGradient(
            colors = listOf(
                parsedAccent.copy(alpha = 0.28f),
                parsedAccent.copy(alpha = 0.12f)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Banner: "📢 اینجا محل تبلیغات شماست!" + Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(text = "📢", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentAd != null && currentAd.title.isNotBlank()) PriceParser.englishToPersianDigits(currentAd.title) else "اینجا محل تبلیغات شماست!",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (currentAd != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = parsedAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, parsedAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = PriceParser.englishToPersianDigits(currentAd.subtitle),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = parsedAccent,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Swipeable / Scrollable Horizontal Pager rendering real Story layers
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) { page ->
                val ad = itemsToDisplay[page]
                AdItemStoryCardView(ad = ad, onCtaClick = { url ->
                    if (url.isNotBlank()) {
                        try {
                            val raw = url.trim()
                            val fullUrl = if (raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("tel:")) raw else "https://$raw"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) { }
                    }
                })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slide Page Indicator Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsToDisplay.indices.forEach { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) parsedAccent
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AdItemStoryCardView(
    ad: AdItem,
    onCtaClick: (String) -> Unit
) {
    val context = LocalContext.current
    var multiLinkDialogCta by remember { mutableStateOf<StoryCtaLayer?>(null) }

    val parsedAccentColor = remember(ad.accentColorHex) {
        try { Color(android.graphics.Color.parseColor(ad.accentColorHex)) } catch (e: Exception) { GoldAccent }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.88f))
            .border(1.dp, parsedAccentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        val layerKeys = remember(ad.layerOrder, ad.extraImages, ad.extraTexts, ad.extraCtas) {
            getOrderedLayerKeys(ad)
        }

        layerKeys.forEach { layerKey ->
            when {
                layerKey == "bg" -> {
                    AsyncImage(
                        model = ad.bgImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = ad.bgOffsetX
                                translationY = ad.bgOffsetY
                                scaleX = ad.bgScale
                                scaleY = ad.bgScale
                                rotationZ = ad.cropRotation.toFloat()
                                alpha = ad.bgOpacity
                            }
                    )
                }

                layerKey == "title" -> {
                    val (titleFontFamily, titleFontWeight) = remember(ad.titleFont) { getPersianFontSpecs(ad.titleFont) }
                    val titleCol = remember(ad.titleColorHex) {
                        try { Color(android.graphics.Color.parseColor(ad.titleColorHex)) } catch (e: Exception) { Color.White }
                    }
                    val (subFontFamily, subFontWeight) = remember(ad.subtitleFont) { getPersianFontSpecs(ad.subtitleFont) }
                    val subCol = remember(ad.subtitleColorHex) {
                        try { Color(android.graphics.Color.parseColor(ad.subtitleColorHex)) } catch (e: Exception) { parsedAccentColor }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                translationX = ad.titleOffsetX
                                translationY = ad.titleOffsetY + 6f
                                scaleX = ad.titleScale
                                scaleY = ad.titleScale
                                rotationZ = ad.titleRotation
                            }
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (ad.titleHasFrame) try { Color(android.graphics.Color.parseColor(ad.titleFrameColorHex)) } catch (e: Exception) { Color.Black.copy(alpha = 0.5f) }
                                else Color.Transparent
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
                                    text = PriceParser.englishToPersianDigits(ad.title),
                                    fontWeight = titleFontWeight,
                                    fontFamily = titleFontFamily,
                                    fontSize = ad.titleFontSize.sp,
                                    color = titleCol
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = parsedAccentColor.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, parsedAccentColor.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = PriceParser.englishToPersianDigits(ad.subtitle),
                                    fontSize = 10.5.sp,
                                    fontFamily = subFontFamily,
                                    fontWeight = subFontWeight,
                                    color = subCol,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                layerKey == "content" -> {
                    val (contentFontFamily, contentFontWeight) = remember(ad.contentFont) { getPersianFontSpecs(ad.contentFont) }
                    val contentCol = remember(ad.contentColorHex) {
                        try { Color(android.graphics.Color.parseColor(ad.contentColorHex)) } catch (e: Exception) { Color.White }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                translationX = ad.contentOffsetX
                                translationY = ad.contentOffsetY
                                scaleX = ad.contentScale
                                scaleY = ad.contentScale
                                rotationZ = ad.contentRotation
                            }
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (ad.contentHasFrame) try { Color(android.graphics.Color.parseColor(ad.contentFrameColorHex)) } catch (e: Exception) { Color.Black.copy(alpha = 0.5f) }
                                else Color.Transparent
                            )
                            .padding(6.dp)
                    ) {
                        Text(
                            text = PriceParser.englishToPersianDigits(ad.content),
                            fontSize = ad.contentFontSize.sp,
                            fontFamily = contentFontFamily,
                            fontWeight = contentFontWeight,
                            color = contentCol,
                            lineHeight = (ad.contentFontSize + 4).sp,
                            textAlign = when (ad.contentAlign) {
                                "CENTER" -> TextAlign.Center
                                "LEFT" -> TextAlign.Left
                                else -> TextAlign.Right
                            }
                        )
                    }
                }

                layerKey == "overlay" -> {
                    if (ad.customOverlayText.isNotBlank()) {
                        val (overlayFontFamily, overlayFontWeight) = remember(ad.overlayFont) { getPersianFontSpecs(ad.overlayFont) }
                        val overlayCol = remember(ad.customOverlayColor) {
                            try { Color(android.graphics.Color.parseColor(ad.customOverlayColor)) } catch (e: Exception) { GoldAccent }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .graphicsLayer {
                                    translationX = ad.overlayOffsetX
                                    translationY = ad.overlayOffsetY - 38f
                                    rotationZ = ad.overlayRotation.toFloat()
                                    alpha = ad.overlayOpacity
                                }
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (ad.overlayHasFrame) Color.Black.copy(alpha = 0.65f) else Color.Transparent
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = PriceParser.englishToPersianDigits(ad.customOverlayText),
                                fontSize = ad.customOverlaySize.sp,
                                fontFamily = overlayFontFamily,
                                fontWeight = overlayFontWeight,
                                color = overlayCol
                            )
                        }
                    }
                }

                layerKey == "main_cta" -> {
                    if (ad.ctaText.isNotBlank()) {
                        val ctaBg = remember(ad.accentColorHex) {
                            try { Color(android.graphics.Color.parseColor(ad.accentColorHex)) } catch (e: Exception) { GoldAccent }
                        }
                        val ctaTxtCol = remember(ad.ctaTextColorHex) {
                            try { Color(android.graphics.Color.parseColor(ad.ctaTextColorHex)) } catch (e: Exception) { Color.Black }
                        }
                        val (ctaFontFamily, ctaFontWeight) = remember(ad.ctaFont) { getPersianFontSpecs(ad.ctaFont) }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .graphicsLayer {
                                    translationX = ad.ctaOffsetX
                                    translationY = ad.ctaOffsetY - 8f
                                    scaleX = ad.ctaScale
                                    scaleY = ad.ctaScale
                                    rotationZ = ad.ctaRotation
                                }
                                .padding(4.dp)
                                .clickable { onCtaClick(ad.ctaUrl) }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ctaBg
                            ) {
                                Text(
                                    text = PriceParser.englishToPersianDigits(ad.ctaText),
                                    fontSize = 11.sp,
                                    fontFamily = ctaFontFamily,
                                    fontWeight = ctaFontWeight,
                                    color = ctaTxtCol,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                layerKey.startsWith("extra_text_") -> {
                    val textId = layerKey.removePrefix("extra_text_")
                    val textLayer = ad.extraTexts.find { it.id == textId }
                    if (textLayer != null) {
                        val (layerFont, layerWeight) = remember(textLayer.fontName) { getPersianFontSpecs(textLayer.fontName) }
                        val layerCol = remember(textLayer.colorHex) {
                            try { Color(android.graphics.Color.parseColor(textLayer.colorHex)) } catch (e: Exception) { Color.White }
                        }

                        val layerFrameCol = remember(textLayer.frameColorHex) {
                            try { Color(android.graphics.Color.parseColor(textLayer.frameColorHex)) } catch (e: Exception) { Color.Black.copy(alpha = 0.65f) }
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
                                .padding(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (textLayer.frameStyle) {
                                    "NONE" -> Color.Transparent
                                    "GLASS" -> Color.White.copy(alpha = 0.18f)
                                    else -> layerFrameCol
                                },
                                border = if (textLayer.frameStyle != "NONE") {
                                    BorderStroke(
                                        width = 1.5.dp,
                                        color = when (textLayer.frameStyle) {
                                            "GOLD_BORDER" -> GoldAccent
                                            "NEON" -> Color(0xFF00E5FF)
                                            "DASHED", "DOTTED", "STRIPED" -> GoldAccent
                                            else -> layerCol.copy(alpha = 0.6f)
                                        }
                                    )
                                } else null
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
                    val imgLayer = ad.extraImages.find { it.id == imgId }
                    if (imgLayer != null) {
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
                        ) {
                            AsyncImage(
                                model = imgLayer.url,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }

                layerKey.startsWith("extra_cta_") -> {
                    val ctaId = layerKey.removePrefix("extra_cta_")
                    val ctaLayer = ad.extraCtas.find { it.id == ctaId }
                    if (ctaLayer != null) {
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
                                .padding(4.dp)
                                .clickable {
                                    if (ctaLayer.subLinks.isNotEmpty()) {
                                        multiLinkDialogCta = ctaLayer
                                    } else {
                                        onCtaClick(ctaLayer.url)
                                    }
                                }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ctaBg
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
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

        multiLinkDialogCta?.let { cta ->
            AlertDialog(
                onDismissRequest = { multiLinkDialogCta = null },
                title = {
                    Text(text = PriceParser.englishToPersianDigits(cta.text), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldAccent)
                },
                text = {
                    Column {
                        Text("جهت ارتباط یکی از راه‌های زیر را انتخاب کنید:", fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        cta.subLinks.forEach { sub ->
                            Button(
                                onClick = {
                                    try {
                                        val raw = sub.url.trim()
                                        val fullUrl = if (raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("tel:")) raw else "https://$raw"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) { }
                                    multiLinkDialogCta = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Text(PriceParser.englishToPersianDigits(sub.title), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { multiLinkDialogCta = null }) {
                        Text("بستن", color = Color.White)
                    }
                }
            )
        }
    }
}

