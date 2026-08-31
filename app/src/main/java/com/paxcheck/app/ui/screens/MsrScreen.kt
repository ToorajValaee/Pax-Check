package com.paxcheck.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.paxcheck.app.ui.components.SharedLogView
import com.paxcheck.app.ui.hardware.HardwareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MsrScreen(
    viewModel: HardwareViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val msrData by viewModel.msrData.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val logs by viewModel.logMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MSR Reader Test") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Swipe a magnetic stripe card through the MSR slot.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Button(
                onClick = { viewModel.readMsr() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isReading
            ) {
                if (isReading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Polling for Swipe...")
                } else {
                    Text("Start MSR Reader")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Captured Track Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (msrData != null) {
                        TrackDataItem("Track 1", msrData?.track1)
                        Spacer(modifier = Modifier.height(8.dp))
                        TrackDataItem("Track 2", msrData?.track2)
                        Spacer(modifier = Modifier.height(8.dp))
                        TrackDataItem("Track 3", msrData?.track3)

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val receiptText = """
                                    --- MSR TEST RECEIPT ---
                                    Track 1:
                                    ${msrData?.track1 ?: "N/A"}
                                    
                                    Track 2:
                                    ${msrData?.track2 ?: "N/A"}
                                    
                                    Track 3:
                                    ${msrData?.track3 ?: "N/A"}
                                    ------------------------
                                """.trimIndent()
                                viewModel.printTest(receiptText)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Print MSR Data")
                        }
                    } else if (isReading) {
                        Text(
                            text = "Waiting for card swipe...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Text(
                            text = "No data captured yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            SharedLogView(logs = logs)
        }
    }
}

@Composable
fun TrackDataItem(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(
            text = value.takeIf { !it.isNullOrEmpty() } ?: "N/A",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
