package ru.rofleksey.roflboard.keyboard

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import ru.rofleksey.roflboard.data.KeyPressed

class KeyboardUtils {
    companion object {
        fun getKeyText(key: KeyPressed): String {
            val text = NativeKeyEvent.getKeyText(key.code)
            if (text.contains("keyCode")) {
                return "(${key.code})"
            }
            return text
        }

        fun getKeyText(keys: List<KeyPressed>): String {
            return keys.joinToString("+") { getKeyText(it) }
        }

        fun getDefaultKeyText(key: KeyPressed?, defaultText: String): String {
            if (key == null) {
                return defaultText
            }
            return getKeyText(key)
        }

        fun getDefaultKeyText(keys: List<KeyPressed>?, defaultText: String): String {
            if (keys == null) {
                return defaultText
            }
            return getKeyText(keys)
        }
    }
}