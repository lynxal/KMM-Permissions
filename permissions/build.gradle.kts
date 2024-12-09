import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
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
            baseName = "kmmpermissions"
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.androidAppCompat)
                implementation(libs.androidComposeActivity)
                implementation(libs.androidLifecycleRuntime)
            }
        }
        commonMain {
            dependencies {
                //put your multiplatform dependencies here
                implementation(libs.coroutinesCore)
                implementation(compose.runtime)
                implementation(libs.logging)
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
    namespace = "com.lynxal.kmmpermissions"
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

    coordinates("com.lynxal.permissions", "permissions", "0.0.3")
    pom {
        name.set("KMM Permissions")
        description.set("A Kotlin Multiplatform Mobile (KMM) library for managing permissions in Android and iOS applications, designed with Jetpack Compose in mind and optimized for modern platforms.")
        url.set("https://github.com/lynxal/KMM-Permissions")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/lynxal/KMM-Permissions/blob/main/LICENSE")
            }
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/lynxal/KMM-Permissions/issues")
        }

        developers {
            developer {
                id.set("VardanK")
                name.set("Vardan Kurkchiyan")
                email.set("central.repo@Lynxal.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com:lynxal/KMM-Permissions.git")
            developerConnection.set("scm:git:ssh://github.com:lynxal/KMM-Permissions.git")
            url.set("https://github.com/lynxal/KMM-Permissions")
        }
    }
}
