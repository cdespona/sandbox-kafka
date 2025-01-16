import org.springframework.boot.gradle.tasks.bundling.BootJar
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `jvm-test-suite`
    id("se.patrikerdes.use-latest-versions") version "0.2.+"
    id("com.github.ben-manes.versions") version "0.51.+"
    id("org.springframework.boot") version "3.4.+"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.7.+"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

tasks.processResources {
    expand(project.properties)
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.4.+"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")

    //Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams:7.8.0-ce")
    implementation("io.confluent:kafka-streams-avro-serde:7.8.0")
    implementation("org.apache.avro:avro:1.12.+")
    testImplementation("commons-codec:commons-codec:1.17.2")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("io.confluent:kafka-avro-serializer:7.8.0")
    testImplementation("org.testcontainers:kafka:1.20.4")

}

tasks.withType<BootJar> {
    archiveClassifier.set("boot")
    archiveFileName.set("kafka-stream-${archiveVersion.orNull ?: "none"}-${archiveClassifier.orNull ?: ""}.${archiveExtension.orNull ?: "jar"}")
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
                    implementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
                    implementation("io.kotest:kotest-assertions-core:5.9.1")
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
