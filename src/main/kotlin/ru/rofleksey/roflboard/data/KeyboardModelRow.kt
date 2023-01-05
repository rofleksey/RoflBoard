package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyboardModelRow(val arrays: List<KeyboardModelKeyArray>)