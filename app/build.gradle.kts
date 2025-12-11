// app/build.gradle.kts (versión corregida - opción A)
plugins {
    // usa el alias definido en libs.versions.toml (no repetir el mismo plugin con id())
    alias(libs.plugins.android.application) // <--- deja este si existe en tu catalogo
    id("com.google.gms.google-services")    // plugin de Google (si lo necesitas aquí)
}

android {
    namespace = "com.example.appiot12"
    compileSdk = 34 // ajusta al SDK que tengas instalado (34/35/36)

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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase: con BOM NO pongas versiones individuales en las librerías gestionadas por el BOM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics") // versión tomada del BOM
    implementation("com.google.firebase:firebase-database")  // versión tomada del BOM

    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")


}
