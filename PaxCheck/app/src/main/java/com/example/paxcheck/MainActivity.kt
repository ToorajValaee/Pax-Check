package com.example.paxcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavEntry
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
        
        // Initialize PAX SDK
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
    val backStack = remember { mutableStateListOf<NavRoutes>(NavRoutes.Dashboard) }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        entryProvider = { key: NavRoutes ->
            when (key) {
                is NavRoutes.Dashboard -> NavEntry(key) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToMsr = { backStack.add(NavRoutes.MsrTest) },
                        onNavigateToPrinter = { backStack.add(NavRoutes.PrinterTest) }
                    )
                }
                is NavRoutes.MsrTest -> NavEntry(key) {
                    MsrScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeAt(backStack.size - 1) }
                    )
                }
                is NavRoutes.PrinterTest -> NavEntry(key) {
                    PrinterScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeAt(backStack.size - 1) }
                    )
                }
            }
        }
    )
}

