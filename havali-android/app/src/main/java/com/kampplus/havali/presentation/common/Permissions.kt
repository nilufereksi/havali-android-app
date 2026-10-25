package com.kampplus.havali.presentation.common

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.kampplus.havali.notification.NotificationHelper

/**
 * Çalışma anı izni: Android 13+ bildirim göstermek için kullanıcıdan izin ister.
 *
 * Döndürülen fonksiyon çağrılınca izin zaten varsa (veya eski Android'deysek)
 * hemen `onResult(true)` gelir; yoksa sistem izin penceresi açılır ve cevap
 * `onResult`'a iletilir. Kullanıcı reddederse uygulama çökmez, yalnızca bildirim gösterilmez.
 */
@Composable
fun rememberNotificationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val latestOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { latestOnResult(it) }
    return remember(launcher, context) {
        {
            when (NotificationHelper.canNotify(context)) {
                true -> latestOnResult(true)
                false -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
