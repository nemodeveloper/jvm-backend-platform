package ru.nemodev.platform.core.extensions

import java.security.MessageDigest

private val md5 = MessageDigest.getInstance("MD5")

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.md5(): String {
    return md5.digest(this).toHexString()
}
