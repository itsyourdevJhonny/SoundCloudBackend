package org.soundcloud

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PkceUtil {

    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(64)
        secureRandom.nextBytes(code)
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(code)
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(digest)
    }
}
