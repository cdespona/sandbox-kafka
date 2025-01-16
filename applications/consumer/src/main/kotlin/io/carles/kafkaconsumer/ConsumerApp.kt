package io.carles.kafkaconsumer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class ConsumerApp {}

fun main(args: Array<String>) {
    SpringApplication.run(ConsumerApp::class.java, *args)
}
