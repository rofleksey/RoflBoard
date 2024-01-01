package ru.rofleksey.roflboard.sound

import ru.rofleksey.roflboard.data.SoundType
import java.util.logging.Logger
import kotlin.random.Random


class ClipSetRotation constructor(
    private val name: String,
    private val clipSets: List<ClipSet>,
    private val type: SoundType,
    private val isRandom: Boolean,
) {
    companion object {
        private var log: Logger = Logger.getLogger(SoundEngine::class.java.name)
    }

    private var curIndex = if (isRandom) {
        (clipSets.size * Random.nextFloat()).toInt() % clipSets.size
    } else {
        0
    }

    fun setVolume(index: Int, volume: Float) {
        clipSets.forEach { clipSet ->
            clipSet.setVolume(index, volume)
        }
    }

    private fun selectNext() {
        if (clipSets.size == 1) {
            return
        }

        curIndex = if (isRandom) {
            (curIndex + 1 + ((clipSets.size - 1) * Random.nextFloat()).toInt()) % clipSets.size
        } else {
            (curIndex + 1) % clipSets.size
        }
    }

    fun onStartPressed() {
        val running = clipSets[curIndex].isRunning()
        clipSets[curIndex].onStartPressed()
        if (type == SoundType.FULL || (running && type == SoundType.TOGGLE)) {
            selectNext()
        }
    }

    fun onStopPressed() {
        if (type == SoundType.PRESSED) {
            clipSets[curIndex].onStopPressed()
            selectNext()
        }
    }

    fun forceStop() {
        clipSets.forEach { clipSet ->
            clipSet.forceStop()
        }
    }

    fun dispose() {
        clipSets.forEach { clipSet ->
            clipSet.dispose()
        }
    }
}