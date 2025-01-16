package io.carles.kafkaconsumer.configurations

import io.carles.kafkaconsumer.UserPageViewsConsumer
import io.carles.kafkastream.UserPageViewsProduct
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableKafka
open class StreamingConfiguration {
    @Value(value = "\${spring.kafka.bootstrap-servers}")
    private val bootstrapAddress: String? = null

    @Value(value = "\${spring.kafka.schema-registry.url}")
    private val schemaRegistryUrl: String? = null

    @Bean
    open fun userPageViewsConsumer(logger: Logger) = UserPageViewsConsumer(logger)

    @Bean
    open fun consumerFactory(): ConsumerFactory<String, UserPageViewsProduct> =
        DefaultKafkaConsumerFactory(defaultConsumerFactoryProperties)

    @Bean
    open fun kafkaListenerContainerConsumerFactory(
        consumerFactory: ConsumerFactory<String, UserPageViewsProduct>
    ): ConcurrentKafkaListenerContainerFactory<String, UserPageViewsProduct> =
        ConcurrentKafkaListenerContainerFactory<String, UserPageViewsProduct>().apply {
            this.consumerFactory = consumerFactory
            setCommonErrorHandler(DefaultErrorHandler(FixedBackOff(5000L, FixedBackOff.UNLIMITED_ATTEMPTS)))
        }

    @Bean
    open fun consumerGroupId(
        @Value("\${application.userpageviews.consumer-group.prefix}") prefix: String,
        @Value("\${application.userpageviews.consumer-group.iteration}") iteration: Int
    ): String {
        return "${prefix}-userpageviews-${iteration}"
    }



    private val defaultConsumerFactoryProperties get() = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
        KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
    )
}