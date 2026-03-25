package com.iosdevc.android.logger.internal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Headers
import okhttp3.HttpUrl

internal object Redactor {

    private const val REDACTED = "********"

    fun redactHeaders(headers: Headers, sensitiveNames: Set<String>): Headers {
        val builder = Headers.Builder()
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            val value = if (sensitiveNames.any { it.equals(name, ignoreCase = true) }) {
                REDACTED
            } else {
                headers.value(i)
            }
            builder.add(name, value)
        }
        return builder.build()
    }

    fun redactUrl(url: HttpUrl, sensitiveParams: Set<String>): String {
        if (sensitiveParams.isEmpty()) return url.toString()
        val builder = url.newBuilder()
        for (param in sensitiveParams) {
            if (url.queryParameter(param) != null) {
                builder.setQueryParameter(param, REDACTED)
            }
        }
        return builder.build().toString()
    }

    fun redactJsonFields(json: String, sensitiveFields: Set<String>): String {
        if (sensitiveFields.isEmpty()) return json
        return try {
            val element = Json.parseToJsonElement(json)
            val redacted = redactElement(element, sensitiveFields)
            Json { prettyPrint = true }.encodeToString(JsonElement.serializer(), redacted)
        } catch (_: Exception) {
            json
        }
    }

    private fun redactElement(element: JsonElement, sensitiveFields: Set<String>): JsonElement {
        return when (element) {
            is JsonObject -> {
                val entries = element.entries.associate { (key, value) ->
                    if (sensitiveFields.any { it.equals(key, ignoreCase = true) }) {
                        key to JsonPrimitive(REDACTED)
                    } else {
                        key to redactElement(value, sensitiveFields)
                    }
                }
                JsonObject(entries)
            }
            is JsonArray -> JsonArray(element.map { redactElement(it, sensitiveFields) })
            else -> element
        }
    }
}
