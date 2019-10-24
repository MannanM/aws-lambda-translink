package com.mannanlive.translink.parser

import com.mannanlive.translink.domain.ChargeType
import com.mannanlive.translink.domain.Journey
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.lang.IllegalStateException

class HistoryParser {
    private val formatter = DateTimeFormat.forPattern("dd MMM yyyy hh:mm a")

    fun process(input: String): List<Journey> {
        var lastDate = ""
        val results = mutableListOf<Journey>()
        Jsoup.parse(input).select("#travel-history tbody tr").forEach { row ->
            if (row.hasClass("sub-heading")) {
                lastDate = row.text()
            } else {
                results.add(processRow(row, lastDate))
            }
        }
        return results.toList()
    }

    private fun processRow(row: Element, lastDate: String): Journey {
        val cells = row.getElementsByTag("td")
        val startDateTime = toDate(lastDate, cells[0])
        return when {
            cells.count() == 6 -> Journey.TopUp(startDateTime, cells[1].text(), toFloat(cells[3]), toFloat(cells[4]))
            cells.count() == 7 -> Journey.Transfer(startDateTime, cells[1].text(), 0f)
            cells.count() == 8 && cells[2].text() == "Unknown" -> Journey.Charge(startDateTime, cells[1].text(),
                    toFloat(cells[4]), toFloat(cells[6]), toLink(cells[7]), ChargeType.TapOff)
            cells.count() == 8 -> createTrip(startDateTime, cells, lastDate)
            else -> throw IllegalStateException("Unknown Journey '${row.text()}', cells: ${cells.count()}")
        }
    }

    private fun createTrip(startDateTime: DateTime, cells: Elements, lastDate: String): Journey {
        val trip = Journey.Trip(startDateTime, cells[1].text(), toFloat(cells[4]), toFloat(cells[6]),
                toDate(lastDate, cells[2]), cells[3].text(), toLink(cells[7]))
        return if (trip.from == trip.to && trip.cost == 10f) {
            Journey.Charge(trip.startDateTime, trip.from, trip.cost, trip.balance, trip.reportUri, ChargeType.SameStation)
        } else {
            trip
        }
    }

    private fun toLink(element: Element) =
            element.getElementsByTag("a")[0].attr("href")

    private fun toDate(lastDate: String, element: Element): DateTime =
            formatter.parseDateTime("$lastDate ${element.text()}")

    private fun toFloat(element: Element) =
            element.text().replace("$ ", "").toFloat()
}
