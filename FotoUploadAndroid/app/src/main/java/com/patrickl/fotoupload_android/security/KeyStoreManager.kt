package com.patrickl.fotoupload_android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore

object KeyStoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    //private const val KEY_ALIAS = "upload_client_key"

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
//    fun getPrivateKey() =
//        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
//            .getKey(KEY_ALIAS, null)
    fun getCertificate(alias: String) =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            .getCertificate(alias)
    fun deleteKey(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }
//    fun hasCertificate(): Boolean {
//        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
//        return keyStore.getCertificate(KEY_ALIAS) != null
//    }

}