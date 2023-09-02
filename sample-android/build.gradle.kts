plugins {
  id("com.android.application")
  kotlin("android")
}

dependencies {
  implementation(project(":sample-core"))

  implementation(libs.material)
  implementation(libs.appCompat)
  implementation(libs.constraintLayout)
}

android {
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  packaging {
    resources.excludes.add("META-INF/licenses/**")
    resources.excludes.add("META-INF/AL2.0")
    resources.excludes.add("META-INF/LGPL2.1")
  }

  compileSdk = android_target_sdk_version
  defaultConfig {
    applicationId = "com.mobilejazz.kmmsample.android"
    minSdk = android_min_sdk_version
    targetSdk = android_target_sdk_version
    versionCode = 1
    versionName = "1.0"
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  buildFeatures {
    viewBinding = true
  }
  namespace = "com.mobilejazz.kmmsample.application"
}

tasks.preBuild {
  dependsOn(tasks.ktlintFormat)
}
