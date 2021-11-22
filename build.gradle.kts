import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.*

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("io.gitlab.arturbosch.detekt").version("1.18.0")
    kotlin("plugin.serialization") version "1.5.31"
}

group = "com.code_labeler"
version = "0.0.1"
application {
    mainClass.set("com.code_labeler.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-tomcat:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("org.jetbrains.exposed", "exposed-core", "0.34.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.34.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.34.1")
    implementation("org.postgresql:postgresql:42.2.24")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.1.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    api("aws.sdk.kotlin:s3:0.4.0-alpha")
}

detekt {
    allRules = true
    detekt.buildUponDefaultConfig = true
    config = files("./config/detekt/detekt.yml")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events(
           TestLogEvent.STANDARD_ERROR,
            TestLogEvent.STARTED,
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED
        )
    }
}
