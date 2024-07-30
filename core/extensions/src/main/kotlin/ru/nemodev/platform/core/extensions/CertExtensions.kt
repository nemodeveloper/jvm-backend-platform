package ru.nemodev.platform.core.extensions

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

private val rsaKeyFactory = KeyFactory.getInstance("RSA")

fun String.toRSAPublicKey() =
    rsaKeyFactory.generatePublic(
        toX509EncodedKeySpec()
    ) as RSAPublicKey

fun String.toRSAPrivateKey() =
    rsaKeyFactory.generatePrivate(
        toPKCS8EncodedKeySpec()
    ) as RSAPrivateKey

fun String.toX509EncodedKeySpec() =
    X509EncodedKeySpec(cleanPublicKey().decodeBase64ToBateArray())

fun String.toPKCS8EncodedKeySpec() =
    PKCS8EncodedKeySpec(cleanPrivateKey().decodeBase64ToBateArray())

fun String.cleanPublicKey(): String {
    return replace("\n", "")
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
}

fun String.cleanPrivateKey(): String {
    return replace("\n", "")
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
}