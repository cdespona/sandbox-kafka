package io.carles.kafkaproducer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class ProducerApp {}

fun main(args: Array<String>) {
    SpringApplication.run(ProducerApp::class.java, *args)
}
