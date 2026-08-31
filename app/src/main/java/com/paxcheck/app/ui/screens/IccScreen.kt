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
fun IccScreen(
    viewModel: HardwareViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iccData by viewModel.iccData.collectAsState()
    val isReading by viewModel.isReadingIcc.collectAsState()
    val logs by viewModel.logMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IC Card (Chip) Test") },
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
                text = "Insert a smart card (chip card) into the lower chip slot.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Button(
                onClick = { viewModel.readIcc() },
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
                    Text("Waiting for Chip Card...")
                } else {
                    Text("Start IC Card Reader")
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
                        text = "Card ATR (Answer to Reset)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (iccData != null) {
                        Text(text = "Slot ${iccData?.slot}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = iccData?.atrHex ?: "N/A",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val receiptText = """
                                    --- IC CARD RECEIPT ---
                                    Slot: ${iccData?.slot}
                                    ATR (Hex):
                                    ${iccData?.atrHex ?: "N/A"}
                                    -----------------------
                                """.trimIndent()
                                viewModel.printTest(receiptText)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Print IC Card Data")
                        }
                    } else if (isReading) {
                        Text(
                            text = "Waiting for chip card insertion...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Text(
                            text = "No chip card detected",
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
