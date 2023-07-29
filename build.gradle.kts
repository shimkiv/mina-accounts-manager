val ktorVersion: String by project
val kotlinVersion: String by project
val kotlinxSerializationVersion: String by project

plugins {
  kotlin("jvm") version "1.9.0"
  id("io.ktor.plugin") version "2.3.2"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
  id("org.graalvm.buildtools.native") version "0.9.23"
}

group = "xyz.p42"
version = "0.1.0"
application {
  mainClass.set("xyz.p42.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

graalvmNative {
  binaries {
    named("main") {
      fallback.set(false)
      verbose.set(true)

      buildArgs.add("--enable-url-protocols=http")
      buildArgs.add("--initialize-at-build-time=io.ktor,kotlin,kotlinx,org.slf4j.LoggerFactory")
      buildArgs.add("-H:+InstallExitHandlers")
      buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
      buildArgs.add("-H:+ReportExceptionStackTraces")
      buildArgs.add("-H:IncludeResources=logging.properties")

      imageName.set("accounts-manager")
    }
  }
}
