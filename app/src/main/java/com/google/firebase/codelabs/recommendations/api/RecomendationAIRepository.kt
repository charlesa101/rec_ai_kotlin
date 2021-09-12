package com.google.firebase.codelabs.recommendations.api

import android.content.Context
//import com.google.firebase.codelabs.recommendations.api.Item
//import com.google.firebase.codelabs.recommendations.utils.Config
//import com.google.firebase.codelabs.recommendations.utils.getJsonDataFromAsset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


import  okhttp3.OkHttpClient
import  okhttp3.Request

import java.io.IOException

class RecomendationAIRepository private constructor() {

    private val items: MutableList<Item> = mutableListOf()
    private val httpClient = OkHttpClient()

    private val PROJECT_ID = "" //add project_id
    private val GOOG_REC_URL = "https://retail.googleapis.com/v2/projects/$PROJECT_ID/locations/global/catalogs/default_catalog/placements/movielens-placement-oyml:predict"
    private val BEARER_TOKEN = "" //add bearer token

    fun fetchRecommendations(): String {
        val request = Request.Builder()
            .url(GOOG_REC_URL)
            .get()
            .addHeader("Authorization" , "Bearer $BEARER_TOKEN")
            .build()

        var result: String = "{}"

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }
            result = response.body!!.string()
            println(result)
        }

        return result

    }

    suspend fun getRecommendations(): List<Item> {
        if (items.isEmpty()) {
            val jsonFileString = fetchRecommendations() //get recommendations
            val gson = Gson()
            val listItemType = object : TypeToken<List<Item>>() {}.type
            items.addAll(gson.fromJson(jsonFileString, listItemType))
        }
        return items
    }

    companion object {
        @Volatile private var instance: com.google.firebase.codelabs.recommendations.api.RecomendationAIRepository? = null
        @Volatile private var context: Context? = null

        fun getInstance(inContext: Context) =
            instance ?: synchronized(this) {
                instance ?: RecomendationAIRepository().also {
                    instance = it
                    context = inContext
                }
            }
    }
}