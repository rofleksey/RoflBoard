package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.KeyPressed
import ru.rofleksey.roflboard.data.SoundEntryJson
import ru.rofleksey.roflboard.data.SoundType

data class SoundEntry(
    val id: Int,
    val name: String,
    val paths: List<String>,
    val type: SoundType,
    val keys: List<KeyPressed>
) {
    fun toJson(): SoundEntryJson {
        return SoundEntryJson(name = name, paths = paths, type = type, keys = keys)
    }
}