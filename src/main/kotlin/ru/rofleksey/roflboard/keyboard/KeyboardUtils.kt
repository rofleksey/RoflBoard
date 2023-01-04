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

        fun getKeyText(codes: List<Int>): String {
            return codes.joinToString("+") { getKeyText(it) }
        }

        fun getDefaultKeyText(code: Int?, defaultText: String): String {
            if (code == null) {
                return defaultText
            }
            return getKeyText(code)
        }

        fun getDefaultKeyText(codes: List<Int>?, defaultText: String): String {
            if (codes == null) {
                return defaultText
            }
            return getKeyText(codes)
        }

//        fun isCombinationPressed(combination: List<Int>, curPressed: List<Int>): Boolean {
//            if (combination.isEmpty()) {
//                return false
//            }
//            var pointer = 0
//            curPressed.forEach { c ->
//                if (c == combination[pointer]) {
//                    if (++pointer == combination.size) {
//                        return true
//                    }
//                }
//            }
//            return false
//        }
    }
}