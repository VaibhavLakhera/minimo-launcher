plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.room")
}

android {
    namespace = "com.minimo.launcher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.minimo.launcher"
        minSdk = 26
        targetSdk = 37
        versionCode = 144
        versionName = "1.35.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    flavorDimensions += "store"
    productFlavors {
        create("foss") {
            dimension = "store"
        }
        create("playStore") {
            dimension = "store"
        }
    }

    buildTypes {
        debug {
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

val playStoreImplementation by configurations

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-graphics")

    implementation("androidx.appcompat:appcompat:1.8.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.navigation:navigation-compose:2.10.0")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-compiler:2.60.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")

    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")

    implementation("androidx.datastore:datastore-preferences:1.2.1")

    implementation("com.jakewharton.timber:timber:5.0.1")

    playStoreImplementation("com.android.billingclient:billing-ktx:9.1.0")
    playStoreImplementation("com.google.android.play:review-ktx:2.0.2")
}
