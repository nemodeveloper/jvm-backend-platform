package ru.nemodev.platform.core.extensions

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

private val rsaKeyFactory = KeyFactory.getInstance("RSA")

fun String.toRSAPublicKey(): RSAPublicKey {
    return rsaKeyFactory.generatePublic(toX509EncodedKeySpec()) as RSAPublicKey
}

fun String.toRSAPrivateKey(): RSAPrivateKey {
    return rsaKeyFactory.generatePrivate(toPKCS8EncodedKeySpec()) as RSAPrivateKey
}

fun String.toX509EncodedKeySpec(): X509EncodedKeySpec {
    return X509EncodedKeySpec(
        Base64.getDecoder().decode(
            clearPublicKey()
        )
    )
}

fun String.toPKCS8EncodedKeySpec(): PKCS8EncodedKeySpec {
    return PKCS8EncodedKeySpec(
        Base64.getDecoder().decode(
            clearPrivateKey()
        )
    )
}

fun String.clearPublicKey(): String {
    return replace("\\n", "")
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
}

fun String.clearPrivateKey(): String {
    return replace("\\n", "")
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
}