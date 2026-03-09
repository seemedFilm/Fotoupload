package com.patrickl.fotoupload_android.security

import kotlinx.serialization.descriptors.PrimitiveKind
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.security.KeyStore
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.util.Base64
import java.security.PrivateKey

object CsrGenerator {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun generateCsr(
        commonName: String,
        alias: String
    ):
            String {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = keyStore.getKey(alias, null) as? PrivateKey
            ?: throw IllegalStateException("PrivateKey not found in Keystore")
        val publicKey = keyStore.getCertificate(alias).publicKey
        val subject = X500Name("CN=$commonName")
        val csrBuilder = JcaPKCS10CertificationRequestBuilder(
            subject,
            publicKey
        )
        val signer = JcaContentSignerBuilder("SHA256withRSA")
            .build(privateKey)
        val csr: PKCS10CertificationRequest = csrBuilder.build(signer)
        val pem = Base64.getEncoder().encodeToString(csr.encoded)
        return """
            -----BEGIN CERTIFICATE REQUEST-----
            $pem
            -----END CERTIFICATE REQUEST-----
        """.trimIndent()
    }
}