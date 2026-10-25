package com.kampplus.havali.presentation.common

import android.util.Log
import androidx.annotation.StringRes
import com.google.gson.JsonParseException
import com.kampplus.havali.R
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/**
 * Ekrana gelen her verinin dört hâli. Ekran `when (state)` ile hepsini ayrı çizer;
 * `sealed` olduğu için bir hâli unutursan derleyici uyarır.
 */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>

    data class Success<T>(val data: T) : LoadState<T>

    data object Empty : LoadState<Nothing>

    data class Error(val error: UiError) : LoadState<Nothing>
}

fun <T> LoadState<T>.dataOrNull(): T? = (this as? LoadState.Success)?.data

/** Hata türleri ve kullanıcıya gösterilecek Türkçe mesajları. */
enum class UiError(@StringRes val messageRes: Int) {
    NO_CONNECTION(R.string.error_no_connection),
    TIMEOUT(R.string.error_timeout),
    SERVER(R.string.error_server),
    BAD_DATA(R.string.error_bad_data),
    UNKNOWN(R.string.error_unknown)
}

/**
 * Teknik istisnayı kullanıcı diline çevirir. Sıra önemlidir: zaman aşımı da bir
 * IOException olduğu için önce o kontrol edilir. Uçak modunda UnknownHostException
 * (bir IOException) gelir ve "İnternet bağlantısı yok" gösterilir.
 */
fun Throwable.toUiError(): UiError = when (this) {
    is SocketTimeoutException -> UiError.TIMEOUT
    is IOException -> UiError.NO_CONNECTION
    is HttpException -> UiError.SERVER
    is JsonParseException, is IllegalStateException -> UiError.BAD_DATA
    else -> UiError.UNKNOWN
}

private const val TAG = "LoadState"

/**
 * Bir veri isteğini çalıştırır ve sonucu [LoadState]'e çevirir.
 * Coroutine iptali (ekran kapandı) hata sayılmaz; yukarı iletilir.
 */
suspend fun <T> loadState(isEmpty: (T) -> Boolean = { false }, block: suspend () -> T): LoadState<T> = try {
    val data = block()
    when (isEmpty(data)) {
        true -> LoadState.Empty
        false -> LoadState.Success(data)
    }
} catch (cancel: CancellationException) {
    throw cancel
} catch (error: Exception) {
    Log.w(TAG, "Veri yüklenemedi", error)
    LoadState.Error(error.toUiError())
}

/** Başarılı veriyi dönüştürür; dönüşüm null verirse sonuç [LoadState.Empty] olur. */
inline fun <T, R> LoadState<T>.map(transform: (T) -> R?): LoadState<R> = when (this) {
    is LoadState.Success -> transform(data)?.let { LoadState.Success(it) } ?: LoadState.Empty
    LoadState.Loading -> LoadState.Loading
    LoadState.Empty -> LoadState.Empty
    is LoadState.Error -> this
}
