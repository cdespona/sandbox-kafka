package io.carles.kafkastream.configurations

import io.carles.kafkastream.pipelines.UserPageViews
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG
import org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import java.io.Serializable

@Configuration
open class PipelinesConfiguration {

    @Value(value = "\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapAddress: String

    @Bean
    open fun userPageViews(
        @Value(value = "\${application.userpageviews.topics.input.users}") usersTopic: String,
        @Value(value = "\${application.userpageviews.topics.input.pageviews}") pageViewsTopic: String,
        @Value(value = "\${application.userpageviews.topics.output}") outputTopic: String,
        @Value(value = "\${spring.schema-registry.url}") schemaRegistryUrl: String,
    ) = UserPageViews.initialize(usersTopic, pageViewsTopic, outputTopic, schemaRegistryConfig(schemaRegistryUrl))

    @Bean
    open fun userPageViewsStreamBuilder(
        @Value(value = "\${application.userpageviews.consumer-group.prefix}") consumerGroupPrefix: String,
        @Value(value = "\${application.userpageviews.consumer-group.iteration}") consumerGroupIteration: String,
    ): StreamsBuilderFactoryBean {
        val userPageViewsConfig = defaultStreamsConfig() +
                mapOf(StreamsConfig.APPLICATION_ID_CONFIG to "$consumerGroupPrefix-userpageviews-$consumerGroupIteration")
        return StreamsBuilderFactoryBean(KafkaStreamsConfiguration(userPageViewsConfig))
    }

    private fun defaultStreamsConfig(): Map<String, Serializable> = Serdes.String().use { stringSerde ->
        return mapOf(
            BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
            DEFAULT_KEY_SERDE_CLASS_CONFIG to stringSerde.javaClass.name,
            DEFAULT_VALUE_SERDE_CLASS_CONFIG to GenericAvroSerde::class.java,
        )
    }

    private fun schemaRegistryConfig(url: String): Map<String, String> {
        return mapOf(
            SCHEMA_REGISTRY_URL_CONFIG to url,
        )
    }
}