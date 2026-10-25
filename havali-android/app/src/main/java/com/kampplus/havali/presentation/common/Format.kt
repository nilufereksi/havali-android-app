package com.kampplus.havali.presentation.common

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Ekranlarda kullanılan biçimler. Tarih adları Türkçe yazılır (Pazartesi, Ekim...). */
object Format {
    private val TURKISH: Locale = Locale.forLanguageTag("tr")
    private val TIME = DateTimeFormatter.ofPattern("HH:mm", TURKISH)
    private val HOUR = DateTimeFormatter.ofPattern("HH", TURKISH)
    private val DAY_SHORT = DateTimeFormatter.ofPattern("EEE", TURKISH)
    private val DAY_LONG = DateTimeFormatter.ofPattern("EEEE", TURKISH)
    private val DATE = DateTimeFormatter.ofPattern("d MMMM", TURKISH)

    fun time(time: LocalTime): String = time.format(TIME)

    fun hour(time: LocalTime): String = time.format(HOUR)

    fun dayShort(date: LocalDate): String = date.format(DAY_SHORT).replaceFirstChar { it.titlecase(TURKISH) }

    fun dayLong(date: LocalDate): String = date.format(DAY_LONG).replaceFirstChar { it.titlecase(TURKISH) }

    fun date(date: LocalDate): String = date.format(DATE)

    fun degrees(value: Double): String = "${value.roundToInt()}°"

    fun degrees(value: Int): String = "$value°"

    /** "6,1 km": Türkçede ondalık ayırıcı virgüldür. */
    fun km(value: Double): String = String.format(TURKISH, "%.1f km", value)
}
