package com.mannanlive

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.mannanlive.email.EmailService
import com.mannanlive.translink.TranslinkService
import com.mannanlive.translink.domain.Journey

class TranslinkHandler  : RequestHandler<Void, List<Journey>> {
    private val translinkService = TranslinkService()

    override fun handleRequest(input: Void, context: Context): List<Journey> {
        val journeys = translinkService.process().also {
            val claims = translinkService.lodgeClaims(it)
            EmailService().createEmail(it + claims)
        }
        return journeys
    }
}
