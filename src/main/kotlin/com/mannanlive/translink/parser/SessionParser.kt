package com.mannanlive.translink.parser

import org.jsoup.Jsoup

class SessionParser {
    fun process(input: String): String {
        Jsoup.parse(input).select("input[name=sessionToken]").forEach { row ->
            return row.attr("value")
        }
        throw IllegalArgumentException("Can not find a session id")
    }

}