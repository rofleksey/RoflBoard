package ru.rofleksey.roflboard.voice

import javax.sound.sampled.Mixer

data class VoiceMixerParams(val mixerInput: Mixer.Info?, val mixerOutput: Mixer.Info?) {
    fun isProper() = mixerInput != null && mixerOutput != null
}