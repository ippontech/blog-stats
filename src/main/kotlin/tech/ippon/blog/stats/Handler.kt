package tech.ippon.blog.stats

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import org.apache.log4j.Logger

class Handler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val logger = Logger.getLogger(javaClass)

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        logger.info("Handler called with input: $input")

        try {
            updateSheet()
            sendNotification()
        } catch (e: Exception) {
            logger.error("Processing failed", e)
            val responseBody = Response("FAILED")
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(responseBody)
                    .build()
        }

        logger.info("Done")

        val responseBody = Response("SUCCESS")
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(responseBody)
                .build()
    }
}
