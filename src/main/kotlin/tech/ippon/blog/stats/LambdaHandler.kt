package tech.ippon.blog.stats

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.logging.log4j.LogManager

// Entry point to run as an AWS Lambda
class LambdaHandler : RequestHandler<Map<String, Any>, String> {

    private val logger = LogManager.getLogger(javaClass)

    override fun handleRequest(input: Map<String, Any>, context: Context): String {
        logger.info("Handler called")
        updateSheet()
        sendNotification()
        logger.info("Done")
        return "SUCCESS"
    }
}