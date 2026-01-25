package org.soundcloud

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SoundCloudCallbackController(
    private val authService: SoundCloudAuthService,
    @Value("\${soundcloud.state}") private val expectedState: String
) {

    @GetMapping("callback")
    /*fun callback(@RequestParam code: String) : ResponseEntity<String> {
        return ResponseEntity.ok(code)
    }*/
    fun callback(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<String> {
        if (state != expectedState) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid state")

        authService.exchangeCodeForToken(code)

        return ResponseEntity.ok("Token successfully obtained! Now you can call /api/search?q=...")
    }
}
