plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless)
}

// Kod biçimi tek standartta tutulur: ./gradlew spotlessApply
spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    kotlin {
        target("app/src/**/*.kt")
        ktlint(ktlintVersion)
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts")
        ktlint(ktlintVersion)
    }
}
