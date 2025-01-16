package io.carles.kafkastream.pipelines

import io.carles.kafkastream.UserPageViewsProduct
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.kotest.matchers.shouldBe
import ksql.pageviews
import ksql.users
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
open class UserPageViewsIT {

    @Test
    fun `should create a whole user pageview given we matching user and pageviews are published`() {
        `send users`()
        `send pageviews`()

        `create test consumer with consumer group id`("test", "user_pageviews").use {
            val record = KafkaTestUtils.getSingleRecord(it, "user_pageviews").value()
            record shouldBe UserPageViewsProduct("User_3", mutableMapOf("284221" to "Page_15"))
        }
    }

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.0"))
            .withKraft()

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }

        @BeforeAll
        @JvmStatic
        fun setupTopics() {
            createTopic("users")
            createTopic("pageviews")
            createTopic("user_pageviews")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            kafka.close()
        }

        private fun createTopic(topic: String, partitions: Int = 1, replication: Short = 1) {
            val props = Properties()
            props[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers

            AdminClient.create(props).use { adminClient ->
                val newTopic = NewTopic(topic, partitions, replication)
                adminClient.createTopics(listOf(newTopic)).all().get()
            }
        }
    }

    private fun `send pageviews`() {
        val producerProps = KafkaTestUtils.producerProps(kafka.bootstrapServers)
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        producerProps[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://url"
        val producerFactory = DefaultKafkaProducerFactory<String, pageviews>(producerProps)
        KafkaTemplate(producerFactory).also {
            it.send("pageviews", "284221", pageviews(284221L, "User_3", "Page_15"))
        }
    }

    private fun `send users`() {
        val producerProps = KafkaTestUtils.producerProps(kafka.bootstrapServers)
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        producerProps[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://url"
        val producerFactory = DefaultKafkaProducerFactory<String, users>(producerProps)
        KafkaTemplate(producerFactory).also {
            it.send("users", "User_3", users(1507051632208, "User_3", "Region_6", "FEMALE"))
        }
    }

    private fun `create test consumer with consumer group id`(
        group: String,
        topic: String
    ): Consumer<String, UserPageViewsProduct> {
        val consumerProps = KafkaTestUtils.consumerProps(kafka.bootstrapServers, group, "true")
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        consumerProps[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://url"
        consumerProps[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
        val defaultKafkaConsumerFactory = DefaultKafkaConsumerFactory<String, UserPageViewsProduct>(consumerProps)
        return defaultKafkaConsumerFactory.createConsumer().apply {
            subscribe(listOf(topic))
        }
    }
}
