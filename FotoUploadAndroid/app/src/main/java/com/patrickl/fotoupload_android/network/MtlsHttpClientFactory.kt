package com.patrickl.fotoupload_android.network

import android.content.Context
import android.util.Log
import com.patrickl.fotoupload_android.R
import com.patrickl.fotoupload_android.security.KeyStoreManager
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.net.Socket
import javax.net.ssl.*

private const val TAG = "MtlsHttpClientFactory.kt"

class MtlsHttpClientFactory(
    private val context: Context
) {

    fun create(preferredAlias: String? = null): OkHttpClient {
        Log.d(TAG, "create: Initializing MTLS OkHttpClient (Preferred Alias: $preferredAlias)")
        
        try {
            val keyStore = KeyStoreManager.getKeyStore()
            
            // 1. Setup KeyManager with alias filtering
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, null)
            val originalKeyManager = kmf.keyManagers.first { it is X509KeyManager } as X509KeyManager
            
            val filteredKeyManager = object : X509ExtendedKeyManager() {
                override fun chooseClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, socket: Socket?): String? = preferredAlias ?: originalKeyManager.chooseClientAlias(keyType, issuers, socket)
                override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = originalKeyManager.getClientAliases(keyType, issuers)
                override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = originalKeyManager.getServerAliases(keyType, issuers)
                override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String? = originalKeyManager.chooseServerAlias(keyType, issuers, socket)
                override fun getCertificateChain(alias: String?): Array<X509Certificate>? = originalKeyManager.getCertificateChain(alias)
                override fun getPrivateKey(alias: String?): PrivateKey? = originalKeyManager.getPrivateKey(alias)
            }

            // 2. Setup TrustManager explicitly loading your CA
            val trustStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
            val caInput = context.resources.openRawResource(R.raw.ca_cert)
            val cf = CertificateFactory.getInstance("X.509")
            val caCert = cf.generateCertificate(caInput) as X509Certificate
            trustStore.setCertificateEntry("ca", caCert)
            caInput.close()
            Log.d(TAG, "create: Trusted CA loaded: ${caCert.subjectDN}")

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(trustStore)
            val trustManager = tmf.trustManagers[0] as X509TrustManager

            // 3. Setup SSLContext
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(arrayOf(filteredKeyManager), tmf.trustManagers, null)

            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .hostnameVerifier { hostname, _ -> 
                    Log.d(TAG, "Verifying hostname: $hostname")
                    true 
                }
                .build()
                
        } catch (e: Exception) {
            Log.e(TAG, "create: Error creating MTLS client", e)
            throw e
        }
    }
}
