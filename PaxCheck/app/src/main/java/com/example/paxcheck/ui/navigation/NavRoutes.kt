package com.example.paxcheck.ui.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.NavKey

@Serializable
sealed class NavRoutes : NavKey<NavRoutes> {
    override val key: NavRoutes get() = this

    @Serializable
    data object Dashboard : NavRoutes()

    @Serializable
    data object MsrTest : NavRoutes()

    @Serializable
    data object PrinterTest : NavRoutes()
}
