package com.example.paxcheck.hardware

/**
 * Data class representing card track data.
 */
data class CardData(
    val track1: String? = null,
    val track2: String? = null,
    val track3: String? = null
)

/**
 * Data class representing IC card (chip card) data.
 */
data class IccData(
    val atrHex: String,
    val slot: Int
)

/**
 * Sealed class representing the result of a hardware operation.
 */
sealed class HardwareResult<out T> {
    data class Success<out T>(val data: T) : HardwareResult<T>()
    data class Error(val message: String) : HardwareResult<Nothing>()
}

/**
 * Interface defining hardware operations for MSR, Printer, and IC Card.
 */
interface HardwareService {
    /**
     * Reads card data from the MSR.
     * @return [HardwareResult] containing [CardData] or error message.
     */
    suspend fun readMsr(): HardwareResult<CardData>

    /**
     * Prints text using the thermal printer.
     * @param text The text to print.
     * @return [HardwareResult] indicating success or failure with message.
     */
    suspend fun printText(text: String): HardwareResult<Unit>

    /**
     * Reads IC card (chip card) data from the smart card slot.
     * @return [HardwareResult] containing [IccData] or error message.
     */
    suspend fun readIcc(): HardwareResult<IccData>
}
