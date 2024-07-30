package ru.nemodev.platform.core.extensions

import org.springframework.http.MediaType

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

fun String.getFileExtension(): String {
    if (lastIndexOf(".") == -1) {
        throw IllegalArgumentException("У файла $this не указан тип файла")
    }
    return substringAfterLast(".")
}

fun String.getFileName() = substringBeforeLast(".")

fun String.getFileContentType(): String {
    return fileExtensionToContentTypeMap[this]
        ?: throw IllegalStateException("Неподдерживаемый тип файла $this")
}

fun String.getMediaType() = MediaType.parseMediaType(getFileContentType())
