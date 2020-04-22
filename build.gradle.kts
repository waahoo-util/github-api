plugins {
  kotlin("jvm") version "1.3.71"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  application
}

group = "com.github.waahoo"
version = "0.0.2"

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("com.github.waahoo-util:http-stack:0.0.8")
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


