package com.kampplus.havali.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import java.time.LocalDateTime
import kotlinx.coroutines.delay

/**
 * Belirli aralıklarla güncellenen "şimdi". Geri sayım gibi saatle değişen metinler için.
 * produceState içindeki coroutine bileşen ekrandan kalkınca kendiliğinden durur.
 */
@Composable
fun rememberNow(periodMillis: Long = 30_000L): LocalDateTime = produceState(initialValue = LocalDateTime.now(), periodMillis) {
    while (true) {
        value = LocalDateTime.now()
        delay(periodMillis)
    }
}.value
