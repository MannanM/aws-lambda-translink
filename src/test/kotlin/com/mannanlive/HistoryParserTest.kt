package com.mannanlive

import com.mannanlive.translink.parser.HistoryParser
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class HistoryParserTest {
    @Test
    fun `HistoryParser process html correctly`() {
        val response = javaClass.getResource("/html/history-response.html").readText()

        val output = HistoryParser().process(response)

        assertThat(output.count(), equalTo(15))
        assertThat(output[0].getDate(), equalTo("06/08/2019"))
        assertThat(output[0].getTime(), equalTo("08:20 am"))
        assertThat(output[1].getDate(), equalTo("06/08/2019"))
        assertThat(output[1].getTime(), equalTo("05:34 pm"))
    }
}