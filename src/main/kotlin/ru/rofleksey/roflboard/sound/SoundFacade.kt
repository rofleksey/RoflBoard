package ru.rofleksey.roflboard.sound

import javafx.beans.property.ReadOnlyFloatProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import ru.rofleksey.roflboard.controller.Controller
import ru.rofleksey.roflboard.data.SoundType
import ru.rofleksey.roflboard.sound.rules.SoundCheckAlert
import java.io.File
import java.io.FileNotFoundException
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
            val clipSet = clipSetRotationFactory.load(
                sound.name, mixers, sound.paths.map { File(it) },
                sound.type, sound.random
            )
            clipSet.setVolume(0, volumeMain.get())
            clipSet.setVolume(1, volumeSecondary.get())
            soundEngine.loadClipSet(sound.id, clipSet)
            soundController.loadSound(sound)
            log.info("Sound '${sound.name}' loaded")
        } catch (e: FileNotFoundException) {
            SoundCheckService.INSTANCE.addManual(
                SoundCheckAlert(
                    "File not found for sound '${sound.name}', paths: ${
                        sound.paths.joinToString(
                            ";"
                        ) { it }
                    }", SoundCheckAlert.Status.ERROR
                )
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            SoundCheckService.INSTANCE.addManual(
                SoundCheckAlert(
                    "Failed to load sound '${sound.name}': $e",
                    SoundCheckAlert.Status.ERROR
                )
            )
        }
    }

    fun reloadAll() {
        SoundCheckService.INSTANCE.clear()
        sounds.forEach { sound ->
            unload(sound)
            load(sound)
        }
        SoundCheckService.INSTANCE.checkHeavy(sounds)
        log.info("Reloaded everything")
    }

    fun tryLoad(files: List<File>) {
        clipSetRotationFactory.load(
            files[0].name,
            mixers,
            files,
            SoundType.FULL,
            true
        ).dispose()
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