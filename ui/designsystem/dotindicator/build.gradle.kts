plugins {
    id("beep.android.library.compose")
}

android {
    namespace = "com.lighthouse.beep.ui.designsystem.dotindicator"
}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
}