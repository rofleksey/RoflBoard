package ru.rofleksey.roflboard.data

import kotlinx.serialization.Serializable
import ru.rofleksey.roflboard.keyboard.KeyboardUtils

@Serializable
data class KeyPressed(val code: Int, val location: Int) {
    override fun toString(): String {
        return KeyboardUtils.getKeyText(this)
    }
}