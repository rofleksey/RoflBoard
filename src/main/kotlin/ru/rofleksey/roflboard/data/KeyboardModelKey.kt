package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyboardModelKey(val name: String, val width: Double, val height: Double, val key: KeyPressed)