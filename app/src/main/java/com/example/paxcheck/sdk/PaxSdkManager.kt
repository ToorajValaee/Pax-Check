package com.example.paxcheck.sdk

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.pax.dal.IDAL
import com.pax.neptunelite.api.NeptuneLiteUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns the NeptuneLite DAL connection used by the hardware layer.
 *
 * The application intentionally uses the SDK API from the bundled
 * NeptuneLiteApi_V4.15.00_20250606.jar directly instead of hiding failures
 * behind reflection/version fallbacks. This makes device-side SDK errors
 * visible and actionable.
 */
class PaxSdkManager private constructor(private val context: Context) {

    @Volatile
    private var dal: IDAL? = null

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status.asStateFlow()

    fun init() {
        if (dal != null) {
            _status.value = "Connected"
            return
        }

        _status.value = "Initializing..."
        checkPermissions()
        dal = acquireDal()
    }

    fun getDal(): IDAL? {
        dal?.let { return it }
        return acquireDal().also { dal = it }
    }

    private fun acquireDal(): IDAL? {
        return try {
            val appContext = context.applicationContext
            Log.i(TAG, "Connecting to NeptuneLite DAL for ${appContext.packageName}")

            val instance = NeptuneLiteUser.getInstance().getDal(appContext)
            if (instance == null) {
                _status.value = "Error: NeptuneLite returned a null DAL"
                Log.e(TAG, _status.value)
                null
            } else {
                // Touch a real hardware interface so native/service linkage errors are
                // reported during initialization instead of on the first button press.
                instance.getMag()
                _status.value = "Connected"
                Log.i(TAG, "NeptuneLite DAL connected")
                instance
            }
        } catch (e: UnsatisfiedLinkError) {
            val detail = e.message ?: "native library could not be loaded"
            _status.value = "Error: NeptuneLite native runtime unavailable: $detail"
            Log.e(TAG, _status.value, e)
            null
        } catch (e: SecurityException) {
            val detail = e.message ?: "hardware permission denied"
            _status.value = "Error: PAX permission denied: $detail"
            Log.e(TAG, _status.value, e)
            null
        } catch (e: Throwable) {
            val detail = e.message ?: e.javaClass.simpleName
            _status.value = "Error: NeptuneLite initialization failed: $detail"
            Log.e(TAG, _status.value, e)
            null
        }
    }

    private fun checkPermissions() {
        PAX_PERMISSIONS.forEach { permission ->
            val granted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "$permission: ${if (granted) "GRANTED" else "DENIED"}")
        }
    }

    companion object {
        private const val TAG = "PaxSdkManager"

        private val PAX_PERMISSIONS = listOf(
            "com.pax.permission.ICC",
            "com.pax.permission.PICC",
            "com.pax.permission.MAG",
            "com.pax.permission.PRINTER",
            "com.pax.permission.PED"
        )

        @Volatile
        private var instance: PaxSdkManager? = null

        fun getInstance(context: Context): PaxSdkManager {
            return instance ?: synchronized(this) {
                instance ?: PaxSdkManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
