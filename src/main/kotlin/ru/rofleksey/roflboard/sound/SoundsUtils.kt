package ru.rofleksey.roflboard.sound

import javax.sound.sampled.AudioSystem

class SoundsUtils {
    companion object {
        fun listMixers(): List<MixerInfoCached> {
            val result = ArrayList<MixerInfoCached>()
            AudioSystem.getMixerInfo().forEach { info ->
                try {
                    result.add(MixerInfoCached(info.name, info.description, info))
                } catch (ignored: Exception) {

                }
            }
            return result
        }
    }
}