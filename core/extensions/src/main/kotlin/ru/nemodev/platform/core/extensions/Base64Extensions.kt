package ru.nemodev.platform.core.extensions

import java.util.*

fun String.encodeBase64() = Base64.getEncoder().encodeToString(toByteArray())
fun String.decodeBase64() = Base64.getDecoder().decode(this).decodeToString()
fun String.decodeBase64ToBateArray() = Base64.getDecoder().decode(this)

fun String.encodeBase64URL() = Base64.getUrlEncoder().encodeToString(toByteArray())
fun String.decodeBase64URL() = Base64.getUrlDecoder().decode(this).decodeToString()
fun String.decodeBase64URLToByteArray() = Base64.getUrlDecoder().decode(this)
