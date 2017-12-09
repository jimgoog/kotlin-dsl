import build.futureKotlin

plugins {
    id("local-kotlin-dsl-module")
    `maven-publish`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.9.9"
}

base {
    archivesBaseName = "gradle-kotlin-dsl-plugins"
}

dependencies {
    compileOnly(gradleKotlinDsl())

    implementation(futureKotlin("stdlib-jre8"))
    implementation(futureKotlin("gradle-plugin"))
    implementation(futureKotlin("sam-with-receiver"))

    testImplementation(project(":test-fixtures"))
}


// --- Plugins declaration ----------------------------------------------

data class GradlePlugin(val displayName: String, val id: String, val implementationClass: String)

val plugins = listOf(
    GradlePlugin(
        "Embedded Kotlin Gradle Plugin",
        "org.gradle.kotlin.embedded-kotlin",
        "org.gradle.kotlin.dsl.plugins.embedded.EmbeddedKotlinPlugin"),
    GradlePlugin(
        "Gradle Kotlin DSL Plugin",
        "org.gradle.kotlin.kotlin-dsl",
        "org.gradle.kotlin.dsl.plugins.dsl.KotlinDslPlugin"))

plugins.forEach { plugin ->

    gradlePlugin {
        (plugins) {
            plugin.id {
                id = plugin.id
                implementationClass = plugin.implementationClass
            }
        }
    }

    pluginBundle {
        tags = listOf("Kotlin", "DSL")
        website = "https://github.com/gradle/kotlin-dsl"
        vcsUrl = "https://github.com/gradle/kotlin-dsl"
        mavenCoordinates.artifactId = base.archivesBaseName
        (plugins) {
            plugin.id {
                id = plugin.id
                displayName = plugin.displayName
                description = plugin.displayName
            }
        }
    }
}

publishing {
    repositories {
        maven(url = "build/repository") {
            name = "test"
        }
    }
}

val customInstallation by rootProject.tasks
tasks {

    val publishPluginsToTestRepository by creating {
        dependsOn("publishPluginMavenPublicationToTestRepository")
        dependsOn(
            plugins.map {
                "publish${it.id.capitalize()}PluginMarkerMavenPublicationToTestRepository"
            })
    }

    val processTestResources: ProcessResources by getting

    val writeTestProperties by creating(WriteProperties::class) {
        outputFile = File(processTestResources.destinationDir, "test.properties")
        property("version", version)
    }

    processTestResources.dependsOn(writeTestProperties)

    "test" {
        dependsOn(customInstallation)
        dependsOn(publishPluginsToTestRepository)
    }
}
