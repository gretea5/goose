plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.ssafy.firstproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ssafy.firstproject"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Glide 사용
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // Retrofit
    // https://github.com/square/retrofit
    implementation(libs.retrofit)
    // https://github.com/square/okhttp
    implementation(libs.okhttp)
    // https://github.com/square/retrofit/tree/master/retrofit-converters/gson
    implementation(libs.converter.gson)
    // https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor
    implementation(libs.logging.interceptor)

    // Jetpack Navigation Kotlin
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //framework ktx dependency
    implementation(libs.androidx.fragment.ktx)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // BlurView
    implementation(libs.blurview)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit
    implementation(libs.text.recognition.korean)
    implementation(libs.text.recognition.korean.v1600beta5)

    // Lottie
    implementation(libs.lottie)
}