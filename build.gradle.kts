plugins {
    kotlin("jvm") version "2.1.10"

    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.2"
    kotlin("plugin.spring") version "2.1.10"
}

group = "org.soundcloud"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
}
tasks.withType<Jar> {
    archiveBaseName.set("soundcloudbackend")
    archiveVersion.set("1.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}