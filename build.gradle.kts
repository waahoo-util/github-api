plugins {
  `maven-publish`
  kotlin("jvm") version "1.3.71"
  kotlin("plugin.serialization") version "1.3.71"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  application
}

group = "com.github.waahoo-util"
version = "0.0.6"

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
  mavenLocal()
}

dependencies {
  val serializationVer = "0.20.0"
  implementation(kotlin("stdlib-jdk8"))
  implementation("com.github.waahoo-util:http-stack:1.0.5")
  api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVer")
  
}

application {
  mainClassName = "com.github.waahoo.MainKt"
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}

val kotlinSourcesJar by tasks

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["kotlin"])
      artifact(kotlinSourcesJar)
    }
  }
}


