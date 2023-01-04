package ru.rofleksey.roflboard.keyboard

interface KeyboardListener {
    fun afterKeyPressed(key: Int, curPressed: List<Int>)
    fun beforeKeyReleased(key: Int, curPressed: List<Int>)
}