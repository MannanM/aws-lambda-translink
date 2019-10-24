package com.mannanlive

import com.mannanlive.translink.parser.SessionParser
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test

import org.junit.Assert.*

class SessionParserTest {
    @Test
    fun `SessionParser can extract session token`() {
        val response = javaClass.getResource("/html/session-response.html").readText()
        val session = SessionParser().process(response)
        assertThat(session, equalTo("24d20866c10ecc42d8cb892c5b5b033b9000"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SessionParser fails if no session token found`() {
        SessionParser().process("blah")
    }
}
