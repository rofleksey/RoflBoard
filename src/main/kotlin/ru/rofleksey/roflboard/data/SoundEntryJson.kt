package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable
import ru.rofleksey.roflboard.sound.SoundEntry

@Serializable
data class SoundEntryJson(
    val name: String,
    val paths: List<String>,
    val random: Boolean? = null,
    val type: SoundType,
    val keys: List<KeyPressed>
) {
    fun toEntry(id: Int): SoundEntry {
        return SoundEntry(id = id, name = name, paths = paths, random = random ?: true, type = type, keys = keys)
    }
}