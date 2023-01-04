package ru.rofleksey.roflboard.sound.rules

interface SoundCheckRule {
    fun check(): List<SoundCheckAlert>
}