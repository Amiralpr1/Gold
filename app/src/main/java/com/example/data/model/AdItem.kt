package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoryImageLayer(
    val id: String,
    val url: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f
)

@JsonClass(generateAdapter = true)
data class StorySubLink(
    val id: String = "",
    val title: String,
    val url: String = "",
    val iconType: String = "PHONE" // PHONE, WHATSAPP, TELEGRAM, INSTAGRAM, WEBSITE
)

@JsonClass(generateAdapter = true)
data class StoryCtaLayer(
    val id: String,
    val text: String,
    val url: String = "",
    val colorHex: String = "#F59E0B",
    val textColorHex: String = "#000000",
    val fontName: String = "وزیرمتن (استاندارد)",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val subLinks: List<StorySubLink> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StoryTextLayer(
    val id: String,
    val text: String,
    val fontName: String = "وزیرمتن (استاندارد)",
    val colorHex: String = "#FFFFFF",
    val fontSize: Int = 14,
    val align: String = "RIGHT",
    val frameStyle: String = "NONE", // NONE, SOLID, GOLD_BORDER, GLASS, NEON, DARK_ROUND, DASHED
    val frameColorHex: String = "#44000000",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f
)

@JsonClass(generateAdapter = true)
data class AdItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val content: String,
    val iconName: String = "WorkspacePremium",
    val accentColorHex: String = "#F59E0B",
    val bgImageUrl: String = "",
    val username: String = "",
    val password: String = "",
    val remainingDays: Int = 30,
    val ctaText: String = "",
    val ctaUrl: String = "",
    val customerPin: String = "1001",
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val bgOpacity: Float = 0.22f,
    val fontName: String = "وزیرمتن (استاندارد)",
    val cropRotation: Int = 0,
    val customOverlayText: String = "",
    val customOverlayColor: String = "#FFD700",
    val customOverlaySize: Int = 13,
    val titleFontSize: Int = 14,
    val contentFontSize: Int = 12,
    val titleOffsetX: Float = 0f,
    val titleOffsetY: Float = 0f,
    val contentOffsetX: Float = 0f,
    val contentOffsetY: Float = 0f,
    val ctaOffsetX: Float = 0f,
    val ctaOffsetY: Float = 0f,
    val overlayOffsetX: Float = 0f,
    val overlayOffsetY: Float = 0f,
    val overlayOpacity: Float = 1.0f,
    val overlayRotation: Int = 0,
    // Per-item typography, color, frame, align & transforms
    val titleFont: String = "وزیرمتن (استاندارد)",
    val titleColorHex: String = "#FFFFFF",
    val titleRotation: Float = 0f,
    val titleScale: Float = 1.0f,
    val titleAlign: String = "RIGHT",
    val titleHasFrame: Boolean = false,
    val titleFrameColorHex: String = "#44000000",

    val subtitleFont: String = "وزیرمتن (استاندارد)",
    val subtitleColorHex: String = "#F59E0B",
    val subtitleRotation: Float = 0f,
    val subtitleScale: Float = 1.0f,
    val subtitleAlign: String = "RIGHT",
    val subtitleHasFrame: Boolean = true,

    val contentFont: String = "وزیرمتن (استاندارد)",
    val contentColorHex: String = "#FFFFFF",
    val contentRotation: Float = 0f,
    val contentScale: Float = 1.0f,
    val contentAlign: String = "RIGHT",
    val contentHasFrame: Boolean = false,
    val contentFrameColorHex: String = "#44000000",

    val overlayFont: String = "وزیرمتن (استاندارد)",
    val overlayAlign: String = "CENTER",
    val overlayHasFrame: Boolean = true,

    val ctaRotation: Float = 0f,
    val ctaScale: Float = 1.0f,
    val ctaTextColorHex: String = "#000000",
    val ctaFont: String = "وزیرمتن (استاندارد)",

    val bgScale: Float = 1.0f,
    val bgOffsetX: Float = 0f,
    val bgOffsetY: Float = 0f,

    // Multiple Images, Multiple Links/CTAs, Dynamic Text Layers, and Photoshop Z-Index Ordering
    val extraImages: List<StoryImageLayer> = emptyList(),
    val extraCtas: List<StoryCtaLayer> = emptyList(),
    val extraTexts: List<StoryTextLayer> = emptyList(),
    val layerOrder: List<String> = emptyList()
)



