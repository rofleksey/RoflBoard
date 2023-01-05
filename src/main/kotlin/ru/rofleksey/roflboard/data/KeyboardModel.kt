package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyboardModel(val name: String, val rows: List<KeyboardModelRow>, val ratio: Double)