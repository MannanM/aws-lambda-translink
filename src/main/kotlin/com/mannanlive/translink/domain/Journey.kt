package com.mannanlive.translink.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.joda.time.DateTime
import org.joda.time.Duration

sealed class Journey {

    abstract val startDateTime: DateTime
    abstract val from: String
    abstract val cost: Float

    data class Transfer(override val startDateTime: DateTime, override val from: String,
                        override val cost: Float = 0f) : Journey() {

        override fun toString(): String {
            return "$startDateTime: Transferred at $from"
        }
    }
    class TopUp(override val startDateTime: DateTime, override val from: String,
                override val cost: Float, val balance: Float) : Journey() {

        override fun toString(): String {
            return "$startDateTime: Top-Up of $$cost now with balance of $$balance"
        }
    }

    class Trip(override val startDateTime: DateTime, override val from: String,
               override val cost: Float, val balance: Float, val endDateTime: DateTime,
               val to: String, val reportUri: String) : Journey() {

        val duration: Long
            get() = Duration(startDateTime, endDateTime).standardMinutes

        override fun toString(): String {
            return "$startDateTime: Trip from $from to $to taking ${duration} minutes costing $$cost"
        }
    }

    class Charge(override val startDateTime: DateTime, override val from: String,
                 override val cost: Float, val balance: Float, val reportUri: String, val type: ChargeType) : Journey() {

        override fun toString(): String {
            return "$startDateTime: Charge at $from of $$cost now with balance of $$balance"
        }
    }

    class RefundRequest(override val startDateTime: DateTime, override val from: String,
                 override val cost: Float, val trackingId: String) : Journey() {

        override fun toString(): String {
            return "$startDateTime: Lodged a dispute of $$cost charge with $trackingId"
        }
    }

    val humanDate: String
        get() = startDateTime.toString("d/M/yy hh:mma")


    @JsonIgnore fun getDate(): String = startDateTime.toString("dd/MM/yyyy")
    @JsonIgnore fun getTime(): String = startDateTime.toString("hh:mm a").toLowerCase()
    @JsonIgnore fun getTimeHour(): String = startDateTime.toString("HH")
    @JsonIgnore fun getTimeMinute(): String = roundDownToNearestFiveMinutes(startDateTime.toString("mm"))
    @JsonIgnore fun getTimeAmPm(): String = startDateTime.toString("a")
    @JsonIgnore fun isInLast24Hours(): Boolean = startDateTime.isAfter(DateTime.now().minusHours(24 - 10))

    @JsonIgnore fun roundDownToNearestFiveMinutes(input: String) = String.format("%02.0f", Math.floor(input.toDouble() / 5) * 5)
}
