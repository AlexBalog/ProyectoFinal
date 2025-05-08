plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.proyectofinalandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyectofinalandroid"
        minSdk = 33
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/io.netty.versions.properties"
            )
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.animation.core.lint)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.hilt.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.compose.material:material-icons-core:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // ========================
    // Dependencias de Retrofit:
    // ========================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Conversor JSON a objetos Kotlin (Gson):
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // (Opcional) Logging Interceptor para depurar las peticiones HTTP:
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
}