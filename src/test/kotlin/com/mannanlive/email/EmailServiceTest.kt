package com.mannanlive.email

import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.mannanlive.translink.domain.Journey
import io.mockk.every
import io.mockk.mockk
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Before
import org.junit.Test

class EmailServiceTest {
    private val email: String = "test@test.com"
    private val date: DateTime = DateTime(2017, 10, 29, 13,58)
    private val client: EmailClient = mockk()
    private val logger: LambdaLogger = mockk()
    private lateinit var objectUnderTest: EmailService

    @Before
    fun setUp() {
        DateTimeUtils.setCurrentMillisFixed(date.millis)
        objectUnderTest = EmailService(client, logger, email)
    }

    @Test
    fun `empty list does nothing`() {
        every { logger.log("No trips in the last 24 hours") } answers { }

        objectUnderTest.createEmail(listOf())
    }

    @Test
    fun `old entry does nothing`() {
        every { logger.log("No trips in the last 24 hours") } answers { }

        objectUnderTest.createEmail(listOf(
                Journey.Transfer(DateTime.now().minusDays(2), "Somewhere")
        ))
    }

    @Test
    fun `new entry does something`() {
        every { logger.log("Emailing activity report to $email") } answers { }
        every {
            client.send(email, email, "Translink Activity Report",
                    """
                    <h1>Translink Activity Report</h1>
                    <h2>Journeys in the last 24 hours</h2>
                    <ul><li>29/10/17 01:38PM: Trip from Somewhere to Oz taking 20 minutes costing ${'$'}3.5</li></ul>
                    <h2>Other trips</h2>
                    <ul><li>27/10/17 01:58PM: Transferred at Somewhere</li></ul>
                    """.trimIndent())
        } answers { "Message ID" }
        every { logger.log("Sent email with id 'Message ID'") } answers { }

        objectUnderTest.createEmail(listOf(
                Journey.Trip(date.minusMinutes(20), "Somewhere", 3.5F, 29.25F, date, "Oz", "/some-url"),
                Journey.Transfer(date.minusDays(2), "Somewhere")
        ))
    }
}