/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn more about Gradle by exploring our Samples at https://docs.gradle.org/8.12.1/samples
 */
plugins {
    `java-library`
    `jvm-test-suite`
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnlyApi(libs.spring.integration.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.spring.integration.core)
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }
    }
}

publishing {
    repositories {
        maven {
            name = "StagingDeploy"
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }

        mavenLocal()
    }

    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            pom {
                name = "Spring Integration PlantUML Graph"
                description = "A tiny library for generating PlantUML diagrams from Spring Integration Graph"
                url = "https://github.com/pchurzin/spring-integration-plantuml-graph"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit"
                    }
                }
                developers {
                    developer {
                        name = "Pavel Churzin"
                        email = "pchurzin@yandex.ru"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/pchurzin/spring-integration-plantuml-graph.git"
                    developerConnection = "scm:git:ssh://github.com/pchurzin/spring-integration-plantuml-graph.git"
                    url = "https://github.com/pchurzin/spring-integration-plantuml-graph"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
    useGpgCmd()
}

val createMavenCentralBundle = tasks.register<Zip>("createMavenCentralBundle") {
    dependsOn(tasks.named("publishReleasePublicationToStagingDeployRepository"))
    from(layout.buildDirectory.dir("staging-deploy"))
    archiveFileName.set("maven-central-bundle.zip")
}