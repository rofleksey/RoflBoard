package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.SoundType
import java.io.File
import java.util.logging.Logger
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer

class ClipSetFactory {
    companion object {
        var log: Logger = Logger.getLogger(ClipSetFactory::class.java.name)
    }

    fun load(name: String, mixerInfoList: List<Mixer.Info>, file: File, type: SoundType): ClipSet {
        val clips = mixerInfoList.map { mixerInfo ->
            AudioSystem.getClip(mixerInfo).apply {
                open(AudioSystem.getAudioInputStream(file))
            }.also {
                log.info("Opened clip for file '${file.name}' and mixer '${mixerInfo.name}'")
            }
        }
        return ClipSet(name, clips, type)
    }
}