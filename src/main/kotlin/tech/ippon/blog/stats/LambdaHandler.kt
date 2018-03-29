package tech.ippon.blog.stats

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.logging.log4j.LogManager

class LambdaHandler : RequestHandler<Map<String, Object>, String> {

    private val logger = LogManager.getLogger(javaClass)

    override fun handleRequest(input: Map<String, Object>, context: Context): String {
        logger.info("Handler called")
        updateSheet()
        logger.info("Done")
        return "{\"success\": \"true\"}"
    }
}