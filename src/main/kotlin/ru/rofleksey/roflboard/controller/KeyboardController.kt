package ru.rofleksey.roflboard.controller

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.data.KeyPressed
import ru.rofleksey.roflboard.keyboard.GlobalEventsManager
import ru.rofleksey.roflboard.keyboard.KeyboardListener
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.voice.VoiceEngine

class KeyboardController : Controller {
    companion object {
        private data class SoundLink(val soundId: Int, val keys: List<KeyPressed>, var isPressed: Boolean)
    }

    private val soundMap = ArrayList<SoundLink>()
    private lateinit var soundEngine: SoundEngine
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceKeys: ReadOnlyObjectProperty<KeyPressed?>
    private lateinit var voiceActive: SimpleBooleanProperty

    private val keyboardListener = object : KeyboardListener {
        override fun afterKeyPressed(key: KeyPressed, curPressed: List<KeyPressed>) {
            var maxLinks: MutableList<SoundLink> = mutableListOf()
            var maxScore = 0
            soundMap.forEach { link ->
                if (curPressed.containsAll(link.keys)) {
                    if (link.keys.size > maxScore) {
                        maxLinks = mutableListOf(link)
                        maxScore = link.keys.size
                    } else if (link.keys.size == maxScore) {
                        maxLinks.add(link)
                    }
                }
            }
            val bestLinks = maxLinks
            bestLinks.forEach { link ->
                if (!link.isPressed) {
                    link.isPressed = true
                    soundEngine.startSound(link.soundId)
                }
            }
            if (key == voiceKeys.get()) {
                voiceActive.set(true)
            }
        }

        override fun beforeKeyReleased(key: KeyPressed, curPressed: List<KeyPressed>) {
            soundMap.forEach { link ->
                if (link.isPressed) {
                    if (link.keys.contains(key)) {
                        link.isPressed = false
                        soundEngine.stopSound(link.soundId)
                    }
                }
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
        soundMap.add(SoundLink(sound.id, sound.keys, false))
    }

    override fun unloadSound(sound: SoundEntry) {
        soundMap.removeIf { it.soundId == sound.id }
    }
}