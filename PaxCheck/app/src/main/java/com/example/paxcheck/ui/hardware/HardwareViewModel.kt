package com.example.paxcheck.ui.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.paxcheck.hardware.HardwareService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for hardware interactions.
 * Connects the UI to the [HardwareService].
 */
class HardwareViewModel(private val hardwareService: HardwareService) : ViewModel() {

    private val _msrData = MutableStateFlow<String?>(null)
    val msrData: StateFlow<String?> = _msrData.asStateFlow()

    private val _printStatus = MutableStateFlow<String?>(null)
    val printStatus: StateFlow<String?> = _printStatus.asStateFlow()

    /**
     * Triggers MSR reading.
     */
    fun readMsr() {
        viewModelScope.launch {
            _msrData.value = "Reading..."
            val result = hardwareService.readMsr()
            _msrData.value = result ?: "Read Failed"
        }
    }

    /**
     * Triggers text printing.
     */
    fun printTest(text: String) {
        viewModelScope.launch {
            _printStatus.value = "Printing..."
            val success = hardwareService.printText(text)
            _printStatus.value = if (success) "Print Success" else "Print Failed"
        }
    }

    /**
     * Factory for creating [HardwareViewModel] with [HardwareService].
     */
    class Factory(private val hardwareService: HardwareService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HardwareViewModel(hardwareService) as T
        }
    }
}
