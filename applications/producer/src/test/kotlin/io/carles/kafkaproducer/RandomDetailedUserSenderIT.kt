package io.carles.kafkaproducer

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.ints.shouldBeGreaterThan
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@EmbeddedKafka(topics = ["test"])
@ActiveProfiles("test")
class RandomDetailedUserSenderIT{

    @Autowired
    private lateinit var randomDetailedUserSender: RandomDetailedUserSender
    @Autowired
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Test
    fun `message is properly sent`() {
        val consumer = `create test consumer with consumer group id`("test-group", "test")

        randomDetailedUserSender.send()

        KafkaTestUtils.getRecords(consumer).count() shouldBeGreaterThan 0
        KafkaTestUtils.getSingleRecord(consumer, "test").value() shouldBeIn listOf(
            DetailedUser("User_1", "Jordan", "Martinez", "12345678A"),
            DetailedUser("User_2", "Avery", "Martinez", "12345678B"),
            DetailedUser("User_3", "Riley", "Davis", "12345678C"),
            DetailedUser("User_4", "Taylor", "Rodriguez", "12345678D"),
            DetailedUser("User_5", "Jamie", "Brown", "12345678E"),
            DetailedUser("User_6", "Alex", "Garcia", "12345678F"),
            DetailedUser("User_7", "Peyton", "Martinez", "12345678G"),
            DetailedUser("User_8", "Quinn", "Rodriguez", "12345678H"),
            DetailedUser("User_9", "Morgan", "Davis", "12345678I"),
        )
    }

    private fun `create test consumer with consumer group id`(group: String, topic: String): Consumer<String, DetailedUser> {
        val consumerProps = KafkaTestUtils.consumerProps(group, "true", embeddedKafka)
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        consumerProps[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://url"
        consumerProps[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
        val defaultKafkaConsumerFactory = DefaultKafkaConsumerFactory<String, DetailedUser>(consumerProps)
        val consumer = defaultKafkaConsumerFactory.createConsumer()
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic)
        return consumer
    }

}