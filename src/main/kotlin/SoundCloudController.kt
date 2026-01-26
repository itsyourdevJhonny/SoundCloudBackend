package org.soundcloud

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

@RestController
@RequestMapping("/api")
class SoundCloudController(
    private val authService: SoundCloudAuthService
) {

    @GetMapping("/search")
    fun search(@RequestParam q: String): ResponseEntity<List<TrackResponse>> {
        if (q.isBlank()) return ResponseEntity.badRequest().build()

        val accessToken = authService.getAccessToken()

        val client = OkHttpClient()
        val url = "https://api.soundcloud.com/tracks?q=$q&limit=10"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "OAuth $accessToken")
            .addHeader("Accept", "application/json; charset=utf-8")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Search failed: ${response.code}")
            val json = response.body?.string() ?: ""
            val map = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                .adapter(Map::class.java)
                .fromJson(json) ?: emptyMap<String, Any>()

            val collection = map["collection"] as? List<Map<String, Any>> ?: emptyList()

            val tracks = collection.map { track ->
                TrackResponse(
                    id = (track["id"] as? Double)?.toLong() ?: 0L,
                    title = track["title"] as? String ?: "Unknown",
                    artist = ((track["user"] as? Map<*, *>)?.get("username") as? String) ?: "Unknown",
                    artworkUrl = track["artwork_url"] as? String,
                    duration = (track["full_duration"] as? Double)?.toLong() ?: 0L,
                    permalinkUrl = track["permalink_url"] as? String ?: ""
                )
            }
            return ResponseEntity.ok(tracks)
        }
    }
}
