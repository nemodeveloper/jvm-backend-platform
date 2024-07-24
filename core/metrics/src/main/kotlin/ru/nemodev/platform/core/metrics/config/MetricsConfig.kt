package ru.nemodev.platform.core.metrics.config

import org.springframework.boot.actuate.autoconfigure.availability.AvailabilityProbesAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration

@AutoConfiguration(before = [AvailabilityProbesAutoConfiguration::class])
class MetricsConfig