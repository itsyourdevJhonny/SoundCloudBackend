package org.soundcloud

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service

@Service
class SoundCloudService(private val authService: SoundCloudAuthService) {

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()

    fun searchTracks(query: String, limit: Int = 10): List<TrackResponse> {
        val accessToken = authService.getAccessToken()

        val url = "https://api-v2.soundcloud.com/search/tracks?q=$query&limit=$limit"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed search request: ${response.code}")
            val json = response.body?.string() ?: throw RuntimeException("Empty response")
            println("SEARCH RESPONSE: $json")

            val mapAdapter = moshi.adapter(Map::class.java)
            val map = mapAdapter.fromJson(json) ?: emptyMap<String, Any>()
            val collection = map["collection"] as? List<Map<String, Any>> ?: emptyList()

            return collection.map { track ->
                TrackResponse(
                    id = (track["id"] as? Double)?.toLong() ?: 0L,
                    title = track["title"] as? String ?: "Unknown",
                    artist = ((track["user"] as? Map<*, *>)?.get("username") as? String) ?: "Unknown",
                    artworkUrl = track["artwork_url"] as? String,
                    duration = (track["full_duration"] as? Double)?.toLong() ?: 0L,
                    permalinkUrl = track["permalink_url"] as? String ?: ""
                )
            }
        }
    }

    /**
     * Search tracks using SoundCloud API
     */
    /*fun searchTracks(code: String, query: String, limit: Int = 10): List<TrackResponse> {
        val accessToken = authService.getAccessToken(code)

        val url = "https://api-v2.soundcloud.com/search/tracks?q=$query&limit=$limit"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed search request: ${response.code}")
            val json = response.body?.string() ?: throw RuntimeException("Empty response")
            println("SEARCH RESPONSE: $json")

            // parse top-level map
            val mapAdapter = moshi.adapter(Map::class.java)
            val map = mapAdapter.fromJson(json) ?: emptyMap<String, Any>()

            val collection = map["collection"] as? List<Map<String, Any>> ?: emptyList()

            // map to TrackResponse
            return collection.map { track ->
                TrackResponse(
                    id = (track["id"] as? Double)?.toLong() ?: 0L,
                    title = track["title"] as? String ?: "Unknown",
                    artist = ((track["user"] as? Map<*, *>)?.get("username") as? String) ?: "Unknown",
                    artworkUrl = track["artwork_url"] as? String,
                    duration = (track["full_duration"] as? Double)?.toLong() ?: 0L,
                    permalinkUrl = track["permalink_url"] as? String ?: ""
                )
            }
        }
    }*/
}