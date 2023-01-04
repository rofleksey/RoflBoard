package ru.rofleksey.roflboard.sound

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

class SoundUtils {
    companion object {
        fun listInputMixers(): List<MixerInfoCached> {
            return AudioSystem.getMixerInfo().filter { info ->
                val mixer = AudioSystem.getMixer(info)
                val lineInfoList = mixer.targetLineInfo
                lineInfoList.any { it.lineClass.equals(TargetDataLine::class.java) }
            }.flatMap { info ->
                try {
                    listOf(MixerInfoCached(info.name, info.description, info))
                } catch (ignored: Exception) {
                    listOf()
                }
            }
        }

        fun listOutputMixers(): List<MixerInfoCached> {
            return AudioSystem.getMixerInfo().filter { info ->
                val mixer = AudioSystem.getMixer(info)
                val lineInfoList = mixer.sourceLineInfo
                lineInfoList.any { it.lineClass.equals(SourceDataLine::class.java) }
            }.flatMap { info ->
                try {
                    listOf(MixerInfoCached(info.name, info.description, info))
                } catch (ignored: Exception) {
                    listOf()
                }
            }
        }
    }
}