package ru.rofleksey.roflboard.controller

import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.voice.VoiceEngine

interface Controller {
    fun register(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData)
    fun unregister(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData)

    fun loadSound(sound: SoundEntry)

    fun unloadSound(sound: SoundEntry)
}