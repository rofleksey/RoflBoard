package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class VoiceJson(
    val enabled: Boolean,
    val mixerInput: MixerJson?,
    val key: Int?,
    val pitchFactor: Float,
    val highPassFactor: Float,
)