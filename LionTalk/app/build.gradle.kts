plugins {
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.liontalk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.liontalk"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    /*
    HiveMQ MQTT Client는 내부적으로 Netty 네트워크 라이브러리를 사용합니다.
    Netty 관련 라이브러리를 포함하면 다음과 같은 리소스 충돌 문제가 발생할 수 있습니다:
    */
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //jetpack compose
    // ───────────────────────────────
    // Jetpack Compose 관련 (UI 및 상태 관리)
    // ───────────────────────────────

    // Compose Navigation: 화면 간 이동 구현 (예: 채팅방 목록 → 채팅방 상세)
    implementation(libs.compose.nav)

    // 코루틴: 비동기 처리 (예: 메시지 송수신, 로컬 DB 저장 등)
    implementation(libs.coroutines.core)

    // ViewModel과 Compose 연동 - Compose에서 ViewModel 상태를 관찰 가능
    implementation(libs.lifecycle.viewmodel.compose)

    // ViewModel에서 코루틴 사용을 위한 확장 - viewModelScope 사용 가능
    implementation(libs.lifecycle.viewmodel.ktx)


    // ───────────────────────────────
    // 로컬 저장소 관련 (Room + DataStore)
    // ───────────────────────────────

    // Room DB 런타임 - 채팅방 및 메시지 로컬 저장소 역할
    implementation(libs.room.runtime)

    // DataStore Preferences - 설정화면에서 사용자 정보 저장 (예: 닉네임)
    implementation(libs.datastore.preferences)

    // LiveData → Compose 연동용 - Room의 LiveData를 Compose에서 사용 가능
    implementation(libs.androidx.runtime.livedata)


    // ───────────────────────────────
    // 네트워크 통신 (REST API - retrofit)
    // ───────────────────────────────

    // Retrofit Core - 서버 API 호출을 위한 기본 클라이언트
    implementation(libs.retrofit.core)

    // Retrofit + Gson 변환기 - JSON 데이터를 객체로 변환
    implementation(libs.retrofit.gson)

    // Gson - JSON 파싱용 기본 라이브러리
    implementation(libs.gson)

    // OkHttp LoggingInterceptor - 네트워크 요청/응답 로그 출력용 (디버깅에 유용)
    implementation(libs.okhttp.logging)


    // ───────────────────────────────
    // MQTT (실시간 메시지 통신)
    // ───────────────────────────────

    // HiveMQ MQTT Client - 실시간 채팅 메시지 송/수신 기능 구현
    implementation(libs.hivemq.mqtt.client)


    // ───────────────────────────────
    // 기타 (이미지 로딩 등)
    // ───────────────────────────────

    // Coil for Compose - 채팅방 프로필 이미지 등의 비동기 이미지 로딩
    implementation(libs.coil.compose)


    // ───────────────────────────────
    // Annotation Processor (Room DAO 자동 생성)
    // ───────────────────────────────

    // Room 컴파일러 - @Dao, @Entity 등 Room 관련 코드 생성 시 필요
    kapt(libs.room.compiler)
}