package ru.rofleksey.roflboard.controller

import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.voice.VoiceEngine

class NetworkController : Controller {
    override fun register(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {

    }

    override fun unregister(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {

    }

    override fun loadSound(sound: SoundEntry) {

    }

    override fun unloadSound(sound: SoundEntry) {

    }
}