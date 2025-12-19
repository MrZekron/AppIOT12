// app/build.gradle.kts
// Proyecto: Agua Segura
// Módulo: app
// Mapas GRATIS con OpenStreetMap (OSMDroid)
// ❌ SIN Google Maps
// ❌ SIN tarjeta
// ✅ 100% open-source

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // SOLO para Firebase
}

android {
    namespace = "com.example.appiot12"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appiot12"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // ===============================
    // ANDROIDX / UI
    // ===============================
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // ===============================
    // FIREBASE (BOM)
    // ===============================
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")

    // ===============================
    // 🗺️ MAPAS GRATIS (OpenStreetMap)
    // ===============================
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ===============================
    // GRÁFICOS (sensores)
    // ===============================
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // ===============================
    // TESTING
    // ===============================
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
