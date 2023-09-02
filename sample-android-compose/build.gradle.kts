@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  // Used for Compose Destinations(https://github.com/raamcosta/compose-destinations)
  alias(libs.plugins.ksp)
}

android {
  compileSdk = android_target_sdk_version

  defaultConfig {
    applicationId = "com.mobilejazz.kmmsample.mvi"
    minSdk = android_min_sdk_version
    targetSdk = android_target_sdk_version
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  namespace = "com.mobilejazz.kmmsample.mvi"

  applicationVariants.all {
    kotlin.sourceSets {
      getByName(name) {
        kotlin.srcDir("build/generated/ksp/$name/kotlin")
      }
    }
  }
}

dependencies {
  implementation(project(":sample-core"))

  implementation(libs.coreKtx)
  implementation(libs.composeUi)
  implementation(libs.composeMaterial)
  implementation(libs.composeUiToolingPreview)
  implementation(libs.lifecycleRuntimeKtx)
  implementation(libs.lifecycleViewModelCompose)
  implementation(libs.activityCompose)
  implementation(libs.composeDestinations)
  ksp(libs.composeDestinationsKsp)
}

tasks.preBuild {
  dependsOn(tasks.ktlintFormat)
}
