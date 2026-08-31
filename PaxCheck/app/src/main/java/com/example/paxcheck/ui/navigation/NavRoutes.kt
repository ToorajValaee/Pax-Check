package com.example.paxcheck.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoutes {
    @Serializable
    data object Dashboard : NavRoutes()

    @Serializable
    data object MsrTest : NavRoutes()

    @Serializable
    data object PrinterTest : NavRoutes()
}
