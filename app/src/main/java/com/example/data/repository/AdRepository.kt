package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ad_preferences", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, AdItem::class.java)
    private val jsonAdapter = moshi.adapter<List<AdItem>>(listType)

    private val _adsFlow = MutableStateFlow<List<AdItem>>(emptyList())
    val adsFlow: StateFlow<List<AdItem>> = _adsFlow.asStateFlow()

    init {
        loadAds()
    }

    fun loadAds(): List<AdItem> {
        val json = prefs.getString("saved_ads_json", null)
        val loadedList = if (!json.isNullOrEmpty()) {
            try {
                jsonAdapter.fromJson(json) ?: getDefaultAds()
            } catch (e: Exception) {
                getDefaultAds()
            }
        } else {
            getDefaultAds()
        }

        val sorted = loadedList.sortedBy { it.displayOrder }
        _adsFlow.value = sorted
        return sorted
    }

    fun saveAds(ads: List<AdItem>) {
        val sorted = ads.sortedBy { it.displayOrder }
        _adsFlow.value = sorted
        try {
            val json = jsonAdapter.toJson(sorted)
            prefs.edit().putString("saved_ads_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSingleAd(updatedAd: AdItem) {
        val currentList = loadAds().toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedAd.id }
        if (index >= 0) {
            currentList[index] = updatedAd
        } else {
            currentList.add(updatedAd)
        }
        saveAds(currentList)
    }

    fun deleteAd(id: String) {
        val currentList = loadAds().filterNot { it.id == id }
        saveAds(currentList)
    }

    fun toggleAdActive(id: String) {
        val currentList = loadAds().map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        saveAds(currentList)
    }

    fun resetToDefaults() {
        val defaults = getDefaultAds()
        saveAds(defaults)
    }

    fun getDefaultAds(): List<AdItem> {
        return listOf(
            AdItem(
                id = "slide_1",
                title = "زرین گلد ✨",
                subtitle = "فروش ویژه و فوری طلا 🏆",
                content = "✨ زرین گلد ✨\n🪙 فروش فوری ۱۰۰۰ گرم شمش طلا ۲۴ عیار با فاکتور رسمی و کد استاندارد.\n📍 محدوده: تهران، پاساژ قائم تجریش\n📞 شماره تماس: 09122773778",
                iconName = "WorkspacePremium",
                accentColorHex = "#F59E0B",
                bgImageUrl = "https://images.unsplash.com/photo-1610375461246-83df859d849d?auto=format&fit=crop&w=600&q=80",
                username = "zarrin",
                password = "1001",
                remainingDays = 25,
                ctaText = "✈️ ارسال پیام در تلگرام",
                ctaUrl = "https://t.me/asantarrah",
                customerPin = "1001",
                isActive = true,
                displayOrder = 0
            ),
            AdItem(
                id = "slide_2",
                title = "گالری طلا و سکه پارسیان 🪙",
                subtitle = "خرید و فروش سکه بانکی 💰",
                content = "🪙 گالری طلا و سکه پارسیان 💰\n🥇 خرید و فروش تضمینی انواع سکه امامی، بهار آزادی و نیم سکه به نرخ بازار.\n📱 تلفن سفارشات: 09121111111",
                iconName = "MonetizationOn",
                accentColorHex = "#34D399",
                bgImageUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?auto=format&fit=crop&w=600&q=80",
                username = "parsian",
                password = "1002",
                remainingDays = 18,
                ctaText = "📞 تماس مستقیم با سفارشات",
                ctaUrl = "tel:09121111111",
                customerPin = "1002",
                isActive = true,
                displayOrder = 1
            ),
            AdItem(
                id = "slide_3",
                title = "صرافی و بازرگانی آریا 💵",
                subtitle = "حواله‌جات ارزی و دلار ⚡",
                content = "💵 صرافی بین‌المللی آریا ⚡\n✈️ انجام کلیه خدمات ارزی، حواله دلار و یورو با بالاترین سرعت و بهترین نرخ روز.\n📍 آدرس: خیابان فردوسی، بالاتر از منوچهری",
                iconName = "CurrencyExchange",
                accentColorHex = "#60A5FA",
                bgImageUrl = "https://images.unsplash.com/photo-1580519542036-c47de6196ba5?auto=format&fit=crop&w=600&q=80",
                username = "aria",
                password = "1003",
                remainingDays = 12,
                ctaText = "🌐 مشاهده وب‌سایت صرافی",
                ctaUrl = "https://www.asantarrah.com",
                customerPin = "1003",
                isActive = true,
                displayOrder = 2
            ),
            AdItem(
                id = "slide_4",
                title = "جواهری سلطنتی الماس 💎",
                subtitle = "طلای کم‌اجرت و سرمایه‌گذاری 💍",
                content = "💎 جواهری سلطنتی الماس 👑\n✨ طراحی و ساخت سرویس‌های لوکس طلا، سرویس عروس و طلای بدون کارمزد.\n🏢 مستقیم از کارگاه با فاکتور رسمی",
                iconName = "Diamond",
                accentColorHex = "#E879F9",
                bgImageUrl = "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=600&q=80",
                username = "almas",
                password = "1004",
                remainingDays = 30,
                ctaText = "💬 چت در تلگرام",
                ctaUrl = "https://t.me/asantarrah",
                customerPin = "1004",
                isActive = true,
                displayOrder = 3
            ),
            AdItem(
                id = "slide_5",
                title = "طلای صفوی & شمش آنلاین 🥇",
                subtitle = "تحویل فوری و آنلاین 🚀",
                content = "🥇 طلای صفوی & شمش آنلاین 🚀\n⚡ ارسال سریع شمش‌های ۱ گرمی تا ۱ کیلوگرمی به سراسر کشور با بیمه‌نامه معتبر.",
                iconName = "Verified",
                accentColorHex = "#FCA5A5",
                bgImageUrl = "https://images.unsplash.com/photo-1589758438368-0ad531db3366?auto=format&fit=crop&w=600&q=80",
                username = "safavi",
                password = "1005",
                remainingDays = 7,
                ctaText = "🛒 ثبت سفارش شمش",
                ctaUrl = "https://www.asantarrah.com",
                customerPin = "1005",
                isActive = true,
                displayOrder = 4
            ),
            AdItem(
                id = "slide_6",
                title = "رزرو مکان تبلیغات شما 📢",
                subtitle = "سفارش آگهی اختصاصی 🌟",
                content = "📢 رزرو مکان تبلیغات شما 📈\n💫 کسب و کار، طلافروشی یا صرافی خود را به هزاران فعال بازار طلا و ارز معرفی کنید!\n📱 جهت سفارش و رزرو آگهی با ما تماس بگیرید.",
                iconName = "Campaign",
                accentColorHex = "#F59E0B",
                bgImageUrl = "https://images.unsplash.com/photo-1557804506-669a67965ba0?auto=format&fit=crop&w=600&q=80",
                username = "admin",
                password = "7788",
                remainingDays = 365,
                ctaText = "📞 رزرو سریع آگهی",
                ctaUrl = "tel:09122773778",
                customerPin = "7788",
                isActive = true,
                displayOrder = 5
            )
        )
    }
}
