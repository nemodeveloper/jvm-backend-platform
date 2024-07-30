package ru.nemodev.platform.core.extensions

import com.ibm.icu.text.Transliterator

private const val ONLY_DIGIT_REGEX_PATTERN = "^[0-9]+$"
private val onlyDigitRegex = Regex(ONLY_DIGIT_REGEX_PATTERN)
private val cyrillicToLatinTransliterator = Transliterator.getInstance("Russian-Latin/BGN")

fun CharSequence?.isNotNullOrEmpty() = !isNullOrEmpty()

fun CharSequence?.isNotNullOrBlank(): Boolean = !isNullOrBlank()

fun CharSequence.splitIgnoreEmpty(vararg delimiters: String) = split(*delimiters).filter { it.isNotEmpty() }

fun String.nullIfEmpty(): String? = ifEmpty { null }

fun String?.isNumeric(): Boolean = this?.matches(onlyDigitRegex) == true

fun String?.toNumericOrNull() = if (isNumeric()) this else null

fun String.removeAllSpecialChars() = filter { it.isLetterOrDigit() }

fun String.transliterateCyrillicToLatin() =
    cyrillicToLatinTransliterator.transliterate(replace(' ', '_'))
