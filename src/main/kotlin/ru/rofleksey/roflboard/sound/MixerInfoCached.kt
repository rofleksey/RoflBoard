package ru.rofleksey.roflboard.sound

import javax.sound.sampled.Mixer

data class MixerInfoCached(val name: String, val description: String, val info: Mixer.Info)