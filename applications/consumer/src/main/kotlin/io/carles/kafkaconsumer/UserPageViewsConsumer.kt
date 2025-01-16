package io.carles.kafkaconsumer


import io.carles.kafkastream.UserPageViewsProduct
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.springframework.kafka.annotation.KafkaListener

open class UserPageViewsConsumer(private val logger: Logger) {
    @KafkaListener(
        topics = ["\${application.userpageviews.topics.input.userpageviews}"],
        groupId = "#{@consumerGroupId}",
        containerFactory = "kafkaListenerContainerConsumerFactory"
    )
    fun receive(message: ConsumerRecord<String, UserPageViewsProduct?>) {
        message.value()?.let {
            logger.info("UserPageViews received for user ${it.userid}, over the pages ${it.pageviews}")
        }
    }

}
