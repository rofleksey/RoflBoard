package ru.rofleksey.roflboard.keyboard

data class KeyPressed(val name: String, val code: Int) {
    companion object {

    }

    override fun toString(): String = name
}