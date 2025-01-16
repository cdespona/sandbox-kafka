package io.carles.kafkastream
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class StreamApp {}

fun main(args: Array<String>) {
    SpringApplication.run(StreamApp::class.java, *args)
}
