package com.example.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET

interface PriceApi {
    @GET("formatted_prices.json")
    suspend fun getFormattedPrices(): ResponseBody
}
