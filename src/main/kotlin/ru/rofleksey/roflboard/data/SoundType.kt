package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable

@Serializable
enum class SoundType {
    FULL, PRESSED, TOGGLE
}