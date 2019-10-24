package com.mannanlive.translink.domain

data class UsualJourney(val mode: String,
                        val route: String,
                        val sourceDestination: String,
                        val regular: String,
                        val abandoned: String) {

    fun getDestination(source: String): String {
        val sourceAndDestination = sourceDestination.split(":")
        return if (sourceAndDestination[0].equals(source, true)) {
            sourceAndDestination[1]
        } else {
            sourceAndDestination[0]
        }
    }

    fun getDescription(type: ChargeType): String {
        return when (type) {
            ChargeType.TapOff -> regular
            ChargeType.SameStation -> abandoned
        }
    }
}
