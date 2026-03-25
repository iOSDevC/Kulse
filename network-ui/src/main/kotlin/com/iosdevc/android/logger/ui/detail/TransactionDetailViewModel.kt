package com.iosdevc.android.logger.ui.detail

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iosdevc.android.logger.Kulse
import com.iosdevc.android.logger.export.LogExporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    application: Application,
    private val transactionId: Long,
) : AndroidViewModel(application) {

    private val repository = Kulse.repository
    private val exporter = LogExporter(repository)

    val transaction = repository.getTransactionById(transactionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    fun copyAsCurl(context: Context) {
        viewModelScope.launch {
            val tx = transaction.value ?: return@launch
            val curl = LogExporter.generateCurl(tx)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("cURL", curl))
            Toast.makeText(context, "cURL copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun share(context: Context) {
        viewModelScope.launch {
            val uri = exporter.exportToFile(context)
            val shareIntent = exporter.shareIntent(uri)
            context.startActivity(Intent.createChooser(shareIntent, "Share transaction"))
        }
    }

    class Factory(
        private val application: Application,
        private val transactionId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionDetailViewModel(application, transactionId) as T
        }
    }
}
