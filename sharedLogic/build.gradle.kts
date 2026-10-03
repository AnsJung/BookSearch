import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }
    
    android {
       namespace = "com.example.booksearch.sharedLogic"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// 실제 카카오 서버 테스트는 명시적으로 선택한 경우에만 실행한다.
val runKakaoIntegrationTest = providers.gradleProperty("runKakaoIntegrationTest")
    .map { it.toBoolean() }
    .orElse(false)
    .get()

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    val integrationTest = "com.example.booksearch.data.remote.KakaoBookApiIntegrationTest"
    if (runKakaoIntegrationTest) {
        filter { includeTestsMatching(integrationTest) }
        systemProperty("booksearch.projectRoot", rootProject.projectDir.absolutePath)
        outputs.upToDateWhen { false }
        outputs.doNotCacheIf("실제 서버 응답은 실행할 때마다 확인한다") { true }
        testLogging.showStandardStreams = true
    } else {
        filter { excludeTestsMatching(integrationTest) }
    }
}
