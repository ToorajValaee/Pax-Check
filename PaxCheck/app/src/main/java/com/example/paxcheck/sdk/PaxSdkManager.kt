package com.example.paxcheck.sdk

import android.content.Context
import android.util.Log
import com.pax.dal.IDal
import com.pax.neptunelite.api.NeptuneLiteUser

/**
 * Manager class for PAX SDK initialization and access.
 * Handles the binding/initialization of the NeptuneLite API and IDal interface.
 */
class PaxSdkManager private constructor(private val context: Context) {

    private var dal: IDal? = null

    /**
     * Initializes the NeptuneLite SDK and retrieves the IDal interface.
     * This should be called early in the application lifecycle.
     */
    fun init() {
        if (dal != null) return

        try {
            Log.d(TAG, "Initializing NeptuneLite SDK...")
            val neptuneLiteApi = NeptuneLiteUser.getInstance().getApi(context)
            if (neptuneLiteApi != null) {
                dal = neptuneLiteApi.getDal(context)
                Log.i(TAG, "NeptuneLite SDK initialized successfully. IDal obtained.")
            } else {
                Log.e(TAG, "Failed to get NeptuneLiteApi instance.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing NeptuneLite SDK: ${e.message}", e)
        }
    }

    /**
     * Returns the initialized IDal interface, or null if not initialized.
     */
    fun getDal(): IDal? = dal

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
