plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun Project.readStringProperty(name: String): String? {
    return findProperty(name)?.toString()?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }
}

val releaseSigningProperties = mapOf(
    "releaseKeystoreFile" to project.readStringProperty("releaseKeystoreFile"),
    "releaseKeystorePassword" to project.readStringProperty("releaseKeystorePassword"),
    "releaseKeyAlias" to project.readStringProperty("releaseKeyAlias"),
    "releaseKeyPassword" to project.readStringProperty("releaseKeyPassword")
)
val hasCompleteReleaseSigning = releaseSigningProperties.values.all { it != null }
val requireReleaseSigning = project.readStringProperty("requireReleaseSigning")?.toBooleanStrictOrNull() == true

android {
    namespace = "com.example.kalorientracker"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.kalorientracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasCompleteReleaseSigning) {
            create("release") {
                storeFile = file(releaseSigningProperties.getValue("releaseKeystoreFile")!!)
                storePassword = releaseSigningProperties.getValue("releaseKeystorePassword")
                keyAlias = releaseSigningProperties.getValue("releaseKeyAlias")
                keyPassword = releaseSigningProperties.getValue("releaseKeyPassword")
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (hasCompleteReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

}

val validateReleaseSigning = tasks.register("validateReleaseSigning") {
    doLast {
        if (requireReleaseSigning && !hasCompleteReleaseSigning) {
            val missingProperties = releaseSigningProperties
                .filterValues { it == null }
                .keys
                .sorted()
                .joinToString()

            throw GradleException(
                "Release signing is required for release tasks. Missing properties: $missingProperties"
            )
        }
    }
}

tasks.configureEach {
    if (name != "validateReleaseSigning" && name.contains("Release", ignoreCase = true)) {
        dependsOn(validateReleaseSigning)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
tasks.register("ciTest") {
    group = "verification"
    description = "Runs verification tasks that do not require a device or emulator."
    dependsOn("testDebugUnitTest")
}
