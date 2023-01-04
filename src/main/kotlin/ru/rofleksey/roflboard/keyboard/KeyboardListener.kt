package ru.rofleksey.roflboard.keyboard

import ru.rofleksey.roflboard.data.KeyPressed

interface KeyboardListener {
    fun afterKeyPressed(key: KeyPressed, curPressed: List<KeyPressed>)
    fun beforeKeyReleased(key: KeyPressed, curPressed: List<KeyPressed>)
}