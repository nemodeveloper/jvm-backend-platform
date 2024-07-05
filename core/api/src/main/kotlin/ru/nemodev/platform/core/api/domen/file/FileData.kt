package ru.nemodev.platform.core.api.domen.file

import org.springframework.http.*
import java.net.URLEncoder

class FileData(
    val name: String,
    val extension: String,
    val mediaType: MediaType,
    val file: ByteArray
) {
    fun toResponseEntity(statusCode: HttpStatusCode = HttpStatus.OK): ResponseEntity<ByteArray> {
        return ResponseEntity.status(statusCode)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=${name}; filename*=UTF-8''${URLEncoder.encode(name, "UTF-8").replace("+", "%20")}"
            )
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .contentType(mediaType)
            .body(file)
    }
}
