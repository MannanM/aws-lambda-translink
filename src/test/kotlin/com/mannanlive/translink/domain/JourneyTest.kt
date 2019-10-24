package com.mannanlive.translink.domain

import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.junit.Assert.*
import org.junit.Test

class JourneyTest {
    private val topUp = Journey.TopUp(DateTime.parse("2010-06-03T01:02Z"),
            "Central", 3.50F, 45.0F)

    @Test
    fun `will give required time information`() {
        assertThat(topUp.getDate(), equalTo("03/06/2010"));
        assertThat(topUp.getTime(), equalTo("01:02 am"));
        assertThat(topUp.getTimeHour(), equalTo("01"));
        assertThat(topUp.getTimeMinute(), equalTo("00"));
        assertThat(topUp.getTimeAmPm(), equalTo("AM"));
    }

    @Test
    fun `round down works for minutes on the increment`() {
        for (minutes in 0 until 60 step 5) {
            val minutesPaddedLeft = minutes.toString().padStart(2, '0')
            assertThat(topUp.roundDownToNearestFiveMinutes(minutesPaddedLeft), equalTo(minutesPaddedLeft))
        }
    }

    @Test
    fun `round down works for minutes after the increment`() {
        val expectedOutput = listOf("00", "05", "50", "55")
        listOf("03", "08", "53", "58").forEachIndexed { i, minute ->
            assertThat(topUp.roundDownToNearestFiveMinutes(minute), equalTo(expectedOutput[i]))
        }
    }
}