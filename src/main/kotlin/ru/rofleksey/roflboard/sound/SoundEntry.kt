package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.SoundEntryJson
import ru.rofleksey.roflboard.data.SoundType

data class SoundEntry(val id: Int, val name: String, val paths: List<String>, val type: SoundType, val key: Int) {
    fun toJson(): SoundEntryJson {
        return SoundEntryJson(name = name, paths = paths, type = type, key = key)
    }
}