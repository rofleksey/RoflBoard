package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class MixerJson(val name: String, val volume: Float)