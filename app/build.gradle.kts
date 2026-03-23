// app/build.gradle.kts
// 📦 Módulo app del proyecto Agua Segura
// 🚀 Configuración principal del módulo Android
// 🗺️ Usa OpenStreetMap con OSMDroid
// ☁️ Usa Firebase
// 📧 Incluye soporte para WorkManager + envío de correo temporal con JavaMail

plugins {
    alias(libs.plugins.android.application) // 📦 Plugin principal de aplicación Android
    id("com.google.gms.google-services") // ☁️ Plugin de Google Services para Firebase
}

android {
    namespace = "com.example.appiot12" // 🏷️ Namespace del proyecto
    compileSdk = 34 // 🔨 SDK de compilación

    defaultConfig {
        applicationId = "com.example.appiot12" // 📱 ID único de la app
        minSdk = 24 // 📉 Versión mínima de Android soportada
        targetSdk = 34 // 🎯 SDK objetivo
        versionCode = 1 // 🔢 Versión interna
        versionName = "1.0" // 🏷️ Versión visible
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 🧪 Runner de tests instrumentados
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 🚫 No minificar en release por ahora
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // ⚙️ Config base de ProGuard
                "proguard-rules.pro" // 🛡️ Reglas personalizadas
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // ☕ Compatibilidad Java 11
        targetCompatibility = JavaVersion.VERSION_11 // ☕ Objetivo Java 11
    }
}

dependencies {

    // ===============================
    // 📱 ANDROIDX / UI
    // ===============================
    implementation(libs.appcompat) // 🧱 Compatibilidad de AppCompat
    implementation(libs.material) // 🎨 Material Design
    implementation(libs.activity) // 🖥️ Soporte Activity
    implementation(libs.constraintlayout) // 📐 Layout flexible
    implementation(libs.gridlayout) // 🧩 GridLayout

    // ===============================
    // ☁️ FIREBASE (BOM)
    // ===============================
    implementation(platform("com.google.firebase:firebase-bom:34.5.0")) // 📦 BOM de Firebase
    implementation("com.google.firebase:firebase-auth") // 👤 Autenticación
    implementation("com.google.firebase:firebase-database") // 🗄️ Realtime Database
    implementation("com.google.firebase:firebase-analytics") // 📊 Analytics

    // ===============================
    // 🗺️ MAPAS GRATIS (OpenStreetMap)
    // ===============================
    implementation("org.osmdroid:osmdroid-android:6.1.18") // 🌍 Mapas con OSMDroid

    // ===============================
    // 📈 GRÁFICOS (sensores)
    // ===============================
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0") // 📊 Gráficos para sensores

    // ===============================
    // 📄 PDF
    // ===============================
    implementation("com.itextpdf:itext7-core:7.2.5") // 🧾 Generación de PDF

    // ===============================
    // 🤖 WORKMANAGER
    // ===============================
    implementation(libs.androidx.work.runtime) // 🔄 Tareas en segundo plano

    // ===============================
    // 📧 JAVAMAIL (envío temporal de correos desde Android)
    // ===============================
    implementation("com.sun.mail:android-mail:1.6.7") // 📨 Soporte mail
    implementation("com.sun.mail:android-activation:1.6.7") // ⚙️ Activación MIME
    implementation("com.google.android.gms:play-services-tasks:18.2.0") // ⏳ Tasks.await para Firebase

    // ===============================
    // 🧪 TESTING
    // ===============================
    testImplementation(libs.junit) // ✅ Tests unitarios
    androidTestImplementation(libs.ext.junit) // ✅ Tests instrumentados JUnit
    androidTestImplementation(libs.espresso.core) // ✅ Espresso UI tests
}