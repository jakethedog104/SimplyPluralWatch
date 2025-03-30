import java.io.File
import java.io.FileInputStream
import java.util.*

val prop = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}
val apiKey: String = prop.getProperty("apiKey")
val spURI: String = prop.getProperty("spURI")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.0"
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.example.simplypluralwatch"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.simplypluralwatch"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            buildConfigField("String", "apiKey", apiKey)
            buildConfigField("String", "spURI", spURI)
        }
        release  {
            buildConfigField("String", "apiKey", apiKey)
            buildConfigField("String", "spURI", spURI)

            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.compose.compiler
                .get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)

    // Request ans JSON libs
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Serialization & Deserialization libs
    implementation(libs.kotlinx.serialization.json)

    // Tiles
    implementation(libs.androidx.tiles)
    implementation(libs.horologist.tiles)
    debugImplementation(libs.androidx.tiles.tooling)
    debugImplementation(libs.androidx.tiles.tooling.preview)

    // General compose dependencies
    implementation(composeBom)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose for Wear OS Dependencies
    // NOTE: DO NOT INCLUDE a dependency on androidx.compose.material:material.
    // androidx.wear.compose:compose-material is designed as a replacement not an addition to
    // androidx.compose.material:material. If there are features from that you feel are missing from
    // androidx.wear.compose:compose-material please raise a bug to let us know:
    // https://issuetracker.google.com/issues/new?component=1077552&template=1598429&pli=1
    implementation(libs.wear.compose.material)

    // Foundation is additive, so you can use the mobile version in your Wear OS app.
    implementation(libs.wear.compose.foundation)
    implementation(libs.androidx.material.icons.core)

    // Compose for Wear OS Dependencies
    // NOTE: DO NOT INCLUDE a dependency on androidx.compose.material:material.
    // androidx.wear.compose:compose-material is designed as a replacement not an addition to
    // androidx.compose.material:material. If there are features from that you feel are missing from
    // androidx.wear.compose:compose-material please raise a bug to let us know:
    // https://issuetracker.google.com/issues/new?component=1077552&template=1598429&pli=1
    implementation(libs.wear.compose.material)

    // Horologist for correct Compose layout
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)

    // Preview Tooling
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)

    // If you are using Compose Navigation, use the Wear OS version (NOT the
    // androidx.navigation:navigation-compose version), that is, uncomment the line below.
    implementation(libs.wear.compose.navigation)

    implementation(libs.androidx.ui.test.manifest)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.horologist.roboscreenshots)

    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(composeBom)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(composeBom)
}