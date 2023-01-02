package ru.rofleksey.roflboard.controller

import ru.rofleksey.roflboard.keyboard.GlobalEventsManager
import ru.rofleksey.roflboard.keyboard.KeyboardListener
import ru.rofleksey.roflboard.sound.SoundController
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import java.util.concurrent.ConcurrentHashMap

class KeyboardSoundController : SoundController {
    private val soundMap = ConcurrentHashMap<Set<Int>, Int>()
    private var soundEngine = SoundEngine()

    private val listener = object : KeyboardListener {
        override fun onKeysPressed(curKey: Int, keys: Set<Int>) {
            val id = soundMap[keys]
            if (id != null) {
                soundEngine.startSound(id)
            }
        }

        override fun onKeysReleased(curKey: Int, keys: Set<Int>) {
            val id = soundMap[keys]
            if (id != null) {
                soundEngine.stopSound(id)
            }
        }
    }

    override fun register(engine: SoundEngine) {
        this.soundEngine = engine
        GlobalEventsManager.INSTANCE.register(listener)
    }

    override fun unregister(engine: SoundEngine) {
        GlobalEventsManager.INSTANCE.unregister(listener)
    }

    override fun loadSound(sound: SoundEntry) {
        soundMap[sound.keys] = sound.id
    }

    override fun unloadSound(sound: SoundEntry) {
        soundMap.remove(sound.keys)
    }
}