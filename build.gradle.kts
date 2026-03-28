// ============================================================
//  KoiUpstream – Team Koi #6230
//  build.gradle.kts
// ============================================================

plugins {
    `java-library`
    `maven-publish`
    id("edu.wpi.first.GradleRIO") version "2026.1.1"
}

// --------------- Project identity ---------------
group   = "team6230"
version = "1.0.12.2.2"

// --------------- Java toolchain ---------------
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

// --------------- Extra Maven repositories ---------------
repositories {
    maven { url = uri("https://frcmaven.wpi.edu/artifactory/littletonrobotics-mvn-release") }
    mavenCentral()
}

// --------------- Vendordep helpers ---------------
// Returns the top-level "version" field from a vendordep JSON.
fun vendordepVersion(fileName: String): String {
    val json = groovy.json.JsonSlurper().parseText(
        file("vendordeps/$fileName").readText()
    ) as Map<*, *>
    return json["version"] as String
}

// Returns all "groupId:artifactId:version" strings from javaDependencies[].
@Suppress("UNCHECKED_CAST")
fun vendordepJavaDeps(fileName: String): List<String> {
    val json = groovy.json.JsonSlurper().parseText(
        file("vendordeps/$fileName").readText()
    ) as Map<*, *>
    val deps = json["javaDependencies"] as? List<Map<*, *>> ?: return emptyList()
    return deps.map { "${it["groupId"]}:${it["artifactId"]}:${it["version"]}" }
}

val akitVersion   by lazy { vendordepVersion("AdvantageKit.json") }

// --------------- Dependencies ---------------
dependencies {
    // ── WPILib core ──────────────────────────────────────────────────────
    wpi.java.deps.wpilib().forEach { implementation(it) }
    // ── WPILib new commands (wpilibj2) ────────────────────────────────────
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:${wpi.versions.wpilibVersion.get()}")
    // ── AdvantageKit ─────────────────────────────────────────────────────
    compileOnly("org.littletonrobotics.akit:akit-java:$akitVersion")
    compileOnly("org.littletonrobotics.akit:akit-autolog:$akitVersion")
    annotationProcessor("org.littletonrobotics.akit:akit-autolog:$akitVersion")
    testAnnotationProcessor("org.littletonrobotics.akit:akit-autolog:$akitVersion")
    // ── Tests ────────────────────────────────────────────────────────────
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// --------------- Test configuration ---------------
tasks.test {
    useJUnitPlatform()
}

// --------------- Publishing ---------------
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "team6230"
            artifactId = "KoiUpstream"
            version = "1.0.12.2.2"
        }
    }

    repositories {
        maven {
            url = uri("${project.projectDir}/repo")
        }
    }
}