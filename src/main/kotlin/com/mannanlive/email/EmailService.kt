package com.mannanlive.email

import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.LambdaRuntime
import com.mannanlive.translink.domain.Journey

class EmailService(private val client: EmailClient = EmailClient(),
                   private val logger: LambdaLogger = LambdaRuntime.getLogger(),
                   private val email: String? = System.getenv("EMAIL")) {

    fun createEmail(journeys: List<Journey>) {
        val email = email ?: return
        val groupedTrips = journeys.groupBy { it.isInLast24Hours() }
        if (groupedTrips[true]?.isEmpty() == false) {
            logger.log("Emailing activity report to $email")
            val messageId = sendMessage(email, groupedTrips)
            logger.log("Sent email with id '$messageId'")
        } else {
            logger.log("No trips in the last 24 hours")
        }
    }

    private fun sendMessage(email: String, groupedTrips: Map<Boolean, List<Journey>>): String =
            client.send(email, email, "Translink Activity Report",
                    """
                    <h1>Translink Activity Report</h1>
                    <h2>Journeys in the last 24 hours</h2>
                    ${journeysToString(groupedTrips[true])}
                    <h2>Other trips</h2>
                    ${journeysToString(groupedTrips[false])}
                    """.trimIndent()
            )

    private fun journeysToString(list: List<Journey>?): String {
        return list?.sortedByDescending { it.startDateTime }
                ?.joinToString(prefix = "<ul>", postfix = "</ul>", separator = "") {
                    "<li>${it.humanDate}: ${journeyToString(it)}</li>"
                } ?: "None"
    }

    private fun journeyToString(journey: Journey) =
            when (journey) {
                is Journey.TopUp -> "Top-Up of $${journey.cost} now with balance of $${journey.balance}"
                is Journey.Transfer -> "Transferred at ${journey.from}"
                is Journey.Charge -> "Charge at ${journey.from} of $${journey.cost} now with balance of $${journey.balance}"
                is Journey.Trip -> "Trip from ${journey.from} to ${journey.to} taking ${journey.duration} minutes costing $${journey.cost}"
                is Journey.RefundRequest -> "Refund request raised for $${journey.cost} at ${journey.from} with tracking ID: ${journey.trackingId}"
            }
}