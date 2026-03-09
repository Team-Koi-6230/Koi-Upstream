// ============================================================
//  KoiUpstream – Team Koi #6230
//  settings.gradle.kts
// ============================================================

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://frcmaven.wpi.edu/release") }
    }
}

rootProject.name = "KoiUpstream"