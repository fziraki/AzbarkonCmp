import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    jacoco
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "abkabk.azbarkon.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.common)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            // 🧠 Ktor Core
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // 🧩 Kotlin Serialization
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.napier)

            implementation(libs.slf4j.nop)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.sqldelight.runtime)
            implementation(libs.paging.common)
            implementation(libs.paging.compose)
            implementation(libs.kotlinx.datetime)

        }
        val androidHostTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit5)
                implementation(libs.assertk)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.paging.testing)
                implementation(libs.sqlite.jdbc)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.paging.testing)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    lintChecks(libs.compose.lint.checks)
}

sqldelight {
    databases {
        create("SarvDatabase") {
            packageName.set("com.sarv.db")
            srcDirs.from("src/commonMain/sqldelight/sarv")
        }
        create("MemorizationDatabase") {
            packageName.set("com.azbarkon.memorization")
            srcDirs.from("src/commonMain/sqldelight/memorization")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    forkEvery = 1
    extensions.configure(JacocoTaskExtension::class) {
        isEnabled = true
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testAndroidHostTest")

    sourceDirectories.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
    )
    classDirectories.setFrom(
        fileTree("build/tmp/kotlin-classes/androidHostTest") { include("**/*.class") }
    )
    executionData.setFrom(
        fileTree("build") {
            include("**/*.exec")
            include("**/*.ec")
        }
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}


