package com.hasabati.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hasabati.app.HasabatiApplication
import com.hasabati.app.data.repository.HasabatiRepository

class SimpleViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}

@Composable
fun repository(): HasabatiRepository {
    val app = LocalContext.current.applicationContext as HasabatiApplication
    return app.repository
}

@Composable
fun backupManager(): com.hasabati.app.data.repository.BackupManager {
    val app = LocalContext.current.applicationContext as HasabatiApplication
    return app.backupManager
}

@Composable
inline fun <reified VM : ViewModel> hasabatiViewModel(noinline creator: (HasabatiRepository) -> VM): VM {
    val repo = repository()
    return viewModel(factory = SimpleViewModelFactory { creator(repo) })
}
