package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.PriceParser
import com.example.data.model.PricesResponse
import com.example.data.remote.PriceApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prices_cache")

class PriceRepository(private val context: Context) {
    private val api: PriceApi

    companion object {
        private const val TAG = "PriceRepository"
        val KEY_RAW_DATA = stringPreferencesKey("raw_prices_json")
    }

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://103.75.197.157/")
            .client(okHttpClient)
            .build()

        api = retrofit.create(PriceApi::class.java)
    }

    suspend fun fetchFreshPrices(): PricesResponse {
        return try {
            val responseBody = api.getFormattedPrices()
            val rawJson = responseBody.string()
            
            if (rawJson.contains("formatted_text")) {
                saveToCache(rawJson)
            }
            PriceParser.parseRawResponse(rawJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching fresh prices, fallback to cache", e)
            val cachedRaw = getCachedRaw()
            if (!cachedRaw.isNullOrEmpty()) {
                PriceParser.parseRawResponse(cachedRaw)
            } else {
                PriceParser.parseRawResponse("")
            }
        }
    }

    fun getCachedPricesFlow(): Flow<PricesResponse> {
        return context.dataStore.data.map { preferences ->
            val raw = preferences[KEY_RAW_DATA] ?: ""
            PriceParser.parseRawResponse(raw)
        }
    }

    suspend fun getCachedPrices(): PricesResponse {
        val raw = getCachedRaw() ?: ""
        return PriceParser.parseRawResponse(raw)
    }

    private suspend fun saveToCache(rawJson: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[KEY_RAW_DATA] = rawJson
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save to DataStore cache", e)
        }
    }

    private suspend fun getCachedRaw(): String? {
        return try {
            val preferences = context.dataStore.data.first()
            preferences[KEY_RAW_DATA]
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read from DataStore cache", e)
            null
        }
    }
}
