plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {

    namespace = "com.example.edgeviewer"
    compileSdk = 34
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.example.edgeviewer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    // ---------------------- CMAKE CONFIG ----------------------
    externalNativeBuild {
        cmake {
            // Your CMakeLists.txt inside src/main/cpp
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // ---------------------- BUILD TYPES ----------------------
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isJniDebuggable = true
        }
    }

    // ---------------------- JAVA/KOTLIN ----------------------
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // ---------------------- JNI LIBS ----------------------
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jnilibs")   // make sure folder exists
        }
    }

    // ---------------------- PACKAGING ----------------------
    packaging {
        resources {
            pickFirsts.addAll(
                listOf(
                    "**/libopencv_java4.so",
                    "**/libc++_shared.so",
                    "**/libnative-lib.so",
                    "**/libopencv_core.so",
                    "**/libopencv_imgproc.so"
                )
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
