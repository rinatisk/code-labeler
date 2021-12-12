group = rootProject.group
version = rootProject.version

plugins {
    kotlin("js") version "1.6.0"
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
                devServer?.port = 8080
            }
        }
        binaries.executable()
    }
}

dependencies {
    // React, React DOM + Wrappers
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.264-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.264-kotlin-1.5.31")
    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))

    // Ktor client
    implementation("io.ktor:ktor-client-core:1.6.6")
    implementation("io.ktor:ktor-client-js:1.6.6")
}