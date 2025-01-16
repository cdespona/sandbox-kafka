package io.carles.kafkaconsumer

import io.carles.kafkastream.UserPageViewsProduct
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.slf4j.Logger

class UserPageViewsConsumerShould {

    @Test
    fun `log whenever a message received`() {
        val logger = mockk<Logger>(relaxed = true)
        val consumer = UserPageViewsConsumer(logger)

        consumer.receive(
            `user page views record`()
        )

        verify { logger.info("UserPageViews received for user User_3, over the pages ${mapOf("2201" to "Page_47")}") }
    }

    @Test
    fun `do nothing when tombstone received`() {
        val logger = mockk<Logger>(relaxed = true)
        val consumer = UserPageViewsConsumer(logger)

        consumer.receive(
            `user page views record`(null)
        )

        verify { logger wasNot Called }
    }

    private fun `user page views record`(
        value: UserPageViewsProduct? = UserPageViewsProduct(
            "User_3",
            mapOf("2201" to "Page_47")
        )
    ) = ConsumerRecord(
        "topic",
        0,
        0,
        "User_3",
        value
    )
}