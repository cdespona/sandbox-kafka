package io.carles.kafkaproducer

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate

class RandomDetailedUserSenderShould{
    @Test
    fun `detailed user is sent`() {
        val kafkaTemplate = mockk<KafkaTemplate<String, DetailedUser>>(relaxed = true)
        val sender = RandomDetailedUserSender("topicName", kafkaTemplate)

        sender.send()

        verify { kafkaTemplate.send("topicName", any(String::class), any(DetailedUser::class)) }
    }
}