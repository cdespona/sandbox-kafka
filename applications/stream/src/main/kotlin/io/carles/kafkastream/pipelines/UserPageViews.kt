package io.carles.kafkastream.pipelines

import io.carles.kafkastream.NullableUserPageViewsProduct
import io.carles.kafkastream.UserPageViewsProduct
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import ksql.pageviews
import ksql.users
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.Stores
import org.springframework.beans.factory.annotation.Autowired

class UserPageViews(
    private val usersTopic: String,
    private val pageViewsTopic: String,
    private val outputTopicName: String,
    private val usersSerde: SpecificAvroSerde<users>,
    private val pageViewsSerde: SpecificAvroSerde<pageviews>,
    private val userPageViewsSerde: SpecificAvroSerde<UserPageViewsProduct>,
    private val nullableUserPageViewsSerde: SpecificAvroSerde<NullableUserPageViewsProduct>
) {
    @Autowired
    fun buildPipeline(streamsBuilder: StreamsBuilder) {
        val usersTable = streamsBuilder.table(usersTopic, Consumed.with(Serdes.String(), usersSerde))
        val userPageViewsTable = streamsBuilder.stream(pageViewsTopic, Consumed.with(Serdes.String(), pageViewsSerde))
            .map { _, value -> KeyValue.pair(value.userid, value) }
            .toTable(Materialized.with(Serdes.String(), pageViewsSerde))
        usersTable
            .leftJoin(
                userPageViewsTable,
                { user, pageViews ->
                    NullableUserPageViewsProduct(
                        user?.userid,
                        pageViews?.let { mutableMapOf(it.viewtime.toString() to it.pageid) }
                    )
                },
                Materialized.with(Serdes.String(), nullableUserPageViewsSerde)
            )
            .toStream()
            .filter { key, value -> key != null && value != null }
            .groupByKey()
            .reduce(
                { aggregated, latest ->
                    NullableUserPageViewsProduct(
                        aggregated.userid,
                        (aggregated.pageviews ?: mutableMapOf()) + (latest.pageviews ?: mutableMapOf())
                    )
                },
                Materialized.`as`(Stores.persistentKeyValueStore("user_pageviews_store"))
            )
            .filter { _, value -> value.userid != null}
            .mapValues { nullableValues -> UserPageViewsProduct(nullableValues.userid, nullableValues.pageviews) }
            .toStream()
            .to(outputTopicName, Produced.with(Serdes.String(), userPageViewsSerde))
    }

    companion object {
        fun initialize(
            usersTopic: String,
            pageViewsTopic: String,
            outputTopicName: String,
            schemaRegistryConfig: Map<String, String>
        ): UserPageViews {
            val usersSerde = SpecificAvroSerde<users>().apply {
                configure(schemaRegistryConfig, false)
            }
            val pageViewsSerde = SpecificAvroSerde<pageviews>().apply {
                configure(schemaRegistryConfig, false)
            }
            val userPageViewsSerde = SpecificAvroSerde<UserPageViewsProduct>().apply {
                configure(schemaRegistryConfig, false)
            }
            val nullableUserPageViewsSerde = SpecificAvroSerde<NullableUserPageViewsProduct>().apply {
                configure(schemaRegistryConfig, false)
            }
            return UserPageViews(
                usersTopic,
                pageViewsTopic,
                outputTopicName,
                usersSerde,
                pageViewsSerde,
                userPageViewsSerde,
                nullableUserPageViewsSerde,
            )
        }
    }

}