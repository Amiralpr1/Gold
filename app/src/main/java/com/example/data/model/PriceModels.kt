package com.example.data.model

import android.util.Log
import kotlin.text.RegexOption

data class PriceItem(
    val name: String,
    val currentPrice: String,
    val currentUnit: String,
    val highPrice: String,
    val highUnit: String,
    val lowPrice: String,
    val lowUnit: String,
    val trend: PriceTrend = PriceTrend.NEUTRAL
)

enum class PriceTrend {
    UP, DOWN, NEUTRAL
}

data class PricesResponse(
    val lastUpdated: String,
    val items: List<PriceItem>,
    val adText: String? = null
)

object JalaliCalendar {
    fun getCurrentIranDateTime(): String {
        return try {
            val zoneId = java.time.ZoneId.of("Asia/Tehran")
            val now = java.time.ZonedDateTime.now(zoneId)
            val year = now.year
            val month = now.monthValue
            val day = now.dayOfMonth
            val (jYear, jMonth, jDay) = gregorianToJalali(year, month, day)
            val hour = String.format("%02d", now.hour)
            val minute = String.format("%02d", now.minute)
            val dateStr = "$jYear/${String.format("%02d", jMonth)}/${String.format("%02d", jDay)} - $hour:$minute"
            PriceParser.englishToPersianDigits(dateStr)
        } catch (e: Exception) {
            "امروز"
        }
    }

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i + 1]
        }
        if (gm > 1 && ((gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0..11) {
            val daysInCurrentMonth = if (i < 6) 31 else if (i < 11) 30 else if (isJalaliLeapYear(jy)) 30 else 29
            if (jDayNo < daysInCurrentMonth) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= daysInCurrentMonth
        }

        return Triple(jy, jm, jd)
    }

    private fun isJalaliLeapYear(jy: Int): Boolean {
        val a = jy - 474
        val b = (a % 2820) + 474
        return ((b + 38) * 682) % 2816 < 682
    }
}

object PriceParser {
    private const val TAG = "PriceParser"

    fun persianToEnglishDigits(input: String): String {
        var output = input
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        val arabicDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        for (i in 0..9) {
            output = output.replace(persianDigits[i], i.toString())
            output = output.replace(arabicDigits[i], i.toString())
        }
        return output
    }

    fun englishToPersianDigits(input: String): String {
        var output = input
        val englishDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        for (i in 0..9) {
            output = output.replace(englishDigits[i], persianDigits[i])
        }
        return output
    }

    fun parseToDouble(priceStr: String): Double? {
        return try {
            val englishStr = persianToEnglishDigits(priceStr)
            val cleanStr = englishStr.replace(",", "").replace(" ", "").trim()
            cleanStr.toDoubleOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting price to double: $priceStr", e)
            null
        }
    }

    private fun parsePriceAndUnit(text: String): Pair<String, String> {
        val cleaned = text.replace("-", "").trim()
        val numberRegex = Regex("""^([0-9۰-۹,\.\s]+)""")
        val match = numberRegex.find(cleaned)
        return if (match != null) {
            val priceStr = match.groupValues[1].trim()
            val unitStr = cleaned.substring(match.groupValues[1].length).trim()
            Pair(priceStr, unitStr)
        } else {
            Pair(cleaned, "")
        }
    }

    fun parseRawResponse(rawJson: String): PricesResponse {
        // Fallback response if parsing fails
        val fallbackResponse = PricesResponse(
            lastUpdated = "نامشخص",
            items = emptyList()
        )

        try {
            // Raw response contains a single JSON field "formatted_text"
            val formattedTextRegex = Regex("""\"formatted_text\"\s*:\s*\"((?:\\.|[^\"])*)\"""", RegexOption.DOT_MATCHES_ALL)
            val match = formattedTextRegex.find(rawJson)
            val rawString = if (match != null) {
                unescapeJson(match.groupValues[1])
            } else {
                if (rawJson.contains("قیمتهای موجود")) {
                    unescapeJson(rawJson)
                } else {
                    ""
                }
            }

            // Check for ad field in JSON (e.g. "ad": "...", "Ad": "...", or "advertisement": "...")
            val adRegex = Regex("""\"(?:ad|advertisement)\"\s*:\s*\"((?:\\.|[^\"])*)\"""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val adMatch = adRegex.find(rawJson)
            var parsedAd = if (adMatch != null) {
                unescapeJson(adMatch.groupValues[1]).trim()
            } else null

            // Fallback 1: check for ((آگهی: ...)) or ((Ad: ...)) or ((تبلیغ: ...)) inside formatted_text
            if (parsedAd.isNullOrEmpty() && rawString.isNotEmpty()) {
                val inlineAdRegex = Regex("""\(\((?:Ad|آگهی|تبلیغ|تبلیغات):\s*(.*?)\)\)""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                val inlineMatch = inlineAdRegex.find(rawString)
                parsedAd = inlineMatch?.groupValues?.get(1)?.trim()
            }

            // Fallback 2: check for "آگهی:" or "تبلیغات:" or "Ad:" appended at the end of formatted_text
            if (parsedAd.isNullOrEmpty() && rawString.isNotEmpty()) {
                val endAdRegex = Regex("""(?:آگهی|تبلیغات|Ad):\s*(.*)""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                val endMatch = endAdRegex.find(rawString)
                parsedAd = endMatch?.groupValues?.get(1)?.trim()
            }

            if (rawString.isEmpty() && rawJson.contains("قیمتهای موجود") == false) {
                return fallbackResponse.copy(adText = parsedAd)
            }

            // Extract last update timestamp (Always format as Jalali Shamsi)
            var lastUpdated = JalaliCalendar.getCurrentIranDateTime()
            val lastUpdateMarker = "آخرین آپدیت:"
            if (rawString.contains(lastUpdateMarker)) {
                val sub = rawString.substringAfter(lastUpdateMarker).trim()
                val timePart = sub.substringBefore(")").trim()
                val timeRegex = Regex("""(\d{1,2}:\d{2}|[۰-۹]{1,2}:[۰-۹]{2})""")
                val timeMatch = timeRegex.find(timePart)
                val timeString = if (timeMatch != null) {
                    PriceParser.englishToPersianDigits(timeMatch.value)
                } else null

                val currentJalali = JalaliCalendar.getCurrentIranDateTime()
                if (timeString != null) {
                    val jalaliDate = currentJalali.substringBefore(" - ")
                    lastUpdated = "$jalaliDate - $timeString"
                } else {
                    lastUpdated = currentJalali
                }
            }

            // Define the 7 target items in exact display order
            val targetNames = listOf(
                "طلای ۱۸ عیار",
                "دلار نقدی",
                "سکه امامی",
                "سکه بهار آزادی",
                "نیم سکه",
                "ربع سکه",
                "اونس طلا"
            )

            // Extract blocks between ((( and )))
            val blockRegex = Regex("""\(\(\((.*?)\)\)\)""", RegexOption.DOT_MATCHES_ALL)
            val blocks = blockRegex.findAll(rawString).map { it.groupValues[1] }.toList()

            // Parse each block and put in a map by its name
            val parsedMap = mutableMapOf<String, PriceItem>()

            for (block in blocks) {
                try {
                    val namePart = block.substringBefore(":").replace("-", "").trim()
                    if (namePart.isEmpty()) continue

                    val rest = block.substringAfter(":")
                    val currentPriceRaw = rest.substringBefore("بیشترین قیمت امروز:")
                    val highPriceRaw = rest.substringAfter("بیشترین قیمت امروز:").substringBefore("کمترین قیمت امروز:")
                    val lowPriceRaw = rest.substringAfter("کمترین قیمت امروز:")

                    val (currentPrice, currentUnit) = parsePriceAndUnit(currentPriceRaw)
                    val (highPrice, highUnit) = parsePriceAndUnit(highPriceRaw)
                    val (lowPrice, lowUnit) = parsePriceAndUnit(lowPriceRaw)

                    val currentVal = parseToDouble(currentPrice)
                    val highVal = parseToDouble(highPrice)
                    val lowVal = parseToDouble(lowPrice)

                    val trend = if (currentVal != null && highVal != null && lowVal != null && highVal > lowVal) {
                        val range = highVal - lowVal
                        if ((highVal - currentVal) / range < 0.15) {
                            PriceTrend.UP
                        } else if ((currentVal - lowVal) / range < 0.15) {
                            PriceTrend.DOWN
                        } else {
                            PriceTrend.NEUTRAL
                        }
                    } else {
                        PriceTrend.NEUTRAL
                    }

                    val priceItem = PriceItem(
                        name = namePart,
                        currentPrice = currentPrice,
                        currentUnit = currentUnit,
                        highPrice = highPrice,
                        highUnit = highUnit,
                        lowPrice = lowPrice,
                        lowUnit = lowUnit,
                        trend = trend
                    )

                    // Find if this parsed name matches any of our target names (substring match)
                    val matchedTarget = targetNames.find { target ->
                        namePart.contains(target) || target.contains(namePart)
                    }

                    if (matchedTarget != null) {
                        parsedMap[matchedTarget] = priceItem.copy(name = matchedTarget)
                    } else {
                        parsedMap[namePart] = priceItem
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing single block: $block", e)
                }
            }

            // Reconstruct the exact list of 7 items in the requested order
            val finalItems = targetNames.map { targetName ->
                parsedMap[targetName] ?: PriceItem(
                    name = targetName,
                    currentPrice = "نامشخص",
                    currentUnit = "",
                    highPrice = "نامشخص",
                    highUnit = "",
                    lowPrice = "نامشخص",
                    lowUnit = "",
                    trend = PriceTrend.NEUTRAL
                )
            }

            return PricesResponse(
                lastUpdated = lastUpdated,
                items = finalItems,
                adText = parsedAd
            )

        } catch (e: Exception) {
            Log.e(TAG, "Critical parsing error", e)
            return fallbackResponse
        }
    }

    private fun unescapeJson(escaped: String): String {
        return try {
            var str = escaped
            str = str.replace("\\n", "\n")
            str = str.replace("\\r", "\r")
            str = str.replace("\\t", "\t")
            str = str.replace("\\\"", "\"")
            str = str.replace("\\\\", "\\")
            str
        } catch (e: Exception) {
            escaped
        }
    }
}
