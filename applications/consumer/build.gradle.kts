import org.springframework.boot.gradle.tasks.bundling.BootJar
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `jvm-test-suite`
    id("se.patrikerdes.use-latest-versions") version "0.2.+"
    id("com.github.ben-manes.versions") version "0.51.+"
    id("org.springframework.boot") version "3.3.+"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.7.+"
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

tasks.processResources {
    expand(project.properties)
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.3.+"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:4.0.+")
    //Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.avro:avro:1.12.+")
    implementation("io.confluent:kafka-avro-serializer:7.7.+")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    }

tasks.withType<BootJar> {
    archiveClassifier.set("boot")
    archiveFileName.set("kafka-consumer-${archiveVersion.orNull ?: "none"}-${archiveClassifier.orNull ?: ""}.${archiveExtension.orNull ?: "jar"}")
}
tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

group = "io.carles"

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
}

kotlin.compilerOptions {
    jvmTarget = JvmTarget.JVM_21
}

tasks.named("generateAvroJava", GenerateAvroJavaTask::class) {
    source("src/main/avro")
    setOutputDir(file("src/main/java"))
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter("5.9.+")
                dependencies {
                    implementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
                    implementation("io.kotest:kotest-assertions-core:5.8.0")
                    implementation("io.mockk:mockk:1.13.+")
                    }
            }
        }
        val test by getting(JvmTestSuite::class)
        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}
