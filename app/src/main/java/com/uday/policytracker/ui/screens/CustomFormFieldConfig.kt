package com.uday.policytracker.ui.screens

import android.content.Context
import com.uday.policytracker.data.model.PolicyCategory
import org.json.JSONArray
import org.json.JSONObject

enum class CustomFieldType {
    TEXT,
    NUMBER,
    DATE,
    DROPDOWN
}

data class CustomFieldDefinition(
    val id: String,
    val label: String,
    val type: CustomFieldType,
    val options: List<String> = emptyList()
)

private const val PREFS_NAME = "policy_form_custom_fields"

fun loadCustomFieldDefinitions(context: Context, category: PolicyCategory): List<CustomFieldDefinition> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(category.name, null).orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val type = runCatching { CustomFieldType.valueOf(o.optString("type", "TEXT")) }
                    .getOrDefault(CustomFieldType.TEXT)
                val options = buildList {
                    val optionsArr = o.optJSONArray("options") ?: JSONArray()
                    for (j in 0 until optionsArr.length()) {
                        val value = optionsArr.optString(j).trim()
                        if (value.isNotBlank()) add(value)
                    }
                }
                val id = o.optString("id").ifBlank { "field_${System.currentTimeMillis()}_$i" }
                val label = o.optString("label").trim()
                if (label.isNotBlank()) {
                    add(
                        CustomFieldDefinition(
                            id = id,
                            label = label,
                            type = type,
                            options = if (type == CustomFieldType.DROPDOWN) options else emptyList()
                        )
                    )
                }
            }
        }
    }.getOrDefault(emptyList())
}

fun saveCustomFieldDefinitions(context: Context, category: PolicyCategory, fields: List<CustomFieldDefinition>) {
    val arr = JSONArray().apply {
        fields.forEach { field ->
            put(
                JSONObject().apply {
                    put("id", field.id)
                    put("label", field.label)
                    put("type", field.type.name)
                    put("options", JSONArray().apply { field.options.forEach { put(it) } })
                }
            )
        }
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(category.name, arr.toString())
        .apply()
}

fun parseCustomFieldValuesJson(raw: String): MutableMap<String, String> {
    if (raw.isBlank()) return mutableMapOf()
    return runCatching {
        val o = JSONObject(raw)
        val keys = o.keys()
        val result = mutableMapOf<String, String>()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = o.optString(key)
        }
        result
    }.getOrDefault(mutableMapOf())
}

fun toCustomFieldValuesJson(values: Map<String, String>): String {
    val obj = JSONObject()
    values.forEach { (key, value) ->
        if (value.isNotBlank()) obj.put(key, value)
    }
    return obj.toString()
}
