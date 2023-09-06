plugins {
    id("com.android.application")
    kotlin("plugin.serialization") version "1.9.0"
    kotlin("android")
}

val composeVersion = "1.6.0-alpha04"//extra["compose.version"] as String
val composeCompilerVersion = "1.5.2"
val lifecycleVersion = "2.6.1"

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "de.mr_pine.recipes.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
    packagingOptions {
        resources.excludes += "META-INF/AL2.0"
        resources.excludes += "META-INF/LGPL2.1"
    }
    namespace = "de.mr_pine.recipes.android"
}

dependencies {
    implementation(project(":common"))

    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha12")

    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material3:material3:1.2.0-alpha06")

    implementation("com.google.android.material:material:1.11.0-alpha02")

    implementation("androidx.navigation:navigation-compose:2.7.1")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha01")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")

    implementation("com.google.accompanist:accompanist-flowlayout:0.26.4-beta")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("net.pwall.json:json-kotlin-schema:0.36")

    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.2")
}