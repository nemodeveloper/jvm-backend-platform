package ru.nemodev.platform.core.extensions

fun String.parseBasicAuth() =
    substring(6, length)
    .decodeBase64()
    .split(":")
    .let { Pair(it[0], it[1]) }
