package com.paxcheck.app.hardware

data class CardData(
    val track1: String? = null,
    val track2: String? = null,
    val track3: String? = null
)

data class IccData(
    val atrHex: String,
    val slot: Int
)

data class PiccData(
    val cardType: Int,
    val serialNumberHex: String
)

sealed class HardwareResult<out T> {
    data class Success<out T>(val data: T) : HardwareResult<T>()
    data class Error(val message: String) : HardwareResult<Nothing>()
}

interface HardwareService {
    suspend fun readMsr(): HardwareResult<CardData>
    suspend fun printText(text: String): HardwareResult<Unit>
    suspend fun readIcc(): HardwareResult<IccData>
    suspend fun readPicc(): HardwareResult<PiccData>
}
