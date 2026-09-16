plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("signing")
}

kotlin {
    jvmToolchain((findProperty("jvm.version") as String).toInt())

    android {
        namespace = "com.lynxal.kmmpermissions"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt())
        }
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Registers the host-test compilation. com.android.library created the unit-test
        // variant automatically; this plugin does not, so without this the module's tests
        // silently stop running rather than failing.
        withHostTestBuilder { }.configure { }
    }
    listOf(
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
                implementation(libs.android.appCompat)
                implementation(libs.androidx.activityCompose)
                implementation(libs.android.lifecycleRuntime)
            }
        }
        commonMain {
            dependencies {
                //put your multiplatform dependencies here
                implementation(libs.coroutines.core)
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

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.lynxal.permissions", "permissions", "0.0.7")
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
