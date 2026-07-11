import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(FileInputStream(f))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace  = "com.gokcank.optidoc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gokcank.optidoc"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 2
        versionName   = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["ADMOB_APP_ID"] = localProperties.getProperty("ADMOB_APP_ID")
            ?: "ca-app-pub-3940256099942544~3347511713"
        val bannerId = localProperties.getProperty("ADMOB_BANNER_ID")
            ?: "ca-app-pub-3940256099942544/6300978111"
        val interstitialId = localProperties.getProperty("ADMOB_INTERSTITIAL_ID")
            ?: "ca-app-pub-3940256099942544/1033173712"
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$bannerId\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitialId\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../optidoc.jks")
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD") ?: System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "optidoc"
            keyPassword = localProperties.getProperty("KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose     = true
        buildConfig = true
        aidl        = false
        shaders     = false
    }

    // Room ÅŸema dÄ±ÅŸa aktarma dizini
    room {
        schemaDirectory("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // ---------- Compose BOM ----------
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ---------- Core ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)

    // ---------- Compose ----------
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ---------- Lifecycle / ViewModel ----------
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ---------- Navigation Compose ----------
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // ---------- Hilt ----------
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    compileOnly("com.google.errorprone:error_prone_annotations:2.26.1")

    // ---------- Room ----------
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ---------- Coroutines ----------
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // ---------- ML Kit ----------
    // Document Scanner: Google Maven Ã¼zerinden gelir (play-services-mlkit-*)
    implementation(libs.mlkit.document.scanner)
    // Text Recognition v2
    implementation(libs.mlkit.text.recognition)

    // ---------- Coil ----------
    implementation(libs.coil.compose)

    // ---------- AdMob ----------
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    // ---------- Test ----------
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}


