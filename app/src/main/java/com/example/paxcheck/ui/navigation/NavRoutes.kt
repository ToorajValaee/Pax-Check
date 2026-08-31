package com.example.paxcheck.ui.navigation

sealed class NavRoutes {
    data object Dashboard : NavRoutes()
    data object MsrTest : NavRoutes()
    data object PrinterTest : NavRoutes()
}
