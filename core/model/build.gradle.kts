plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.focusanchor.core.model"
        compileSdk = 36
        minSdk = 26
        withHostTestBuilder {}
    }
}
