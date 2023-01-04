package ru.rofleksey.roflboard.sound.rules

import ru.rofleksey.roflboard.sound.SoundEntry

class TestRule(sounds: List<SoundEntry>) : SoundCheckRule {
    private val sounds = ArrayList(sounds)
    override fun check(): List<SoundCheckAlert> {
        return listOf(SoundCheckAlert("test", SoundCheckAlert.Status.WARNING))
    }
}