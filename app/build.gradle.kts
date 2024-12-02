// app/build.gradle.kts (Module-Level)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.quizapp"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.quizapp"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = project.findProperty("MYAPP_RELEASE_KEY_ALIAS") as String? ?: "quizapp_alias"
            keyPassword = project.findProperty("MYAPP_RELEASE_KEY_PASSWORD") as String? ?: "your_key_password"
            storeFile = file(project.findProperty("MYAPP_RELEASE_STORE_FILE") as String? ?: "path/to/my-release-key.keystore")
            storePassword = project.findProperty("MYAPP_RELEASE_STORE_PASSWORD") as String? ?: "your_store_password"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // Set to true to enable code shrinking (e.g., ProGuard/R8)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // For HTTP requests
    // Add any other dependencies you need
}
