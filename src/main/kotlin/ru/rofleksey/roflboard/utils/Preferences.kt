package ru.rofleksey.roflboard.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.Charset

class Preferences private constructor() {
    companion object {
        val INSTANCE = Preferences().load()
    }

    private val preferencesFile = File("roflboard_prefs.json")
    private val data = HashMap<String, String>()

    fun getString(key: String): String? {
        return data[key]
    }

    fun putString(key: String, value: String): Preferences {
        data[key] = value
        return this
    }

    fun load(): Preferences {
        data.clear()
        if (!preferencesFile.exists()) {
            return this
        }
        val str = preferencesFile.readText(charset = Charset.forName("UTF-8"))
        val newData: Map<String, String> = Json.decodeFromString(str)
        data.putAll(newData)
        return this
    }

    fun save() {
        val json = Json { prettyPrint = true }
        val str = json.encodeToString(data)
        preferencesFile.writeText(str, charset = Charset.forName("UTF-8"))
    }
}