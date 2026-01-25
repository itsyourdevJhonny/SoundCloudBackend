package org.soundcloud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SoundCloudBackendApplication

fun main(args: Array<String>) {
    runApplication<SoundCloudBackendApplication>(*args)
}
