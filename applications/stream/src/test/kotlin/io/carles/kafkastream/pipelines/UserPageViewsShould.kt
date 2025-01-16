package io.carles.kafkastream.pipelines

import io.carles.kafkastream.NullableUserPageViewsProduct
import io.carles.kafkastream.UserPageViewsProduct
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import ksql.pageviews
import ksql.users
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.*
import org.apache.kafka.streams.KeyValue.pair
import org.apache.kafka.streams.state.KeyValueStore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserPageViewsShould {

    private lateinit var topologyTestDriver: TopologyTestDriver
    private lateinit var inputUsersTopic: TestInputTopic<String, users>
    private lateinit var inputPageViewsTopic: TestInputTopic<String, pageviews>
    private lateinit var outputTopic: TestOutputTopic<String, UserPageViewsProduct>
    private lateinit var keyValueStore: KeyValueStore<String, Map<String, String>>

    @Test
    fun `do nothing when no data is available`() {
        `no message processed`()
    }

    @Test
    fun `do nothing when no user data available`() {
        inputPageViewsTopic.pipeInput("284221", pageviews(284221L, "User_3", "Page_15"))

        `no message processed`()
    }

    @Test
    fun `publish user without views when no views data is available`() {
        inputUsersTopic.pipeInput("User_3", users(1507051632208L, "User_3", "Region_6", "FEMALE"))

        outputTopic.readKeyValue() shouldBe pair("User_3", UserPageViewsProduct("User_3", null))
    }

    @Test
    fun `publish user without views when no views data is matching`() {
        inputUsersTopic.pipeInput("User_3", users(1507051632208, "User_3", "Region_6", "FEMALE"))
        inputPageViewsTopic.pipeInput("284221", pageviews(284221, "User_4", "Page_15"))

        outputTopic.readKeyValue() shouldBe pair("User_3", UserPageViewsProduct("User_3", null))
    }

    @Test
    fun `publish user with views when views data is available`() {
        inputUsersTopic.pipeInput("User_3", users(1507051632208, "User_3", "Region_6", "FEMALE"))
        inputPageViewsTopic.pipeInput("284221", pageviews(284221, "User_3", "Page_15"))

        outputTopic.readKeyValuesToList()
            .shouldContainAll(
                pair("User_3", UserPageViewsProduct("User_3", null)),
                pair("User_3", UserPageViewsProduct("User_3", mutableMapOf("284221" to "Page_15"))),
            )
        keyValueStore["User_3"] shouldBe NullableUserPageViewsProduct("User_3", mutableMapOf("284221" to "Page_15"))
    }

    @Test
    fun `publish user with aggregated views when multiple views data is available`() {
        inputUsersTopic.pipeInput("User_3", users(1507051632208, "User_3", "Region_6", "FEMALE"))
        inputPageViewsTopic.pipeInput("284221", pageviews(284221, "User_3", "Page_15"))
        inputPageViewsTopic.pipeInput("284222", pageviews(284222, "User_3", "Page_17"))

        outputTopic.readKeyValuesToList()
            .shouldContainAll(
                pair("User_3", UserPageViewsProduct("User_3", mutableMapOf("284221" to "Page_15"))),
                pair("User_3", UserPageViewsProduct("User_3", mutableMapOf("284221" to "Page_15", "284222" to "Page_17"))),
        )
        keyValueStore["User_3"] shouldBe NullableUserPageViewsProduct("User_3", mutableMapOf("284221" to "Page_15", "284222" to "Page_17"))
    }

    @BeforeEach
    fun setUp() {
        val streamsBuilder = StreamsBuilder()
        val userPageViews: UserPageViews = UserPageViews.initialize(
            USERS_TOPIC,
            PAGE_VIEWS_TOPIC,
            OUTPUT_TOPIC_NAME,
            SCHEMA_REGISTRY_CONFIG,
        )
        userPageViews.buildPipeline(streamsBuilder)
        val topology: Topology = streamsBuilder.build()
        topologyTestDriver = TopologyTestDriver(topology)
        SpecificAvroSerde<pageviews>().use { serde ->
            serde.configure(SCHEMA_REGISTRY_CONFIG, false)
            inputPageViewsTopic = topologyTestDriver.createInputTopic(
                PAGE_VIEWS_TOPIC,
                StringSerializer(),
                serde.serializer(),
            )
        }
        SpecificAvroSerde<users>().use { serde ->
            serde.configure(SCHEMA_REGISTRY_CONFIG, false)
            inputUsersTopic = topologyTestDriver.createInputTopic(
                USERS_TOPIC,
                StringSerializer(),
                serde.serializer(),
            )
        }
        SpecificAvroSerde<UserPageViewsProduct>().use { valueSerde ->
            valueSerde.configure(SCHEMA_REGISTRY_CONFIG, false)
            outputTopic = topologyTestDriver.createOutputTopic(
                OUTPUT_TOPIC_NAME,
                StringDeserializer(),
                valueSerde.deserializer(),
            )
        }

        keyValueStore = topologyTestDriver.getKeyValueStore("user_pageviews_store")
    }

    @AfterEach
    fun tearDown() {
        topologyTestDriver.close()
    }

    private fun `no message processed`() {
        outputTopic.isEmpty shouldBe true
    }

    companion object {
        private val SCHEMA_REGISTRY_CONFIG: Map<String, String> = mapOf(
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to "mock://url",
        )
        private const val USERS_TOPIC = "users"
        private const val PAGE_VIEWS_TOPIC = "pageviews"
        private const val OUTPUT_TOPIC_NAME = "user_pageviews"
    }
}