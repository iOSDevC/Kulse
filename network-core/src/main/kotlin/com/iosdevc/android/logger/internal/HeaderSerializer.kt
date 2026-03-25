package com.iosdevc.android.logger.internal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Headers

object HeaderSerializer {

    fun serialize(headers: Headers): String {
        val list = (0 until headers.size).map { i ->
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(headers.name(i)),
                    "value" to JsonPrimitive(headers.value(i)),
                )
            )
        }
        return JsonArray(list).toString()
    }

    fun deserialize(json: String?): List<Pair<String, String>> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            Json.parseToJsonElement(json).jsonArray.map { element ->
                val obj = element.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: ""
                val value = obj["value"]?.jsonPrimitive?.content ?: ""
                name to value
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
