package ru.rofleksey.roflboard.sound

interface SoundController {
    fun register(engine: SoundEngine)
    fun unregister(engine: SoundEngine)

    fun loadSound(sound: SoundEntry)

    fun unloadSound(sound: SoundEntry)
}