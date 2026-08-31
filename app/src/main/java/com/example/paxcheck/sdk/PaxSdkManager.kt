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
 * Manager class for PAX SDK initialization and access.
 * Handles the binding/initialization of the NeptuneLite API and IDAL interface.
 */
class PaxSdkManager private constructor(private val context: Context) {

    private var dal: IDAL? = null

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status.asStateFlow()

    /**
     * Initializes the NeptuneLite SDK and retrieves the IDAL interface.
     * This should be called early in the application lifecycle.
     */
    fun init() {
        if (dal != null) {
            _status.value = "Connected"
            return
        }

        try {
            Log.d(TAG, "Initializing NeptuneLite SDK...")
            _status.value = "Initializing..."
            
            checkPermissions()
            
            Log.d(TAG, "Context info: pkg=${context.packageName}, filesDir=${context.filesDir?.absolutePath}")
            val dalInstance = acquireDal()

            if (dalInstance != null) {
                Log.i(TAG, "NeptuneLite SDK initialized. Performing health check...")
                // Perform a quick health check to ensure native libs are loadable
                try {
                    dalInstance.getMag()
                    Log.i(TAG, "Health check passed. NeptuneLite SDK ready.")
                    dal = dalInstance
                    _status.value = "Connected"
                } catch (t: UnsatisfiedLinkError) {
                    Log.e(TAG, "Native library missing (health check): ${t.message}", t)
                    val errorMsg = "Error: Missing Native Libraries (UnsatisfiedLinkError). Please check .so files."
                    _status.value = errorMsg
                    dal = null
                } catch (t: Throwable) {
                    Log.e(TAG, "Health check failed: ${t.message}", t)
                    val errorMsg = "Error: SDK Health Check Failed (${t.javaClass.simpleName}: ${t.message})"
                    _status.value = errorMsg
                    dal = null
                }
            } else {
                Log.e(TAG, "Failed to get IDAL instance (acquireDal returned null).")
                // _status was already updated inside acquireDal with more specific error if possible
                if (_status.value == "Initializing...") {
                    _status.value = "Error: IDAL Null (Reason Unknown)"
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Native library missing during init: ${e.message}", e)
            _status.value = "Error: Native Libraries Missing (UnsatisfiedLinkError)"
        } catch (e: Throwable) {
            Log.e(TAG, "Error initializing NeptuneLite SDK: ${e.message}", e)
            _status.value = "Error: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    /**
     * Returns the IDAL interface.
     * Re-acquires it from NeptuneLiteUser to ensure it's fresh.
     */
    fun getDal(): IDAL? {
        if (dal == null) {
            Log.d(TAG, "getDal() called but dal is null. Attempting re-init...")
            dal = acquireDal()
            if (dal != null) {
                Log.i(TAG, "Re-init successful, dal acquired.")
            } else {
                Log.e(TAG, "Re-init failed, dal still null.")
            }
        }
        return dal
    }

    /**
     * Attempts to acquire the IDAL instance using multiple strategies to handle
     * different NeptuneLite SDK versions or runtime environments.
     */
    private fun acquireDal(): IDAL? {
        val neptuneLiteUser = try {
            Log.d(TAG, "Attempting to get NeptuneLiteUser instance...")
            NeptuneLiteUser.getInstance()
        } catch (t: Throwable) {
            val errorMsg = "Failed to get NeptuneLiteUser instance: ${t.message}"
            Log.e(TAG, errorMsg, t)
            _status.value = "Error: $errorMsg"
            return null
        }

        // Log available methods for debugging
        try {
            val methods = neptuneLiteUser.javaClass.methods
            Log.d(TAG, "NeptuneLiteUser available methods (${methods.size}):")
            methods.forEach { Log.v(TAG, "  - ${it.name}(${it.parameterTypes.joinToString { p -> p.simpleName }}) -> ${it.returnType.simpleName}") }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to log NeptuneLiteUser methods: ${e.message}")
        }

        val appCtx = context.applicationContext

        // Strategy 1: Standard getDal(Context)
        try {
            Log.d(TAG, "Attempting strategy: getDal(context)...")
            val instance = neptuneLiteUser.getDal(appCtx)
            if (instance != null) {
                Log.i(TAG, "Strategy 'getDal(context)' succeeded.")
                return instance
            }
            Log.w(TAG, "Strategy 'getDal(context)' returned null.")
        } catch (_: NoSuchMethodError) {
            Log.w(TAG, "getDal(Context) method not found (NoSuchMethodError).")
        } catch (e: Throwable) {
            val msg = e.message ?: e.javaClass.simpleName
            Log.e(TAG, "Error in 'getDal(context)': $msg")
            if (msg.contains("LOAD DAL ERR")) {
                _status.value = "Error: $msg (Check if on PAX hardware)"
            }
        }

        // Strategy 2: getDalWithProcessSafe(Context)
        try {
            Log.d(TAG, "Attempting strategy: getDalWithProcessSafe(context)...")
            val instance = neptuneLiteUser.getDalWithProcessSafe(appCtx)
            if (instance != null) {
                Log.i(TAG, "Strategy 'getDalWithProcessSafe(context)' succeeded.")
                return instance
            }
            Log.w(TAG, "Strategy 'getDalWithProcessSafe(context)' returned null.")
        } catch (_: NoSuchMethodError) {
            Log.w(TAG, "getDalWithProcessSafe(Context) method not found.")
        } catch (e: Throwable) {
            Log.e(TAG, "Error in 'getDalWithProcessSafe(context)': ${e.message}")
        }

        // Strategy 3: Reflection - check for getDal() without arguments
        try {
            Log.d(TAG, "Attempting strategy: getDal() via reflection...")
            val method = neptuneLiteUser.javaClass.getMethod("getDal")
            val instance = method.invoke(neptuneLiteUser) as? IDAL
            if (instance != null) {
                Log.i(TAG, "Strategy 'getDal()' succeeded.")
                return instance
            }
        } catch (_: NoSuchMethodException) {
            // Ignore
        } catch (e: Throwable) {
            Log.d(TAG, "Strategy 'getDal()' failed: ${e.message}")
        }

        // Strategy 4: Reflection - check for getApi(Context)
        try {
            Log.d(TAG, "Attempting strategy: getApi(context) via reflection...")
            val method = neptuneLiteUser.javaClass.getMethod("getApi", Context::class.java)
            val instance = method.invoke(neptuneLiteUser, appCtx) as? IDAL
            if (instance != null) {
                Log.i(TAG, "Strategy 'getApi(context)' succeeded.")
                return instance
            }
        } catch (_: NoSuchMethodException) {
            // Ignore
        } catch (e: Throwable) {
            Log.d(TAG, "Strategy 'getApi(context)' failed: ${e.message}")
        }

        // Strategy 5: Exhaustive search for any method returning IDAL
        try {
            Log.d(TAG, "Attempting strategy: Exhaustive search for method returning IDAL...")
            for (method in neptuneLiteUser.javaClass.methods) {
                if (IDAL::class.java.isAssignableFrom(method.returnType)) {
                    Log.i(TAG, "Potential DAL method found: ${method.name}")
                    val instance = if (method.parameterTypes.isEmpty()) {
                        method.invoke(neptuneLiteUser) as? IDAL
                    } else if (method.parameterTypes.size == 1 && method.parameterTypes[0] == Context::class.java) {
                        method.invoke(neptuneLiteUser, appCtx) as? IDAL
                    } else {
                        null
                    }
                    
                    if (instance != null) {
                        Log.i(TAG, "Strategy 'Exhaustive: ${method.name}' succeeded.")
                        return instance
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exhaustive reflection search failed: ${e.message}")
        }

        _status.value = "Error: All DAL acquisition strategies failed"
        return null
    }

    private fun checkPermissions() {
        val paxPermissions = listOf(
            "com.pax.permission.ICC",
            "com.pax.permission.PICC",
            "com.pax.permission.MAG",
            "com.pax.permission.PRINTER",
            "com.pax.permission.PED"
        )
        
        Log.d(TAG, "Checking PAX permissions:")
        paxPermissions.forEach { permission ->
            val granted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "  - $permission: ${if (granted) "GRANTED" else "DENIED"}")
        }
    }

    companion object {
        private const val TAG = "PaxSdkManager"

        @Volatile
        private var instance: PaxSdkManager? = null

        fun getInstance(context: Context): PaxSdkManager {
            return instance ?: synchronized(this) {
                instance ?: PaxSdkManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
