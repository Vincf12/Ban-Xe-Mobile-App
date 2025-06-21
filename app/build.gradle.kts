plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.carsale"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.carsale"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Firebase Authentication
    implementation ("com.google.firebase:firebase-auth:22.0.0")
    // Firebase Firestore
    implementation ("com.google.firebase:firebase-firestore")
    // Firebase Realtime Database (tùy chọn)
    implementation ("com.google.firebase:firebase-database")
    // Material Design
    implementation ("com.google.android.material:material:1.11.0")
    // Google Sign-In
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    // Facebook SDK
    implementation ("com.facebook.android:facebook-login:16.1.3")
    //Splash Screen
    implementation ("androidx.core:core-splashscreen:1.0.1")
    // LottieFile
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation ("com.google.android.gms:play-services-safetynet:18.1.0") // Hoặc phiên bản mới nhất
    implementation ("com.google.firebase:firebase-core:21.1.1")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
}