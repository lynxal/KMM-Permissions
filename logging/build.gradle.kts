import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("signing")
}

kotlin {
    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "logging"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.lynxal.logging"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompatibility.get()
    }

    kotlin {
        jvmToolchain((findProperty("jvm.version") as String).toInt())
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("com.lynxal.logging", "logging", "0.0.1")
    pom {
        name.set("KMM Logging")
        description.set("A lightweight and flexible logging library for Kotlin Multiplatform Mobile (KMM) projects. It provides platform-specific logging implementations for Android and iOS, with an easy-to-use API and customizable log levels. Designed to integrate seamlessly into KMM applications.")
        url.set("https://github.com/lynxal/KMM-Logging")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/lynxal/KMM-Logging/blob/main/LICENSE")
            }
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/lynxal/KMM-Logging/issues")
        }

        developers {
            developer {
                id.set("VardanK")
                name.set("Vardan Kurkchiyan")
                email.set("central.repo@Lynxal.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com:lynxal/KMM-Logging.git")
            developerConnection.set("scm:git:ssh://github.com:lynxal/KMM-Logging.git")
            url.set("https://github.com/lynxal/KMM-Logging")
        }
    }
}