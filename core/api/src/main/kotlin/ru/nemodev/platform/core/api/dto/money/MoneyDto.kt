package ru.nemodev.platform.core.api.dto.money

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Денежное представление")
data class MoneyDto(
    @Schema(description = "Сумма", example = "100.00", minimum = "-999999999", maximum = "999999999")
    val amount: BigDecimal,

    @Schema(description = "Валюта, по умолчанию рубли")
    val currency: CurrencyDto = CurrencyDto()
)

@Schema(description = "Информация о валюте")
data class CurrencyDto(
    @Schema(description = "Код", example = "RUB", maxLength = 3)
    val code: String = CURRENCY_CODE_RUB
) {
    companion object {
        const val CURRENCY_CODE_RUB = "RUB"
    }
}
