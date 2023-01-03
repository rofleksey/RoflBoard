package ru.rofleksey.roflboard.keyboard

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent

class KeyboardUtils {
    companion object {
        fun getKeyText(code: Int): String {
            val text = NativeKeyEvent.getKeyText(code)
            if (text.contains("keyCode")) {
                return "($code)"
            }
            return text
        }

        fun getDefaultKeyText(code: Int?, defaultText: String): String {
            if (code == null) {
                return defaultText
            }
            return getKeyText(code)
        }
    }
}