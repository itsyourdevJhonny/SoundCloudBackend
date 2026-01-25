package org.soundcloud

import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OAuthService(
    @Value("\${soundcloud.client-id}") private val clientId: String,
    @Value("\${soundcloud.client-secret}") private val clientSecret: String,
    @Value("\${soundcloud.redirect-uri}") private val redirectUri: String
) {
    private val client = OkHttpClient()
    private var accessToken: String? = null

    // Step 1: Generate authorization URL to redirect user
    fun getAuthorizationUrl(): String {
        return "https://soundcloud.com/connect?" +
                "client_id=$clientId" +
                "&redirect_uri=$redirectUri" +
                "&response_type=code" +
                "&scope=non-expiring"
    }

    // Step 2: Exchange code for access token
    fun exchangeCodeForToken(code: String) {
        val form = FormBody.Builder()
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("redirect_uri", redirectUri)
            .add("grant_type", "authorization_code")
            .add("code", code)
            .build()

        val request = Request.Builder()
            .url("https://api.soundcloud.com/oauth2/token")
            .post(form)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Token request failed")
            val json = response.body?.string() ?: throw RuntimeException("Empty response")
            val moshi = Moshi.Builder().build()
            val mapAdapter = moshi.adapter(Map::class.java)
            val map = mapAdapter.fromJson(json) ?: throw RuntimeException("Invalid JSON")
            accessToken = map["access_token"] as? String ?: throw RuntimeException("No access token")
        }
    }

    fun getAccessToken(): String {
        return accessToken ?: throw RuntimeException("Access token not set. Authenticate first.")
    }
}
