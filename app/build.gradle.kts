plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidTarget {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

    jvm("desktop") {
        mainRun {
            mainClass = "hu.riposte.game.MainKt"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.activity.compose)

                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.ui.tooling.preview)
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
                implementation("androidx.navigation:navigation-compose:2.7.7")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("androidx.media3:media3-exoplayer:1.2.0")
                implementation("androidx.media3:media3-ui:1.2.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                implementation("com.googlecode.soundlibs:jorbis:0.0.17.4")
                implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
                implementation("com.googlecode.soundlibs:tritonus-share:0.3.7.4")
                implementation("com.google.code.gson:gson:2.10.1")
            }
        }
    }
}

android {
    namespace = "hu.riposte.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "hu.riposte.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/androidMain/resources")
    sourceSets["main"].assets.srcDirs("src/androidMain/assets")

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
compose.desktop {
    application {
        mainClass = "hu.riposte.game.MainKt"
        nativeDistributions {
            targetFormats( org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                           org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm,
                           org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                           org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "Riposte"
            packageVersion = "1.0.0"
        }
    }
}
