@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")

include(":compoKit")
include(":compoKit-bubble")
include(":compoKit-common")

rootProject.name = "ProjectCompoKit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")