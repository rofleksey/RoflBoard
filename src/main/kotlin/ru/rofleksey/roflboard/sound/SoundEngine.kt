package ru.rofleksey.roflboard.sound

open class SoundEngine {

    private val clipSetMap = HashMap<Int, ClipSet>()

    fun loadClipSet(id: Int, clipSet: ClipSet) {
        clipSetMap[id] = clipSet
    }

    fun startSound(id: Int) {
        clipSetMap[id]?.onStartPressed()
    }

    fun stopSound(id: Int) {
        clipSetMap[id]?.onStopPressed()
    }

    fun stopAllSounds() {
        clipSetMap.values.forEach { sound ->
            sound.forceStop()
        }
    }

    fun setVolume(index: Int, volume: Float) {
        clipSetMap.values.forEach { clipSet ->
            clipSet.setVolume(index, volume)
        }
    }

    fun unloadClipSet(key: Int) {
        val clipSet = clipSetMap.remove(key)
        clipSet?.dispose()
    }

    fun unloadAll() {
        clipSetMap.values.forEach { clipSet ->
            clipSet.dispose()
        }
        clipSetMap.clear()
    }
}