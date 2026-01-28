package org.soundcloud

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
        val url = "https://api.soundcloud.com/tracks?q=$q&limit=50"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "OAuth $accessToken")
            .addHeader("Accept", "application/json; charset=utf-8")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Search failed: ${response.code}")
            val json = response.body?.string() ?: ""

            val listAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                .adapter<List<Map<String, Any>>>(Types.newParameterizedType(List::class.java, Map::class.java))

            val collection = listAdapter.fromJson(json) ?: emptyList()

            val tracks = collection.map { track ->
                TrackResponse(
                    id = (track["id"] as? Double)?.toLong() ?: 0L,
                    title = track["title"] as? String ?: "Unknown",
                    artist = ((track["user"] as? Map<*, *>)?.get("username") as? String) ?: "Unknown",
                    artworkUrl = track["artwork_url"] as? String,
                    duration = (track["duration"] as? Double)?.toLong() ?: 0L,
                    permalinkUrl = track["permalink_url"] as? String ?: ""
                )
            }
            return ResponseEntity.ok(tracks)
        }
    }

    @GetMapping("/track/playable")
    fun getPlayableTrack(@RequestParam trackId: Long): ResponseEntity<Map<String, String>> {
        val accessToken = authService.getAccessToken() // safe on server

        val client = OkHttpClient.Builder()
            .followRedirects(true)  // follow 302 automatically
            .build()

        val streamUrl = "https://api.soundcloud.com/tracks/$trackId/stream"

        val request = Request.Builder()
            .url(streamUrl)
            .addHeader("Authorization", "OAuth $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return ResponseEntity.status(response.code).body(
                    mapOf("error" to response.body?.string().orEmpty())
                )
            }

            // response.request.url will now be the final redirected URL
            val playableUrl = response.request.url.toString()
            return ResponseEntity.ok(mapOf("playableUrl" to playableUrl))
        }
    }
}
