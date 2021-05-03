object Dependencies {
    const val activity = "androidx.activity:activity-ktx:${Versions.activity}"
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val biometric = "androidx.biometric:biometric:${Versions.biometric}"
    const val cardView = "androidx.cardview:cardview:${Versions.cardView}"
    const val coil = "io.coil-kt:coil:${Versions.coil}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val documentFile = "androidx.documentfile:documentfile:${Versions.documentFile}"
    const val dataStore = "androidx.datastore:datastore:${Versions.dataStore}"
    const val hamcrest = "org.hamcrest:hamcrest-all:${Versions.hamcrest}"
    const val inject = "javax.inject:javax.inject:${Versions.inject}"
    const val junit = "junit:junit:${Versions.junit}"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    const val photoView = "com.github.chrisbanes:PhotoView:${Versions.photoView}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val savedState = "androidx.savedstate:savedstate-ktx:${Versions.savedState}"
    const val security = "androidx.security:security-crypto:${Versions.security}"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val transition = "androidx.transition:transition-ktx:${Versions.transition}"
    const val vectorDrawable = "androidx.vectordrawable:vectordrawable:${Versions.vectorDrawable}"
    const val webkit = "androidx.webkit:webkit:${Versions.webkit}"

    object AndroidTest {
        const val core = "androidx.test:core:${Versions.androidTest}"
        const val coreKtx = "androidx.test:core-ktx:${Versions.androidTest}"
        const val extJunit = "androidx.test.ext:junit:${Versions.androidTestJunit}"
        const val runner = "androidx.test:runner:${Versions.androidTest}"
        const val rules = "androidx.test:rules:${Versions.androidTest}"
    }

    object ArchCore {
        const val runtime = "androidx.arch.core:core-runtime:${Versions.archCore}"
        const val testing = "androidx.arch.core:core-testing:${Versions.archCore}"
    }

    object Camera {
        const val core = "androidx.camera:camera-core:${Versions.cameraX}"
        const val camera2 = "androidx.camera:camera-camera2:${Versions.cameraX}"
        const val lifecycle = "androidx.camera:camera-lifecycle:${Versions.cameraX}"
        const val view = "androidx.camera:camera-view:${Versions.cameraXExt}"
        const val extensions = "androidx.camera:camera-extensions:${Versions.cameraXExt}"
    }

    object Compose {
        const val compiler = "androidx.compose.compiler:compiler:${Versions.compose}"
        const val runtime = "androidx.compose.runtime:runtime:${Versions.compose}"
        const val foundation = "androidx.compose.foundation:foundation:${Versions.compose}"
        const val `foundation-layout` = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
        const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        const val `ui-tooling` = "androidx.compose.ui:ui-tooling:${Versions.compose}"
        const val `ui-test` = "androidx.compose.ui:ui-test-junit4:${Versions.compose}"
        const val material = "androidx.compose.material:material:${Versions.compose}"
        const val `material-icons-core` = "androidx.compose.material:material-icons-core:${Versions.compose}"
        const val `material-icons-extended` = "androidx.compose.material:material-icons-extended:${Versions.compose}"
    }

    object Dagger {
        const val runtime = "com.google.dagger:dagger:${Versions.dagger}"
        const val compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"

        object Hilt {
            const val plugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.dagger}"
            const val runtime = "com.google.dagger:hilt-android:${Versions.dagger}"
            const val testing = "com.google.dagger:hilt-android-testing:${Versions.dagger}"
            const val compiler = "com.google.dagger:hilt-android-compiler:${Versions.dagger}"

            object Jetpack {
                const val lifecycleViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltJetpack}"
                const val compiler = "androidx.hilt:hilt-compiler:${Versions.hiltJetpack}"
            }
        }
    }

    object Espresso {
        const val core = "androidx.test.espresso:espresso-core:${Versions.espresso}"
        const val idlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"
        const val contrib = "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
        const val intents = "androidx.test.espresso:espresso-intents:${Versions.espresso}"
    }

    object ExoPlayer {
        const val runtime = "com.google.android.exoplayer:exoplayer:${Versions.exoPlayer}"
        const val core = "com.google.android.exoplayer:exoplayer-core:${Versions.exoPlayer}"
        const val dash = "com.google.android.exoplayer:exoplayer-dash:${Versions.exoPlayer}"
        const val hls = "com.google.android.exoplayer:exoplayer-hls:${Versions.exoPlayer}"
        const val smoothstreaming = "com.google.android.exoplayer:exoplayer-smoothstreaming:${Versions.exoPlayer}"
        const val ui = "com.google.android.exoplayer:exoplayer-ui:${Versions.exoPlayer}"
    }

    object Fragment {
        const val runtime = "androidx.fragment:fragment-ktx:${Versions.fragment}"
        const val testing = "androidx.fragment:fragment-testing:${Versions.fragment}"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
        const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }

    object Lifecycle {
        const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
        const val java8 = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
        const val compiler = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
        const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
        const val process = "androidx.lifecycle:lifecycle-process:${Versions.lifecycle}"
    }

    object Mockito {
        const val core = "org.mockito:mockito-core:${Versions.mockito}"
        const val android = "org.mockito:mockito-android:${Versions.mockito}"
    }

    object Moshi {
        const val runtime = "com.squareup.moshi:moshi:${Versions.moshi}"
        const val kotlinCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
        const val adapters = "com.squareup.moshi:moshi-adapters:${Versions.moshi}"
    }

    object Navigation {
        const val runtime = "androidx.navigation:navigation-runtime-ktx:${Versions.navigation}"
        const val fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
        const val ui = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
        const val safeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation}"
        const val testing = "androidx.navigation:navigation-testing:${Versions.navigation}"
    }

    object OkHttp {
        const val runtime = "com.squareup.okhttp3:okhttp:${Versions.okHttp}"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okHttp}"
    }

    object Retrofit {
        const val runtime = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val mock = "com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}"
        const val moshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    }

    object Room {
        const val runtime = "androidx.room:room-runtime:${Versions.room}"
        const val compiler = "androidx.room:room-compiler:${Versions.room}"
        const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
        const val testing = "androidx.room:room-testing:${Versions.room}"
    }

    object Work {
        const val runtime = "androidx.work:work-runtime-ktx:${Versions.work}"
        const val testing = "androidx.work:work-testing:${Versions.work}"
    }
}
