package com.mannanlive.email

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*

class EmailClient {
    private val client = AmazonSimpleEmailServiceClientBuilder
            .standard()
            .withRegion(Regions.US_WEST_2)
            .build()

    fun send(source: String, destination: String, subject: String, body: String) =
            client.sendEmail(
                    SendEmailRequest()
                            .withSource(source)
                            .withDestination(Destination()
                                    .withToAddresses(destination))
                            .withMessage(Message()
                                    .withSubject(Content(subject))
                                    .withBody(Body()
                                            .withHtml(Content(body))))
            ).messageId!!
}
