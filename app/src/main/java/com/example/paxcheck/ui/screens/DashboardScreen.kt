package com.example.paxcheck.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paxcheck.ui.components.SharedLogView
import com.example.paxcheck.ui.hardware.HardwareViewModel

@Composable
fun DashboardScreen(
    viewModel: HardwareViewModel,
    onNavigateToMsr: () -> Unit,
    onNavigateToPrinter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdkStatus by viewModel.sdkStatus.collectAsState()
    val logs by viewModel.logMessages.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pax Check Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (sdkStatus == "Connected") 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column {
                    Text(text = "SDK Status", style = MaterialTheme.typography.titleMedium)
                    Text(text = sdkStatus, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onNavigateToMsr,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.SdCard, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("MSR Test")
            }
            Button(
                onClick = onNavigateToPrinter,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Printer Test")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        SharedLogView(logs = logs)
    }
}
