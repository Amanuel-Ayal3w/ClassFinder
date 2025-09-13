plugins {
    kotlin("jvm") version "2.0.21"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.24")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Appium + Selenium
    testImplementation("io.appium:java-client:9.2.0")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.18.1")
    testImplementation("commons-io:commons-io:2.15.1")
}

tasks.test {
    useJUnitPlatform()
    // Allow passing environment variables for device/emulator selection
    systemProperty("appium.server.url", System.getProperty("appium.server.url", System.getenv("APPIUM_SERVER_URL") ?: "http://127.0.0.1:4723"))
    systemProperty("android.udid", System.getProperty("android.udid", System.getenv("ANDROID_UDID") ?: ""))
}
