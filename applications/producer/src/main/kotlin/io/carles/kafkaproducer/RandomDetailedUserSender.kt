package io.carles.kafkaproducer

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import kotlin.random.Random

class RandomDetailedUserSender(
    private val topicName: String,
    private val kafkaTemplate: KafkaTemplate<String, DetailedUser>
) {
    @Scheduled(initialDelay = 30000, fixedRate = 5000)
    fun send() {
        val detailedUser = USER_DETAILS["User_${Random.nextInt(1, 10)}"]
        kafkaTemplate.send(topicName, detailedUser!!.userid, detailedUser)
    }

    companion object{
        private val USER_DETAILS = mapOf(
            "User_1" to DetailedUser("User_1", "Jordan", "Martinez", "12345678A"),
            "User_2" to DetailedUser("User_2", "Avery", "Martinez", "12345678B"),
            "User_3" to DetailedUser("User_3", "Riley", "Davis", "12345678C"),
            "User_4" to DetailedUser("User_4", "Taylor", "Rodriguez", "12345678D"),
            "User_5" to DetailedUser("User_5", "Jamie", "Brown", "12345678E"),
            "User_6" to DetailedUser("User_6", "Alex", "Garcia", "12345678F"),
            "User_7" to DetailedUser("User_7", "Peyton", "Martinez", "12345678G"),
            "User_8" to DetailedUser("User_8", "Quinn", "Rodriguez", "12345678H"),
            "User_9" to DetailedUser("User_9", "Morgan", "Davis", "12345678I"),
        )
    }
}