package ru.rofleksey.roflboard.keyboard

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

data class KeyPressed(val name: String, val code: Int) {
    companion object {
        fun fromCode(code: Int): KeyPressed {
            var name = NativeKeyEvent.getKeyText(code)
            if (name.contains("keyCode")) {
                name = "($code)"
            }
            return KeyPressed(name, code)
        }
    }

    override fun toString(): String = name
}