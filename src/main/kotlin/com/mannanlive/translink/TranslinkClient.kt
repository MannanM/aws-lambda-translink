package com.mannanlive.translink

import com.amazonaws.util.IOUtils
import com.mannanlive.translink.parser.HistoryParser
import com.mannanlive.translink.parser.SessionParser
import com.mannanlive.translink.domain.Journey
import com.mannanlive.translink.domain.UsualJourney
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.StatusLine
import org.apache.http.client.CookieStore
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.util.EntityUtils
import org.joda.time.DateTime

class TranslinkClient {
    private val BASE_URL = "https://gocard.translink.com.au"
    private val ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
    private val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36"

    private val httpCookieStore: CookieStore = BasicCookieStore()
    private val client = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).build()

    fun login(cardNum: String, password: String): Boolean {
        val response = extractResponse(HttpPost("$BASE_URL/webtix/welcome/welcome.do").apply {
            entity = UrlEncodedFormEntity(
                    listOf(BasicNameValuePair("cardOps", "Display"),
                            BasicNameValuePair("cardNum", cardNum),
                            BasicNameValuePair("pass", password)))
            setHeader(HttpHeaders.ACCEPT, ACCEPT)
            setHeader(HttpHeaders.USER_AGENT, USER_AGENT)
        })
        return response.first.statusCode == 302
    }

    fun getHistory(): List<Journey> {
        val response = extractResponse(HttpGet("$BASE_URL/webtix/tickets-and-fares/go-card/online/history").apply {
            setHeader(HttpHeaders.ACCEPT, ACCEPT)
            setHeader(HttpHeaders.USER_AGENT, USER_AGENT)
        })
        return if (response.first.statusCode == 200) {
            HistoryParser().process(response.second)
        } else {
            listOf()
        }
    }

    fun lodgeRequest(charge: Journey.Charge, usual: UsualJourney): Journey.RefundRequest {
        val response = extractResponse(HttpPost("$BASE_URL/webtix/tickets-and-fares/go-card/online/enquiries").apply {
            setHeader(HttpHeaders.ACCEPT, ACCEPT)
            setHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            setHeader(HttpHeaders.REFERER, "$BASE_URL${charge.reportUri}")
            entity = UrlEncodedFormEntity(
                    listOf(BasicNameValuePair("sessionToken", getSessionId(charge)),
                            BasicNameValuePair("incidentDate", charge.getDate()),
                            BasicNameValuePair("incidentTimeHour", charge.getTimeHour()),
                            BasicNameValuePair("incidentTimeMinute", charge.getTimeMinute()),
                            BasicNameValuePair("incidentTimeAmPm", "time${charge.getTimeAmPm()}"),
                            BasicNameValuePair("incidentTime", charge.getTime()),
                            BasicNameValuePair("tripRouteLine", ""),
                            BasicNameValuePair("tripFrom", ""),
                            BasicNameValuePair("tripTo", ""),
                            BasicNameValuePair("ticketSerialNumber", ""),
                            BasicNameValuePair("tripCounter", ""),
                            BasicNameValuePair("travelMode", "mode${usual.mode}"),
                            BasicNameValuePair("travelRoute", usual.route),
                            BasicNameValuePair("travelSource", charge.from),
                            BasicNameValuePair("travelDestination", usual.getDestination(charge.from)),
                            BasicNameValuePair("incidentDescription", usual.getDescription(charge.type)),
                            BasicNameValuePair("submit3001", "Submit fare enquiry")))
        })
        if (response.first.statusCode == 200) {
            val trackingId = SessionParser().process(response.second)
            return Journey.RefundRequest(DateTime.now(), charge.from, charge.cost, trackingId)
        } else {
            throw IllegalArgumentException("Can not get lodge request for '$charge'")
        }
    }

    private fun getSessionId(charge: Journey.Charge): String {
        val response = extractResponse(HttpGet("$BASE_URL${charge.reportUri.replace(" ", "%20")}")
                .apply {
                    setHeader(HttpHeaders.ACCEPT, ACCEPT)
                    setHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                })
        if (response.first.statusCode == 200) {
            return SessionParser().process(response.second)
        } else {
            throw IllegalArgumentException("Can not get a session id for '$charge'")
        }
    }

    private fun extractResponse(request: HttpUriRequest): Pair<StatusLine, String> {
        val response = client.execute(request)
        val statusLine = response.statusLine
        val result = IOUtils.toString(response.entity.content)
        EntityUtils.consume(response.entity)
        return statusLine to result
    }
}