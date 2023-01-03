package ru.rofleksey.roflboard.keyboard

interface KeyboardListener {
    fun onKeyPressed(key: Int)
    fun onKeyReleased(key: Int)
}