package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable
import ru.rofleksey.roflboard.sound.SoundEntry

@Serializable
data class SoundEntryJson(val name: String, val paths: List<String>, val type: SoundType, val keys: List<Int>) {
    fun toEntry(id: Int): SoundEntry {
        return SoundEntry(id = id, name = name, paths = paths, type = type, keys = keys)
    }
}