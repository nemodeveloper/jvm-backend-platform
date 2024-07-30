package ru.nemodev.platform.core.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.scaleAndRoundAmount() = setScale(2, RoundingMode.DOWN)