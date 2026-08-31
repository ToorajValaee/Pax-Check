package com.example.paxcheck.hardware

import android.util.Log
import com.example.paxcheck.sdk.PaxSdkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * PAX implementation of [HardwareService].
 * Wraps NeptuneLite SDK calls using Coroutines for asynchronous execution.
 */
class PaxHardwareService(private val sdkManager: PaxSdkManager) : HardwareService {

    override suspend fun readMsr(): String? = withContext(Dispatchers.IO) {
        val dal = sdkManager.getDal()
        if (dal == null) {
            Log.e(TAG, "DAL not initialized")
            return@withContext null
        }

        val msr = dal.getMsr()
        return@withContext try {
            Log.d(TAG, "Opening MSR...")
            msr.open()
            msr.reset()
            Log.d(TAG, "Reading MSR data...")
            val data = msr.read()
            Log.d(TAG, "MSR read successful")
            data
        } catch (e: Exception) {
            Log.e(TAG, "Error reading MSR: ${e.message}", e)
            null
        } finally {
            try {
                msr.close()
                Log.d(TAG, "MSR closed")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing MSR: ${e.message}")
            }
        }
    }

    override suspend fun printText(text: String): Boolean = withContext(Dispatchers.IO) {
        val dal = sdkManager.getDal()
        if (dal == null) {
            Log.e(TAG, "DAL not initialized")
            return@withContext false
        }

        val printer = dal.getPrinter()
        return@withContext try {
            Log.d(TAG, "Initializing printer...")
            printer.init()
            Log.d(TAG, "Adding text to printer: $text")
            printer.printStr(text, null)
            Log.d(TAG, "Starting print job...")
            val result = printer.start()
            if (result == 0) {
                Log.i(TAG, "Printing successful")
                true
            } else {
                Log.e(TAG, "Printing failed with code: $result")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during printing: ${e.message}", e)
            false
        }
    }

    companion object {
        private const val TAG = "PaxHardwareService"
    }
}
