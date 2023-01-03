package ru.rofleksey.roflboard.sound

open class SoundEngine {

    private val clipSetRotationMap = HashMap<Int, ClipSetRotation>()

    fun loadClipSet(id: Int, clipSetRotation: ClipSetRotation) {
        clipSetRotationMap[id] = clipSetRotation
    }

    fun startSound(id: Int) {
        clipSetRotationMap[id]?.onStartPressed()
    }

    fun stopSound(id: Int) {
        clipSetRotationMap[id]?.onStopPressed()
    }

    fun stopAllSounds() {
        clipSetRotationMap.values.forEach { clipRotation ->
            clipRotation.forceStop()
        }
    }

    fun setVolume(index: Int, volume: Float) {
        clipSetRotationMap.values.forEach { clipRotation ->
            clipRotation.setVolume(index, volume)
        }
    }

    fun unloadClipSet(key: Int) {
        val clipSet = clipSetRotationMap.remove(key)
        clipSet?.dispose()
    }

    fun unloadAll() {
        clipSetRotationMap.values.forEach { clipRotation ->
            clipRotation.dispose()
        }
        clipSetRotationMap.clear()
    }
}