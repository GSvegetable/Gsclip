plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.gsvegetable.gsclip"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gsvegetable.gsclip"
        minSdk = 33
        targetSdk = 36
        versionCode = 3
        versionName = "0.3.0"
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.github.neoruaa.glasmio:glasmio-scaffold:0.1.2")
    implementation("io.github.neoruaa.glasmio:glasmio-preference:0.1.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
