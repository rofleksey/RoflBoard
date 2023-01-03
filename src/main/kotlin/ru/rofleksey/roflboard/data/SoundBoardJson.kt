package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class SoundBoardJson(
    val sounds: List<SoundEntryJson>,
    val mixerMain: MixerJson?,
    val mixerSecondary: MixerJson?,
)