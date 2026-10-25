package com.kampplus.havali.presentation

import com.google.gson.JsonSyntaxException
import com.kampplus.havali.presentation.common.UiError
import com.kampplus.havali.presentation.common.toUiError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

/** Teknik hataların kullanıcı diline çevrilmesi. */
class ErrorMappingTest {

    @Test
    fun `ucak modundaki DNS hatasi baglanti yok demektir`() {
        assertEquals(UiError.NO_CONNECTION, UnknownHostException("api.open-meteo.com").toUiError())
        assertEquals(UiError.NO_CONNECTION, IOException().toUiError())
    }

    @Test
    fun `zaman asimi baglanti yoktan once yakalanir`() {
        assertEquals(UiError.TIMEOUT, SocketTimeoutException().toUiError())
    }

    @Test
    fun `bozuk JSON veri hatasidir`() {
        assertEquals(UiError.BAD_DATA, JsonSyntaxException("beklenmeyen karakter").toUiError())
        assertEquals(UiError.UNKNOWN, IllegalArgumentException().toUiError())
    }
}
