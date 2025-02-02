package io.carles.kafkaconsumer.configurations

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class LoggerConfiguration {
    @Bean
    open fun logger(): Logger = LoggerFactory.getLogger("mainLogger")
}