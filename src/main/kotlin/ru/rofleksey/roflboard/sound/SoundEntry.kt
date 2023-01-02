package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.SoundEntryJson
import ru.rofleksey.roflboard.data.SoundType

data class SoundEntry(val id: Int, val name: String, val path: String, val type: SoundType, val keys: Set<Int>) {
    fun toJson(): SoundEntryJson {
        return SoundEntryJson(name = name, path = path, type = type, keys = keys)
    }
}