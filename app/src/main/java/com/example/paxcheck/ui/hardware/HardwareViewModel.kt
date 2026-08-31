package com.example.paxcheck.ui.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.paxcheck.hardware.CardData
import com.example.paxcheck.hardware.HardwareResult
import com.example.paxcheck.hardware.HardwareService
import com.example.paxcheck.hardware.IccData
import com.example.paxcheck.sdk.PaxSdkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for hardware interactions.
 * Connects the UI to the [HardwareService].
 */
class HardwareViewModel(
    private val hardwareService: HardwareService,
    private val sdkManager: PaxSdkManager
) : ViewModel() {

    private val _msrData = MutableStateFlow<CardData?>(null)
    val msrData: StateFlow<CardData?> = _msrData.asStateFlow()

    private val _isReading = MutableStateFlow(false)
    val isReading: StateFlow<Boolean> = _isReading.asStateFlow()

    private val _iccData = MutableStateFlow<IccData?>(null)
    val iccData: StateFlow<IccData?> = _iccData.asStateFlow()

    private val _isReadingIcc = MutableStateFlow(false)
    val isReadingIcc: StateFlow<Boolean> = _isReadingIcc.asStateFlow()

    private val _printStatus = MutableStateFlow<String?>(null)
    val printStatus: StateFlow<String?> = _printStatus.asStateFlow()

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    private val _sdkStatus = MutableStateFlow("Unknown")
    val sdkStatus: StateFlow<String> = _sdkStatus.asStateFlow()

    init {
        viewModelScope.launch {
            sdkManager.status.collectLatest { status ->
                _sdkStatus.value = status
            }
        }
    }

    private fun addLog(message: String) {
        val currentLogs = _logMessages.value.toMutableList()
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        currentLogs.add(0, "[$timestamp] $message")
        _logMessages.value = currentLogs.take(50) // Keep last 50 logs
    }

    /**
     * Triggers MSR reading.
     */
    fun readMsr() {
        viewModelScope.launch {
            _isReading.value = true
            _msrData.value = null
            addLog("MSR Read started (30s polling)")
            when (val result = hardwareService.readMsr()) {
                is HardwareResult.Success -> {
                    _msrData.value = result.data
                    addLog("MSR Read successful: Track data captured")
                }
                is HardwareResult.Error -> {
                    addLog("MSR Read failed: ${result.message}")
                }
            }
            _isReading.value = false
        }
    }

    /**
     * Triggers IC card (chip card) reading.
     */
    fun readIcc() {
        viewModelScope.launch {
            _isReadingIcc.value = true
            _iccData.value = null
            addLog("IC Card Read started (30s polling)")
            when (val result = hardwareService.readIcc()) {
                is HardwareResult.Success -> {
                    _iccData.value = result.data
                    addLog("IC Card Read successful: ATR [${result.data.atrHex}]")
                }
                is HardwareResult.Error -> {
                    addLog("IC Card Read failed: ${result.message}")
                }
            }
            _isReadingIcc.value = false
        }
    }

    /**
     * Triggers text printing.
     */
    fun printTest(text: String) {
        viewModelScope.launch {
            _printStatus.value = "Printing..."
            addLog("Print started: $text")
            when (val result = hardwareService.printText(text)) {
                is HardwareResult.Success -> {
                    _printStatus.value = "Print Success"
                    addLog("Print status: Success")
                }
                is HardwareResult.Error -> {
                    _printStatus.value = "Print Failed"
                    addLog("Printer Error: ${result.message}")
                }
            }
        }
    }

    /**
     * Factory for creating [HardwareViewModel] with [HardwareService].
     */
    class Factory(
        private val hardwareService: HardwareService,
        private val sdkManager: PaxSdkManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HardwareViewModel(hardwareService, sdkManager) as T
        }
    }
}
