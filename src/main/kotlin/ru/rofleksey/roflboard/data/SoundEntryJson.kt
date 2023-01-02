package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable
import ru.rofleksey.roflboard.sound.SoundEntry

@Serializable
data class SoundEntryJson(val name: String, val path: String, val type: SoundType, val keys: Set<Int>) {
    fun toEntry(id: Int): SoundEntry {
        return SoundEntry(id = id, name = name, path = path, type = type, keys = keys)
    }
}