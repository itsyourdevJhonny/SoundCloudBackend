package org.soundcloud

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

    fun exchangeCodeForToken(code: String) {
        val client = OkHttpClient()
        val form = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("redirect_uri", redirectUri)
            .add("code", code)
            .build()

        val request = Request.Builder()
            .url("https://api.soundcloud.com/oauth/token")
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
}


/*@Service
class SoundCloudAuthService(
    @Value("\${soundcloud.client-id}") private val clientId: String,
    @Value("\${soundcloud.client-secret}") private val clientSecret: String
) {

    private var accessToken: String? = null
    private var expiryTime: Long = 0
    private val lock = ReentrantLock()
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()

    *//**
     * Returns valid access token, refreshes if expired
     *//*
    fun getAccessToken(): String = lock.withLock {
        val now = System.currentTimeMillis()
        if (accessToken != null && now < expiryTime) return accessToken!!

        val form = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url("https://secure.soundcloud.com/oauth/token")
            .post(form)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed token request: ${response.code}")
            val json = response.body?.string() ?: throw RuntimeException("Empty response")
            println("TOKEN RESPONSE: $json")

            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(json) ?: emptyMap<String, Any>()

            accessToken = map["access_token"] as? String ?: throw RuntimeException("No token")
            val expiresIn = (map["expires_in"] as? Double)?.toLong() ?: 3600
            expiryTime = now + expiresIn * 1000
            return accessToken!!
        }
    }
}*/

/*
@Service
class SoundCloudAuthService(
    @Value("\${soundcloud.client-id}") private val clientId: String,
    @Value("\${soundcloud.client-secret}") private val clientSecret: String,
    @Value("\${soundcloud.redirect-uri}") private val redirectUri: String,
    @Value("\${soundcloud.code-verifier}") private val codeVerifier: String
) {

    private var accessToken: String? = null
    private var expiryTime: Long = 0
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()

    */
/**
     * Exchanges authorization code for access token
     *//*

    fun getAccessToken(code: String): String {
        val now = System.currentTimeMillis()
        if (accessToken != null && now < expiryTime) return accessToken!!

        val form = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("redirect_uri", redirectUri)
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .build()

        val request = Request.Builder()
            .url("https://secure.soundcloud.com/oauth/token")
            .post(form)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed token request: ${response.code}")
            val json = response.body?.string() ?: throw RuntimeException("Empty response")
            println("TOKEN RESPONSE: $json")

            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(json) ?: emptyMap<String, Any>()

            accessToken = map["access_token"] as? String ?: throw RuntimeException("No token")
            val expiresIn = (map["expires_in"] as? Double)?.toLong() ?: 3600
            expiryTime = now + expiresIn * 1000
            return accessToken!!
        }
    }
}
*/
