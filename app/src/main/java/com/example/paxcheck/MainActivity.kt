package com.example.paxcheck

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.paxcheck.hardware.PaxHardwareService
import com.example.paxcheck.sdk.PaxSdkManager
import com.example.paxcheck.ui.hardware.HardwareViewModel
import com.example.paxcheck.ui.navigation.NavRoutes
import com.example.paxcheck.ui.screens.DashboardScreen
import com.example.paxcheck.ui.screens.MsrScreen
import com.example.paxcheck.ui.screens.PrinterScreen
import com.example.paxcheck.ui.theme.PaxCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = arrayOf(
            "com.pax.permission.ICC",
            "com.pax.permission.PICC",
            "com.pax.permission.MAG",
            "com.pax.permission.PRINTER",
            "com.pax.permission.PED"
        )
        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(permissions, 100)
        }

        val sdkManager = PaxSdkManager.getInstance(this)
        sdkManager.init()

        val hardwareService = PaxHardwareService(sdkManager)
        val hardwareViewModel = ViewModelProvider(
            this,
            HardwareViewModel.Factory(hardwareService, sdkManager)
        )[HardwareViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            PaxCheckTheme {
                MainNavigation(viewModel = hardwareViewModel)
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: HardwareViewModel) {
    var route by remember { mutableStateOf<NavRoutes>(NavRoutes.Dashboard) }

    when (route) {
        NavRoutes.Dashboard -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToMsr = { route = NavRoutes.MsrTest },
            onNavigateToPrinter = { route = NavRoutes.PrinterTest }
        )

        NavRoutes.MsrTest -> MsrScreen(
            viewModel = viewModel,
            onBack = { route = NavRoutes.Dashboard }
        )

        NavRoutes.PrinterTest -> PrinterScreen(
            viewModel = viewModel,
            onBack = { route = NavRoutes.Dashboard }
        )
    }
}
