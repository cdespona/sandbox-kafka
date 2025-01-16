package io.carles.kafkaconsumer

import com.ninjasquad.springmockk.MockkBean
import io.carles.kafkastream.UserPageViewsProduct
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.mockk.verify
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@EmbeddedKafka(topics = ["test"])
@ActiveProfiles("test")
class UserPageViewsConsumerIT {

    @Autowired
    private lateinit var consumer: UserPageViewsConsumer

    @Autowired
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @MockkBean(relaxed = true)
    private lateinit var logger: Logger

    @Test
    fun `message is correctly logged when received`() {
        `send message to`("test")

        verify(timeout = 5000) { logger.info("UserPageViews received for user User_3, over the pages ${mapOf("2201" to "Page_47")}") }
    }

    private fun `send message to`(topic: String) {
        val producerProps = KafkaTestUtils.producerProps(embeddedKafka)
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        producerProps[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://url"
        val producerFactory = DefaultKafkaProducerFactory<String, UserPageViewsProduct>(producerProps)
        val kafkaTemplate = KafkaTemplate(producerFactory)
        kafkaTemplate.send(topic, "User_3", UserPageViewsProduct("User_3", mapOf("2201" to "Page_47")))
    }
}