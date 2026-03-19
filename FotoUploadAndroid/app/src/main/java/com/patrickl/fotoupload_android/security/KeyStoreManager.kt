package com.patrickl.fotoupload_android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore

object KeyStoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val CERT_PREFIX = "client_cert_"

    fun generateKeyPairIfNeeded(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) return
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    fun getCertificate(alias: String) =
        getKeyStore().getCertificate(alias)

    fun deleteKey(alias: String) {
        val keyStore = getKeyStore()
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    fun hasAnyCertificate(): Boolean {
        val keyStore = getKeyStore()
        return keyStore.aliases().asSequence().any { it.startsWith(CERT_PREFIX) }
    }

    fun deleteAllCertificates() {
        val keyStore = getKeyStore()
        keyStore.aliases().asSequence().filter { it.startsWith(CERT_PREFIX) }.forEach {
            keyStore.deleteEntry(it)
        }
    }
}