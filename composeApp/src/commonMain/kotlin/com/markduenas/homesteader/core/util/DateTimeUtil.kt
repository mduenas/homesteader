package com.markduenas.homesteader.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun currentInstant(): Instant

object DateTimeUtil {
    fun now(): Instant = currentInstant()

    fun nowIsoString(): String = now().toString()

    fun today(): LocalDate {
        return now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun parseIsoDate(isoString: String?): LocalDate? {
        if (isoString.isNullOrBlank()) return null
        return try {
            LocalDate.parse(isoString)
        } catch (e: Exception) {
            null
        }
    }

    fun parseIsoInstant(isoString: String?): Instant? {
        if (isoString.isNullOrBlank()) return null
        return try {
            Instant.parse(isoString)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: LocalDate?): String {
        if (date == null) return ""
        return "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
    }

    fun formatShortDate(date: LocalDate?): String {
        if (date == null) return ""
        return "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
    }
}
