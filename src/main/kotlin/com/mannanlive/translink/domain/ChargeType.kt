package com.mannanlive.translink.domain

sealed class ChargeType {
    object TapOff : ChargeType()
    object SameStation : ChargeType()
}
