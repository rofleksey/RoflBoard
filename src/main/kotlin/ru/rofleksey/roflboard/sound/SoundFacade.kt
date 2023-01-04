package ru.rofleksey.roflboard.sound

import javafx.beans.property.ReadOnlyFloatProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import ru.rofleksey.roflboard.controller.Controller
import ru.rofleksey.roflboard.data.SoundType
import java.io.File
import java.util.logging.Logger
import javax.sound.sampled.Mixer

class SoundFacade(
    private val soundEngine: SoundEngine,
    private val clipSetRotationFactory: ClipSetRotationFactory,
    private val soundController: Controller,
    private val sounds: ObservableList<SoundEntry>,
    private val mixers: ObservableList<Mixer.Info>,
    private val volumeMain: ReadOnlyFloatProperty,
    private val volumeSecondary: ReadOnlyFloatProperty,
) {
    companion object {
        private var log: Logger = Logger.getLogger(SoundFacade::class.java.name)
    }

    private fun unload(sound: SoundEntry) {
        soundController.unloadSound(sound)
        soundEngine.unloadClipSet(sound.id)
        log.info("Sound '${sound.name}' unloaded")
    }

    private fun load(sound: SoundEntry) {
        try {
            val clipSet = clipSetRotationFactory.load(sound.name, mixers, sound.paths.map { File(it) }, sound.type)
            clipSet.setVolume(0, volumeMain.get())
            clipSet.setVolume(1, volumeSecondary.get())
            soundEngine.loadClipSet(sound.id, clipSet)
            soundController.loadSound(sound)
            log.info("Sound '${sound.name}' loaded")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reloadAll() {
        sounds.forEach { sound ->
            unload(sound)
            load(sound)
        }
        log.info("Reloaded everything")
    }

    fun setVolume(index: Int, volume: Float) {
        soundEngine.setVolume(index, volume)
    }

    fun tryLoad(files: List<File>) {
        clipSetRotationFactory.load(files[0].name, mixers, files, SoundType.FULL).dispose()
    }

    fun init() {
        sounds.addListener(ListChangeListener { c ->
            while (c.next()) {
                for (sound in c.removed) {
                    unload(sound)
                }
                for (sound in c.addedSubList) {
                    load(sound)
                }
            }
        })
        mixers.addListener(ListChangeListener {
            reloadAll()
        })
        volumeMain.addListener { _, _, newVolume ->
            soundEngine.setVolume(0, newVolume.toFloat())
        }
        volumeSecondary.addListener { _, _, newVolume ->
            soundEngine.setVolume(1, newVolume.toFloat())
        }
    }
}