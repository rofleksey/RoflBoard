package ru.rofleksey.roflboard.controller

import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.voice.VoiceEngine

class ComplexController(private val controllers: List<Controller>) : Controller {
    override fun register(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {
        controllers.forEach { c ->
            c.register(soundEngine, voiceEngine, appData)
        }
    }

    override fun unregister(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {
        controllers.forEach { c ->
            c.unregister(soundEngine, voiceEngine, appData)
        }
    }

    override fun loadSound(sound: SoundEntry) {
        controllers.forEach { c ->
            c.loadSound(sound)
        }
    }

    override fun unloadSound(sound: SoundEntry) {
        controllers.forEach { c ->
            c.unloadSound(sound)
        }
    }
}