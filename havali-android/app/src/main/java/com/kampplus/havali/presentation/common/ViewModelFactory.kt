package com.kampplus.havali.presentation.common

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.kampplus.havali.HavaliApplication
import com.kampplus.havali.di.AppContainer

/** ViewModel fabrikalarının bağımlılık kabına ulaşması için kısa yol. */
fun CreationExtras.appContainer(): AppContainer = (checkNotNull(this[APPLICATION_KEY]) as HavaliApplication).container
