package ru.nemodev.platform.core.api.dto.paging

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Страница пагинации")
data class PageDtoRs<T>(

    @Schema(description = "Набор элементов на текущей странице")
    val items: List<T>,

    @Schema(description = "Номер страницы", example = "0", minimum = "0", maximum = "999999999")
    val pageNumber: Int = 0,

    @Schema(description = "Кол-во элементов на странице", example = "25", minimum = "0", maximum = "999999999")
    val pageSize: Int = items.size,

    @Schema(description = "Признак наличия оставшихся страниц")
    val hasMore: Boolean = items.size >= pageSize
) {

    companion object {
        fun <T> empty() = PageDtoRs<T>(emptyList(), 0, 0)
    }
}