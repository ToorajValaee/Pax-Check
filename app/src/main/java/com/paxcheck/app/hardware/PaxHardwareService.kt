package com.paxcheck.app.hardware

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.paxcheck.app.sdk.PaxSdkManager
import com.pax.dal.entity.EDetectMode
import com.pax.dal.entity.EPiccType
import com.pax.dal.entity.PiccCardInfo
import com.pax.dal.entity.TrackData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PaxHardwareService(private val sdkManager: PaxSdkManager) : HardwareService {

    override suspend fun readMsr(): HardwareResult<CardData> = withContext(Dispatchers.IO) {
        val dal = sdkManager.getDal()
        if (dal == null) {
            Log.e(TAG, "MSR: DAL not initialized")
            return@withContext HardwareResult.Error("DAL not initialized")
        }

        val mag = dal.getMag()
        return@withContext try {
            Log.d(TAG, "MSR: Opening Mag...")
            try {
                mag.open()
            } catch (e: Throwable) {
                Log.w(TAG, "MSR: Open failed, attempting recovery close and re-open: ${e.message}")
                try { mag.close() } catch (_: Throwable) {}
                mag.open()
            }

            Log.d(TAG, "MSR: Resetting Mag...")
            mag.reset()
            
            Log.d(TAG, "MSR: Polling for card swipe (30s timeout)...")
            val startTime = System.currentTimeMillis()
            var trackData: TrackData? = null
            
            var lastLogTime = 0L
            while (System.currentTimeMillis() - startTime < 30000) {
                try {
                    if (mag.isSwiped()) {
                        Log.i(TAG, "MSR: Swipe detected!")
                        trackData = mag.read()
                        Log.d(TAG, "MSR: Read result - resultCode: ${trackData?.resultCode}, t1: [${trackData?.track1}], t2: [${trackData?.track2}], t3: [${trackData?.track3}]")
                        if (trackData != null && (!trackData.track1.isNullOrEmpty() || !trackData.track2.isNullOrEmpty() || !trackData.track3.isNullOrEmpty())) {
                            break
                        } else {
                            Log.w(TAG, "MSR: Swipe detected but track data is empty, retrying...")
                            mag.reset()
                        }
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "MSR: Polling error: ${e.message}")
                }
                
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLogTime > 5000) {
                    Log.d(TAG, "MSR: Still waiting for swipe... (${(30000 - (currentTime - startTime)) / 1000}s left)")
                    lastLogTime = currentTime
                }
                
                delay(200)
            }

            if (trackData != null && 
                (!trackData.track1.isNullOrEmpty() || !trackData.track2.isNullOrEmpty() || !trackData.track3.isNullOrEmpty())) {
                Log.i(TAG, "MSR: Read successful")
                HardwareResult.Success(
                    CardData(
                        track1 = trackData.track1 ?: "",
                        track2 = trackData.track2 ?: "",
                        track3 = trackData.track3 ?: ""
                    )
                )
            } else {
                Log.w(TAG, "MSR: Read timeout or no valid data")
                HardwareResult.Error("Read timeout or no valid data")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "MSR: Error: ${e.message}", e)
            HardwareResult.Error("SDK Error: ${e.javaClass.name}: ${e.message ?: "Unknown error"}")
        } finally {
            try {
                mag.close()
                Log.d(TAG, "MSR: Closed")
            } catch (e: Throwable) {
                Log.e(TAG, "MSR: Error closing: ${e.message}")
            }
        }
    }

    override suspend fun printText(text: String): HardwareResult<Unit> = withContext(Dispatchers.IO) {
        val dal = sdkManager.getDal()
        if (dal == null) {
            Log.e(TAG, "Printer: DAL not initialized")
            return@withContext HardwareResult.Error("DAL not initialized")
        }

        val printer = dal.getPrinter()
        return@withContext try {
            Log.d(TAG, "Printer: Initializing...")
            printer.init()
            
            Log.d(TAG, "Printer: Checking status...")
            try {
                val status = printer.getStatus()
                Log.d(TAG, "Printer: Status code: $status")
                if (status == 1 || status == 4 || status == 8 || status == 9) {
                    val statusMsg = getPrinterErrorMessage(status)
                    Log.e(TAG, "Printer: Critical status error: $status ($statusMsg)")
                    return@withContext HardwareResult.Error("Printer Error: $statusMsg ($status)")
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Printer: getStatus threw exception (ignoring): ${e.message}")
            }
            
            Log.d(TAG, "Printer: Creating receipt bitmap...")
            val bitmap = createReceiptBitmap(text)
            
            Log.d(TAG, "Printer: Printing bitmap with mono threshold...")
            printer.printBitmapWithMonoThreshold(bitmap, 128)
            
            Log.d(TAG, "Printer: Stepping paper (150)...")
            printer.step(150)
            
            Log.d(TAG, "Printer: Starting print job...")
            val result = printer.start()
            if (result == 0) {
                Log.i(TAG, "Printer: Success")
                HardwareResult.Success(Unit)
            } else {
                val errorMsg = getPrinterErrorMessage(result)
                Log.e(TAG, "Printer: Failed with code: $result ($errorMsg)")
                HardwareResult.Error("Printer Error: $errorMsg ($result)")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Printer: Error: ${e.message}", e)
            HardwareResult.Error("SDK Error: ${e.javaClass.name}: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun readIcc(): HardwareResult<IccData> = withContext(Dispatchers.IO) {
        val dal = sdkManager.getDal()
        if (dal == null) {
            Log.e(TAG, "ICC: DAL not initialized")
            return@withContext HardwareResult.Error("DAL not initialized")
        }

        val icc = dal.getIcc()
        val slot: Byte = 0
        return@withContext try {
            Log.d(TAG, "ICC: Detecting card in slot $slot...")
            val startTime = System.currentTimeMillis()
            var atrBytes: ByteArray? = null

            while (System.currentTimeMillis() - startTime < 30000) {
                try {
                    if (icc.detect(slot)) {
                        Log.i(TAG, "ICC: Card detected! Initializing...")
                        atrBytes = icc.init(slot)
                        if (atrBytes != null && atrBytes.isNotEmpty()) {
                            break
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "ICC: Polling/init error: ${e.message}")
                }
                delay(500)
            }

            if (atrBytes != null && atrBytes.isNotEmpty()) {
                val atrHex = atrBytes.joinToString("") { "%02X".format(it) }
                Log.i(TAG, "ICC: Init successful, ATR: $atrHex")
                HardwareResult.Success(IccData(atrHex = atrHex, slot = slot.toInt()))
            } else {
                Log.w(TAG, "ICC: Read timeout or no card inserted")
                HardwareResult.Error("IC Card read timeout or no card inserted")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "ICC: Error: ${e.message}", e)
            HardwareResult.Error("SDK Error: ${e.javaClass.name}: ${e.message ?: "Unknown error"}")
        } finally {
            try {
                icc.close(slot)
                Log.d(TAG, "ICC: Closed slot $slot")
            } catch (e: Throwable) {
                Log.e(TAG, "ICC: Error closing slot: ${e.message}")
            }
        }
    }

    override suspend fun readPicc(): HardwareResult<PiccData> = withContext(Dispatchers.IO) {
        val dal = sdkManager.getDal()
        if (dal == null) {
            Log.e(TAG, "PICC: DAL not initialized")
            return@withContext HardwareResult.Error("DAL not initialized")
        }

        val picc = dal.getPicc(EPiccType.INTERNAL)
        return@withContext try {
            try { picc.close() } catch (_: Throwable) {}
            Log.d(TAG, "PICC: Opening Contactless Reader...")
            picc.open()

            Log.d(TAG, "PICC: Polling for contactless card (30s timeout)...")
            val startTime = System.currentTimeMillis()
            var cardInfo: PiccCardInfo? = null

            while (System.currentTimeMillis() - startTime < 30000) {
                try {
                    cardInfo = picc.detect(EDetectMode.ISO14443_AB)
                    if (cardInfo != null) {
                        break
                    }
                } catch (_: Throwable) {}
                delay(200)
            }

            if (cardInfo != null) {
                val serialHex = cardInfo.serialInfo?.joinToString("") { "%02X".format(it) } ?: "N/A"
                Log.i(TAG, "PICC: Card detected! Type: ${cardInfo.cardType}, UID: $serialHex")
                HardwareResult.Success(
                    PiccData(
                        cardType = cardInfo.cardType.toInt(),
                        serialNumberHex = serialHex
                    )
                )
            } else {
                Log.w(TAG, "PICC: Read timeout or no contactless card detected")
                HardwareResult.Error("Contactless card read timeout or no card detected")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "PICC: Error: ${e.message}", e)
            HardwareResult.Error("SDK Error: ${e.javaClass.name}: ${e.message ?: "Unknown error"}")
        } finally {
            try {
                picc.close()
                Log.d(TAG, "PICC: Closed Contactless Reader")
            } catch (e: Throwable) {
                Log.e(TAG, "PICC: Error closing: ${e.message}")
            }
        }
    }

    private fun createReceiptBitmap(text: String): Bitmap {
        val width = 384
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }

        @Suppress("DEPRECATION")
        val staticLayout = StaticLayout(
            text,
            textPaint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            1.0f,
            0.0f,
            false
        )

        val height = staticLayout.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        staticLayout.draw(canvas)

        return bitmap
    }

    private fun getPrinterErrorMessage(result: Int): String {
        return when (result) {
            0 -> "Success"
            1 -> "Out of paper"
            2 -> "Device busy"
            3 -> "The print data is too long"
            4 -> "Printer overheat"
            8 -> "Printer voltage too low"
            9 -> "Printer paper jam"
            -1 -> "General error"
            -2 -> "Invalid parameter"
            -3 -> "Device not supported"
            -4 -> "Device occupied"
            -16 -> "Communication error / Not initialized"
            else -> "Unknown error code: $result"
        }
    }

    companion object {
        private const val TAG = "PaxHardwareService"
    }
}
