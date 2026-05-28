// app/build.gradle.kts
// =====================================================
// 📦 Módulo app del proyecto Agua Segura
// =====================================================
// ¿Qué hace este archivo?
// 1. Configura el módulo principal de Android
// 2. Activa Firebase con Google Services
// 3. Define versiones de SDK
// 4. Configura compatibilidad con Java 11
// 5. Agrega librerías de UI, Firebase, mapas, gráficos, PDF y correo
// 6. Agrega Google Places para autocompletado de direcciones reales
//
// IMPORTANTE:
// - Se usa OpenStreetMap con OSMDroid para mapas
// - Se usa Google Places SOLO para autocompletar direcciones reales
// - La API Key de Google Places se puede leer desde strings.xml
// =====================================================

plugins {
    alias(libs.plugins.android.application) // Plugin principal Android
    id("com.google.gms.google-services")    // Plugin de Firebase / Google Services
}

android {
    namespace = "com.example.appiot12"
    compileSdk = 35

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

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/NOTICE-notice.md"
        }
    }
}

dependencies {

    // ===============================
    // 📱 ANDROIDX / UI
    // ===============================
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // ===============================
    // ☁️ FIREBASE (BOM)
    // ===============================
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // ===============================
    // 🗺️ MAPAS GRATIS (OpenStreetMap)
    // ===============================
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ===============================
    // 📍 GOOGLE PLACES
    // ===============================
    // Esta librería permite autocompletar direcciones reales.
    implementation("com.google.android.libraries.places:places:3.5.0")

    // ===============================
    // 📈 GRÁFICOS (sensores)
    // ===============================
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // ===============================
    // 📄 PDF
    // ===============================
    implementation("com.itextpdf:itext7-core:7.2.5")

    // ===============================
    // 🤖 WORKMANAGER
    // ===============================
    implementation(libs.androidx.work.runtime)

    // ===============================
    // 📧 JAVAMAIL
    // ===============================
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // ===============================
    // ⚙️ GOOGLE TASKS
    // ===============================
    // Esta librería es para tareas de Google Play Services.
    implementation("com.google.android.gms:play-services-tasks:18.2.0")

    // ===============================
    // 🧪 TESTING
    // ===============================
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}