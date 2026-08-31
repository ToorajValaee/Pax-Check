package com.example.paxcheck.hardware

/**
 * Interface defining hardware operations for MSR and Printer.
 */
interface HardwareService {
    /**
     * Reads card data from the MSR.
     * @return Track data as a string, or null if reading failed or was cancelled.
     */
    suspend fun readMsr(): String?

    /**
     * Prints text using the thermal printer.
     * @param text The text to print.
     * @return True if printing was successful, false otherwise.
     */
    suspend fun printText(text: String): Boolean
}
