package ru.nemodev.platform.core.extensions

import com.ibm.icu.text.Transliterator
import org.springframework.http.MediaType
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

private const val ONLY_DIGIT_REGEX_PATTERN = "^[0-9]+$"
private val ONLY_DIGIT_REGEX = Regex(ONLY_DIGIT_REGEX_PATTERN)
private val CYRILLIC_TO_LATIN_TRANSLITERATOR = Transliterator.getInstance("Russian-Latin/BGN")

private val utf8 = Charset.forName("UTF-8")

val fileExtensionToContentTypeMap = mapOf(
    "html" to MediaType.TEXT_HTML_VALUE,
    "pdf" to MediaType.APPLICATION_PDF_VALUE,
    "csv" to "text/csv",
    "xls" to "application/vnd.ms-excel",
    "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "png" to MediaType.IMAGE_PNG_VALUE,
    "jpeg" to MediaType.IMAGE_JPEG_VALUE,
    "txt" to MediaType.TEXT_PLAIN_VALUE
)

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(
        URLEncoder.encode(
            this,
            utf8
        ).toByteArray()
    )
}

fun String.decodeBase64(throwException: Boolean = false): String {
    try {
        return URLDecoder.decode(
            Base64.getDecoder().decode(this).decodeToString(),
            utf8
        )
    } catch (e: Exception) {
        if (throwException) {
            throw e
        }
    }

    return this
}

fun CharSequence?.isNotNullOrEmpty() = !this.isNullOrEmpty()

fun CharSequence?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

fun CharSequence.splitIgnoreEmpty(vararg delimiters: String) = this.split(*delimiters).filter { it.isNotEmpty() }

fun String.nullIfEmpty(): String? = this.ifEmpty { null }

fun String?.isNumeric(): Boolean = this?.matches(ONLY_DIGIT_REGEX) == true

fun String?.toStringIfNumOrNull() = if (this?.isNumeric() == true) this else null

fun String.removeAllSpecialChars() = this.filter { it.isLetterOrDigit() }

fun String.getFileExtension(): String {
    if (this.lastIndexOf(".") == -1) {
        throw IllegalArgumentException("У файла $this не указан тип файла")
    }
    return this.substringAfterLast(".")
}

fun String.getFileName(): String {
    return this.substringBeforeLast(".")
}

fun String.getFileContentType(): String {
    return fileExtensionToContentTypeMap[this]
        ?: throw IllegalStateException("Неподдерживаемый тип файла $this")
}

fun String.parseBasicAuth(): Pair<String, String> {
    val base64 = String(Base64.getDecoder().decode(this.substring(6, this.length)))
    val usernameAndPassword = base64.split(":")
    return Pair(usernameAndPassword[0], usernameAndPassword[1])
}

fun String.getMediaType(): MediaType {
    return MediaType.parseMediaType(this.getFileContentType())
}

fun String.transliterateCyrillicToLatin(): String {
    return CYRILLIC_TO_LATIN_TRANSLITERATOR.transliterate(this.replace(' ', '_'))
}
