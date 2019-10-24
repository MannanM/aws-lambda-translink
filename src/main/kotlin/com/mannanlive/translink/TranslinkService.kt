package com.mannanlive.translink

import com.amazonaws.services.lambda.runtime.LambdaRuntime
import com.mannanlive.translink.domain.Journey
import com.mannanlive.translink.domain.UsualJourney

class TranslinkService {
    private val logger = LambdaRuntime.getLogger()
    private val client = TranslinkClient()

    fun process(): List<Journey> {
        return if (client.login(System.getenv("CARD_NUM"), System.getenv("PASSWORD"))) {
            val history = client.getHistory()
            history.forEach {
                logger.log(it.toString())
            }
            history
        } else {
            logger.log("Failed to login to Translink")
            listOf()
        }
    }

    fun lodgeClaims(history: List<Journey>): List<Journey.RefundRequest> {
        val mode = System.getenv("MODE") ?: return listOf()
        val route = System.getenv("ROUTE") ?: return listOf()
        val sourceAndDestination = System.getenv("SOURCE_DESTINATION") ?: return listOf()
        val regular = System.getenv("REGULAR_JOURNEY") ?: return listOf()
        val abandoned = System.getenv("ABANDONED_JOURNEY") ?: return listOf()
        val usualJourney = UsualJourney(mode, route, sourceAndDestination, regular, abandoned)
        logger.log("Searching for any charges to lodge $usualJourney")
        return history.filter { journey -> journey is Journey.Charge }
               .filter { journey -> journey.isInLast24Hours() }
               .map { charge -> client.lodgeRequest(charge as Journey.Charge, usualJourney) }
    }
}