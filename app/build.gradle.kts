plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.inen_citas_medicas_kotlin"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.inen_citas_medicas_kotlin"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Necesario para leer BuildConfig.DEBUG desde el codigo. Desde AGP 8 la
    // generacion de BuildConfig esta desactivada por defecto.
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            // Sigue en false a proposito. Activar R8 sin reglas de conservacion
            // romperia Gson: ofusca los nombres de campo de los data class, y
            // Gson mapea el JSON por nombre, asi que todos los campos llegarian
            // nulos solo en release. Antes de activarlo hay que escribir las
            // reglas en proguard-rules.pro y probar un APK de release en un
            // dispositivo. Ver el README.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}