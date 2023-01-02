package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class ConfigJson(
    val sounds: List<SoundEntryJson>,
    val mixerMain: String?,
    val mixerSecondary: String?,
    val volumeMain: Float,
    val volumeSecondary: Float
)