package tech.ippon.blog.stats.service

import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.PublishRequest
import org.apache.log4j.Logger

class NotificationService {

    private val logger = Logger.getLogger(javaClass)

    private val topicArn: String

    init {
        topicArn = System.getenv("TOPIC_ARN")
    }

    fun sendNotification(message: String) {
        logger.info("Sending notification")

        val snsClient = AmazonSNSClientBuilder.defaultClient()
        val publishRequest = PublishRequest(topicArn, message, message)
        val publishResult = snsClient.publish(publishRequest)

        logger.info("Sent notification: messageId=${publishResult.messageId}")
    }
}