package ru.nemodev.platform.core.starter.config

import org.springframework.boot.actuate.autoconfigure.availability.AvailabilityProbesAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration

@AutoConfiguration(before = [AvailabilityProbesAutoConfiguration::class])
class CoreStarterConfig
