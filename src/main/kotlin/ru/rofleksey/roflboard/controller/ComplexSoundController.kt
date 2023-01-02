package ru.rofleksey.roflboard.controller

import ru.rofleksey.roflboard.sound.SoundController
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry

class ComplexSoundController(private val controllers: List<SoundController>) : SoundController {
    override fun register(engine: SoundEngine) {
        controllers.forEach { c ->
            c.register(engine)
        }
    }

    override fun unregister(engine: SoundEngine) {
        controllers.forEach { c ->
            c.unregister(engine)
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