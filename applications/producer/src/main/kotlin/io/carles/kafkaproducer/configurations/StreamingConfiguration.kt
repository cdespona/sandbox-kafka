package io.carles.kafkaproducer.configurations

import io.carles.kafkaproducer.DetailedUser
import io.carles.kafkaproducer.RandomDetailedUserSender
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
open class StreamingConfiguration {

    @Bean
    open fun randomDetailedUserSender(
        @Value(value = "\${spring.kafka.topic}") topicName: String,
        kafkaTemplate: KafkaTemplate<String, DetailedUser>,
    ) = RandomDetailedUserSender(topicName, kafkaTemplate)

    @Bean
    open fun producerFactory(
        @Value(value = "\${spring.kafka.bootstrap-servers}") bootstrapServerUrl: String,
        @Value(value = "\${spring.schema-registry.url}") schemaRegistryUrl: String,
    ): ProducerFactory<String, DetailedUser> = DefaultKafkaProducerFactory(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServerUrl,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl
        )
    )

    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, DetailedUser>) = KafkaTemplate(producerFactory)
}