package com.patrickl.fotoupload_android.enroll

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security
import java.security.Signature
import org.bouncycastle.jce.provider.BouncyCastleProvider

object CsrHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun generateKeyPair(alias: String): KeyPair {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or 
            KeyProperties.PURPOSE_VERIFY or 
            KeyProperties.PURPOSE_DECRYPT
        )
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_NONE)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        kpg.initialize(spec)
        return kpg.generateKeyPair()
    }

    fun getKeyPair(alias: String): KeyPair {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE)
        ks.load(null)

        val privateKey = ks.getKey(alias, null) as PrivateKey
        val publicKey = ks.getCertificate(alias).publicKey

        return KeyPair(publicKey, privateKey)
    }

    fun createCsr(alias: String, deviceId: String): String {
        val keyPair = getKeyPair(alias)
        val subject = X500Name("CN=$deviceId")

        val csrBuilder = JcaPKCS10CertificationRequestBuilder(
            subject,
            keyPair.public
        )

        // Custom ContentSigner to work around AndroidKeyStore provider issues with BouncyCastle.
        // We remove the explicit "AndroidKeyStore" provider from Signature.getInstance
        // because it often causes NoSuchAlgorithmException on some devices.
        // The system will automatically select the appropriate provider (e.g. Conscrypt)
        // that can handle the AndroidKeyStore private key.
        val signer = object : ContentSigner {
            private val signature = Signature.getInstance("SHA256withRSA").apply {
                initSign(keyPair.private)
            }
            private val outputStream = ByteArrayOutputStream()

            override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
                return DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA")
            }

            override fun getOutputStream(): OutputStream = outputStream

            override fun getSignature(): ByteArray {
                signature.update(outputStream.toByteArray())
                return signature.sign()
            }
        }

        val csr: PKCS10CertificationRequest = csrBuilder.build(signer)
        return convertToPem(csr.encoded)
    }

    private fun convertToPem(der: ByteArray): String {
        val base64 = Base64.encodeToString(der, Base64.NO_WRAP)
            .chunked(64)
            .joinToString("\n")

        return """
-----BEGIN CERTIFICATE REQUEST-----
$base64
-----END CERTIFICATE REQUEST-----
""".trimIndent()
    }
}