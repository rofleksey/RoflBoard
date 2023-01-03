package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.SoundType
import java.io.File
import java.util.logging.Logger
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer

class ClipSetRotationFactory {
    companion object {
        private var log: Logger = Logger.getLogger(ClipSetRotationFactory::class.java.name)
    }

    fun load(name: String, mixerInfoList: List<Mixer.Info>, files: List<File>, type: SoundType): ClipSetRotation {
        val clipSets = files.map { file ->
            val clips = mixerInfoList.map { mixerInfo ->
                AudioSystem.getClip(mixerInfo).apply {
                    open(AudioSystem.getAudioInputStream(file))
                }.also {
                    log.info("Opened clip for file '${file.name}' and mixer '${mixerInfo.name}'")
                }
            }
            ClipSet(name, clips, type)
        }
        return ClipSetRotation(name, clipSets, type)
    }
}