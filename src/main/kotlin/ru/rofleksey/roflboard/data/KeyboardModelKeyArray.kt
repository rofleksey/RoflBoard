package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyboardModelKeyArray(val x: Double, val y: Double, val gap: Double, val keys: List<KeyboardModelKey>)