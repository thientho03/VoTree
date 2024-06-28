import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.votree"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.votree"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        val keystoreFile = rootProject.file("apis.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        buildConfigField("String", "PLACES_API_KEY", properties.getProperty("PLACES_API_KEY") ?: "")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }

    sourceSets {
        getByName("main").java.srcDirs("build/generated/source/navigation-args")
    }

    dataBinding {
        enable = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.11.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-functions-ktx:20.4.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    implementation("com.google.android.gms:play-services-auth:21.1.0")
    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.multidex:multidex:2.0.1")

    val nav_version = "2.7.7"
    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    // Declare the dependencies for the Firebase Cloud Firestore and Analytics libraries
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:17.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Activity KTX
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Places API
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.23"))
    implementation("com.google.android.libraries.places:places:3.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Stripe Android SDK
    implementation("com.stripe:stripe-android:20.41.0")

    //
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // OTP View
    implementation("io.github.chaosleung:pinview:1.4.4")

    //bar chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Search Algorithm
//    annotationProcessor("info.debatty:java-string-similarity:2.0.0")
////    implementation("info.debatty:java-string-similarity:2.0.0"){
////        exclude(group = "com.github.stephenc.jcip", module = "jcip-annotations")
////    }
//    implementation("info.debatty:java-string-similarity:2.0.0"){
//        exclude(module = "jsr305")
//        exclude(module = "jcip-annotations")
//    }

    // Tab for sort
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Slide bar for Filter price
//    implementation("com.github.Jay-Goo:RangeSeekBar:v3.0.0")

    // PhotoView
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
    // Espresso intents for validating and stubbing intents
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    // Espresso web for web view testing
    androidTestImplementation("androidx.test.espresso:espresso-web:3.6.1")
    // Espresso idling resource for handling asynchronous operations
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.6.1")
}

