package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class ConfigJson(
    val soundBoard: SoundBoardJson,
    val voice: VoiceJson
)