package ru.rofleksey.roflboard.keyboard

interface KeyboardListener {
    fun onKeysPressed(curKey: Int, keys: Set<Int>)
    fun onKeysReleased(curKey: Int, keys: Set<Int>)
}