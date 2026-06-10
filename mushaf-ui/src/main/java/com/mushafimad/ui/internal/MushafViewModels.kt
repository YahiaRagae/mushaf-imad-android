package com.mushafimad.ui.internal

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mushafimad.core.MushafLibrary

/**
 * Resolves a library ViewModel from the isolated Koin context, scoped to the
 * current ViewModelStoreOwner.
 *
 * Replaces koin-compose's koinViewModel(), which assumes ownership of the
 * global Koin context and breaks host apps that use Koin themselves.
 *
 * @internal Not part of the public API.
 */
@Composable
internal inline fun <reified VM : ViewModel> mushafViewModel(): VM =
    viewModel { MushafLibrary.getKoin().get(VM::class) }
