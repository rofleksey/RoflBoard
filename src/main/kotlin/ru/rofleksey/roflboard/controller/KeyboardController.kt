package ru.rofleksey.roflboard.controller

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.keyboard.GlobalEventsManager
import ru.rofleksey.roflboard.keyboard.KeyboardListener
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.voice.VoiceEngine
import java.util.concurrent.ConcurrentHashMap

class KeyboardController : Controller {
    private val soundMap = ConcurrentHashMap<Int, Int>()
    private lateinit var soundEngine: SoundEngine
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceKeys: ReadOnlyObjectProperty<Int?>
    private lateinit var voiceActive: SimpleBooleanProperty

    private val keyboardListener = object : KeyboardListener {
        override fun onKeyPressed(key: Int) {
            val id = soundMap[key]
            if (id != null) {
                soundEngine.startSound(id)
            }
            if (key == voiceKeys.get()) {
                voiceActive.set(true)
            }
        }

        override fun onKeyReleased(key: Int) {
            val id = soundMap[key]
            if (id != null) {
                soundEngine.stopSound(id)
            }
            if (key == voiceKeys.get()) {
                voiceActive.set(false)
            }
        }
    }

    override fun register(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {
        this.soundEngine = soundEngine
        this.voiceEngine = voiceEngine
        this.voiceKeys = appData.getVoiceKey()
        this.voiceActive = appData.getVoiceActive()
        GlobalEventsManager.INSTANCE.register(keyboardListener)
    }

    override fun unregister(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {
        GlobalEventsManager.INSTANCE.unregister(keyboardListener)
    }

    override fun loadSound(sound: SoundEntry) {
        soundMap[sound.key] = sound.id
    }

    override fun unloadSound(sound: SoundEntry) {
        soundMap.remove(sound.key)
    }
}