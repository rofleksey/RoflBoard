package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.SoundType
import java.util.logging.Logger
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import kotlin.math.ln


class ClipSet constructor(private val name: String, private val clips: List<Clip>, private val type: SoundType) {
    companion object {
        var log: Logger = Logger.getLogger(SoundEngine::class.java.name)
    }

    private fun startImpl() {
        clips.forEach { clip ->
            clip.start()
        }
    }

    private fun stopImpl() {
        clips.forEach { clip ->
            clip.stop()
            clip.microsecondPosition = 0
            clip.framePosition = 0
        }
    }

    fun setVolume(index: Int, volume: Float) {
        if (index >= clips.size) {
            return
        }
        val clip = clips[index]
        val control = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val gain = ln(volume.toDouble()) / ln(10.0) * 20.0
        control.shift(control.value, gain.toFloat(), 250 * 1000)
    }

    fun onStartPressed() {
        when (type) {
            SoundType.PRESSED -> {
                startImpl()
            }

            SoundType.FULL -> {
                stopImpl()
                startImpl()
            }

            SoundType.TOGGLE -> {
                if (isRunning()) {
                    stopImpl()
                } else {
                    stopImpl()
                    startImpl()
                }
            }
        }
    }

    fun onStopPressed() {
        if (type == SoundType.PRESSED) {
            stopImpl()
        }
    }

    fun forceStop() {
        stopImpl()
    }

    fun dispose() {
        clips.forEach { clip ->
            clip.close()
        }
        log.info("ClipSet '$name' disposed")
    }

    private fun isRunning(): Boolean {
        return clips.any { clip ->
            clip.isRunning
        }
    }
}