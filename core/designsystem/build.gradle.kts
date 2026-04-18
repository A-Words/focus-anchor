plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.focusanchor.core.designsystem"
        compileSdk = 36
        minSdk = 26
        withHostTestBuilder {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation(compose.preview)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}
