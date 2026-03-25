package com.iosdevc.android.logger.ui.detail

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iosdevc.android.logger.Kulse
import com.iosdevc.android.logger.export.LogExporter
import com.iosdevc.android.logger.model.HttpTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ShareFormat(val label: String) {
    PLAIN_TEXT("Share as Plain Text"),
    MARKDOWN("Share as Markdown"),
    HTML("Share as HTML"),
    CURL("Share as cURL"),
}

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

    fun shareAs(context: Context, format: ShareFormat) {
        val tx = transaction.value ?: return
        viewModelScope.launch {
            when (format) {
                ShareFormat.CURL -> {
                    val curl = LogExporter.generateCurl(tx)
                    copyToClipboard(context, "cURL", curl)
                }
                ShareFormat.PLAIN_TEXT -> {
                    val text = LogExporter.formatAsPlainText(tx)
                    shareText(context, text, "text/plain", "txt")
                }
                ShareFormat.MARKDOWN -> {
                    val md = LogExporter.formatAsMarkdown(tx)
                    shareText(context, md, "text/markdown", "md")
                }
                ShareFormat.HTML -> {
                    val html = LogExporter.formatAsHtml(tx)
                    shareText(context, html, "text/html", "html")
                }
            }
        }
    }

    private fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(context: Context, content: String, mimeType: String, extension: String) {
        val dir = File(context.cacheDir, "kulse")
        dir.mkdirs()
        val file = File(dir, "request_${transactionId}.$extension")
        file.writeText(content)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.kulse.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Request Log"))
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
