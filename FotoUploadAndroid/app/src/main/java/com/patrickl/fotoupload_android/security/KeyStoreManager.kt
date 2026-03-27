package com.patrickl.fotoupload_android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore

object KeyStoreManager {

    private const val TAG = "KeyStoreManager.kt"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val CERT_PREFIX = "client_cert_"

    fun generateKeyPairIfNeeded(alias: String) {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                Log.d(TAG, "generateKeyPairIfNeeded: Alias '$alias' already exists.")
                return
            }
            
            Log.d(TAG, "generateKeyPairIfNeeded: Creating new key pair for alias '$alias'")
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build()

            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()
            Log.i(TAG, "generateKeyPairIfNeeded: Key pair successfully generated for alias '$alias'")
        } catch (e: Exception) {
            Log.e(TAG, "generateKeyPairIfNeeded: Failed to generate key pair for alias '$alias'", e)
            throw e
        }
    }

    fun getKeyStore(): KeyStore {
        return try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        } catch (e: Exception) {
            Log.e(TAG, "getKeyStore: Failed to load AndroidKeyStore", e)
            throw e
        }
    }

    fun getCertificate(alias: String) =
        try {
            getKeyStore().getCertificate(alias)
        } catch (e: Exception) {
            Log.w(TAG, "getCertificate: Could not find certificate for alias '$alias'")
            null
        }

    fun deleteKey(alias: String) {
        try {
            val keyStore = getKeyStore()
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                Log.i(TAG, "deleteKey: Deleted entry for alias '$alias'")
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteKey: Failed to delete entry for alias '$alias'", e)
        }
    }

    fun hasAnyCertificate(): Boolean {
        return try {
            val keyStore = getKeyStore()
            keyStore.aliases().asSequence().any { it.startsWith(CERT_PREFIX) }
        } catch (e: Exception) {
            Log.e(TAG, "hasAnyCertificate: Error checking aliases", e)
            false
        }
    }

    fun deleteAllCertificates() {
        try {
            val keyStore = getKeyStore()
            keyStore.aliases().asSequence().filter { it.startsWith(CERT_PREFIX) }.forEach {
                keyStore.deleteEntry(it)
                Log.d(TAG, "deleteAllCertificates: Deleted alias '$it'")
            }
            Log.i(TAG, "deleteAllCertificates: All client certificates deleted")
        } catch (e: Exception) {
            Log.e(TAG, "deleteAllCertificates: Error during deletion", e)
        }
    }
}
