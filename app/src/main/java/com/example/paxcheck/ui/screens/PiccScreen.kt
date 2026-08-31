package com.example.paxcheck.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.paxcheck.ui.components.SharedLogView
import com.example.paxcheck.ui.hardware.HardwareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiccScreen(
    viewModel: HardwareViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val piccData by viewModel.piccData.collectAsState()
    val isReading by viewModel.isReadingPicc.collectAsState()
    val logs by viewModel.logMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactless (NFC) Test") },
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
                text = "Tap a contactless card or NFC device on the screen / contactless area.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = { viewModel.readPicc() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isReading
            ) {
                if (isReading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Waiting for Card/NFC...")
                } else {
                    Text("Start Contactless Reader")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Contactless Card Info:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (piccData != null) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "Card Type: ${piccData?.cardType}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "UID / Serial (Hex): ${piccData?.serialNumberHex ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val receiptText = """
                            --- CONTACTLESS (NFC) RECEIPT ---
                            Card Type: ${piccData?.cardType}
                            UID / Serial:
                            ${piccData?.serialNumberHex ?: "N/A"}
                            -------------------------------
                        """.trimIndent()
                        viewModel.printTest(receiptText)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Print Contactless Data")
                }
            } else if (isReading) {
                Text(
                    text = "Waiting for contactless card tap...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = "No contactless card detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            SharedLogView(logs = logs)
        }
    }
}
