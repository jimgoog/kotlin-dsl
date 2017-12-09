import build.*

plugins {
    id("local-kotlin-library")
}

dependencies {
    compile(project(":test-fixtures"))
    compile("org.xmlunit:xmlunit-matchers:2.5.1")
}

val customInstallation by rootProject.tasks
tasks {
    "test" {
        dependsOn(customInstallation)
        inputs.dir("../samples")
    }
}

withTestWorkersMemoryLimits()
withParallelTests()
