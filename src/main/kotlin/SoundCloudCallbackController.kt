package org.soundcloud

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SoundCloudAuthController(

    private val authService: SoundCloudAuthService,

    @Value("\${soundcloud.client-id}")
    private val clientId: String,

    @Value("\${soundcloud.redirect-uri}")
    private val redirectUri: String,

    @Value("\${soundcloud.state}")
    private val expectedState: String
) {

    @GetMapping("/auth/soundcloud")
    fun startAuth(
        response: HttpServletResponse,
        session: HttpSession
    ) {
        val verifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.generateCodeChallenge(verifier)

        session.setAttribute("pkce_verifier", verifier)

        val authUrl =
            "https://secure.soundcloud.com/authorize" +
                    "?client_id=$clientId" +
                    "&redirect_uri=$redirectUri" +
                    "&response_type=code" +
                    "&code_challenge=$challenge" +
                    "&code_challenge_method=S256" +
                    "&state=$expectedState"

        response.sendRedirect(authUrl)
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam code: String,
        @RequestParam state: String,
        session: HttpSession
    ): ResponseEntity<String> {

        if (state != expectedState) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid state")
        }

        val verifier = session.getAttribute("pkce_verifier") as? String
            ?: return ResponseEntity.badRequest()
                .body("Missing PKCE verifier")

        authService.exchangeCodeForToken(code, verifier)

        return ResponseEntity.ok("SoundCloud login successful! Your Token: ${authService.getAccessToken()}")
    }
}

