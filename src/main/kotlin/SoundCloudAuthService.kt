package org.soundcloud

import org.springframework.beans.factory.annotation.Value
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class SoundCloudAuthService(
    @Value("\${soundcloud.client-id}") private val clientId: String,
    @Value("\${soundcloud.client-secret}") private val clientSecret: String,
) {

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private var accessToken: String? = null
    private var expiryTime: Long = 0
    private val lock = ReentrantLock()

    /**
     * Returns a valid access token.
     * If expired or missing, automatically fetches a new one via client credentials flow.
     */
    fun getAccessToken(): String = lock.withLock {
        val now = System.currentTimeMillis()
        if (accessToken != null && now < expiryTime) return accessToken!!

        fetchAccessToken()
        return accessToken!!
    }

    /**
     * Fetches a new access token from SoundCloud using client credentials flow.
     */
    private fun fetchAccessToken() {
        val form = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url("https://api.soundcloud.com/oauth2/token")
            .post(form)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed to fetch token: ${response.code}")

            val json = response.body?.string() ?: throw RuntimeException("Empty token response")
            val map = moshi.adapter(Map::class.java).fromJson(json) ?: emptyMap<String, Any>()

            accessToken = map["access_token"] as? String
                ?: throw RuntimeException("No access token in response")
            val expiresIn = (map["expires_in"] as? Double)?.toLong() ?: 3600
            expiryTime = System.currentTimeMillis() + expiresIn * 1000
        }
    }
}


/*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


@Service
class SoundCloudAuthService(
    @Value("\${soundcloud.client-id}") private val clientId: String,
    @Value("\${soundcloud.client-secret}") private val clientSecret: String,
    @Value("\${soundcloud.redirect-uri}") private val redirectUri: String
) {

    private var accessToken: String? = null
    private var expiryTime: Long = 0

    fun getAccessToken(): String {
        val now = System.currentTimeMillis()
        if (accessToken != null && now < expiryTime) return accessToken!!

        throw RuntimeException("Access token missing! Exchange authorization code first.")
    }

    fun exchangeCodeForToken(code: String, verifier: String) {
        val client = OkHttpClient()
        val form = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("redirect_uri", redirectUri)
            .add("code_verifier", verifier)
            .add("code", code)
            .build()

        val request = Request.Builder()
            .url("https://secure.soundcloud.com/oauth/token")
            .post(form)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed token request: ${response.code}")

            val json = response.body?.string() ?: throw RuntimeException("Empty token response")
            val map = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                .adapter(Map::class.java)
                .fromJson(json) ?: emptyMap<String, Any>()

            accessToken = map["access_token"] as? String ?: throw RuntimeException("No token in response")
            val expiresIn = (map["expires_in"] as? Double)?.toLong() ?: 3600
            expiryTime = System.currentTimeMillis() + expiresIn * 1000
        }
    }
}*/
