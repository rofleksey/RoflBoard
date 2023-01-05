package ru.rofleksey.roflboard.keyboard

import ru.rofleksey.roflboard.sound.SoundEntry

data class RenderKeyboardKey(val name: String, val sounds: List<SoundEntry>, val width: Double, val height: Double)