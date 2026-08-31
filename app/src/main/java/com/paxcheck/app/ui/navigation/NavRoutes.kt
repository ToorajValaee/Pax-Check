package com.paxcheck.app.ui.navigation

sealed class NavRoutes {
    data object Dashboard : NavRoutes()
    data object MsrTest : NavRoutes()
    data object PrinterTest : NavRoutes()
    data object IccTest : NavRoutes()
    data object PiccTest : NavRoutes()
}
