package com.iosdevc.android.logger.ui.console

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iosdevc.android.logger.Kulse
import com.iosdevc.android.logger.export.LogExporter
import com.iosdevc.android.logger.ui.filter.FilterState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConsoleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Kulse.repository
    private val exporter = LogExporter(repository)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Default: filter by current session
    private val _filterState = MutableStateFlow(
        FilterState(sessionId = Kulse.currentSessionId)
    )
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    @OptIn(FlowPreview::class)
    private val debouncedQuery = _searchQuery.debounce(300L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions = combine(debouncedQuery, _filterState) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        repository.getTransactionsFiltered(
            sessionId = filter.sessionId,
            method = filter.method,
            host = filter.host,
            minStatusCode = filter.minStatusCode,
            maxStatusCode = filter.maxStatusCode,
            searchQuery = query.ifBlank { null },
            afterDate = filter.afterDate,
            beforeDate = filter.beforeDate,
            limit = 500,
            offset = 0,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val hosts = repository.getAllHosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: FilterState) {
        _filterState.value = filter
    }

    fun setSessionId(sessionId: String?) {
        _filterState.value = _filterState.value.copy(sessionId = sessionId)
    }

    fun export(context: Context) {
        viewModelScope.launch {
            val uri = exporter.exportToFile(context)
            val shareIntent = exporter.shareIntent(uri)
            context.startActivity(Intent.createChooser(shareIntent, "Share network logs"))
        }
    }
}
