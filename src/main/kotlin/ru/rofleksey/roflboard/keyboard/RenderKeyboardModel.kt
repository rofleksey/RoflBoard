package ru.rofleksey.roflboard.keyboard

import ru.rofleksey.roflboard.data.KeyPressed
import ru.rofleksey.roflboard.data.KeyboardModel
import ru.rofleksey.roflboard.sound.SoundEntry
import kotlin.math.max
import kotlin.math.min

data class RenderKeyboardModel(val rows: List<RenderKeyboardRow>, val ratio: Double) {
    companion object {
        fun fromModelAndSounds(model: KeyboardModel, sounds: List<SoundEntry>): RenderKeyboardModel {
            val soundMap = HashMap<KeyPressed, MutableList<SoundEntry>>()
            sounds.forEach { sound ->
                soundMap.compute(sound.keys.last()) { _, list ->
                    list?.apply { add(sound) } ?: mutableListOf(sound)
                }
            }
            val newRows = model.rows.map { row ->
                val newArrays = row.arrays.map { array ->
                    val keys = array.keys.map { key ->
                        RenderKeyboardKey(
                            key.name,
                            soundMap[key.key]?.sortedBy { -it.keys.size } ?: listOf(),
                            key.width,
                            key.height)
                    }
                    RenderKeyboardArray(array.x, array.y, array.gap, keys)
                }
                RenderKeyboardRow(newArrays)
            }
            return RenderKeyboardModel(newRows, model.ratio)
        }
    }

    var width = 0.0
    var height = 0.0

    fun calcLayoutDimensions() {
        var minStartX = Double.MAX_VALUE
        var minStartY = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var maxY = Double.MIN_VALUE
        rows.forEach { row ->
            row.arrays.forEach { array ->
                minStartX = min(array.x, minStartX)
                minStartY = min(array.y, minStartY)
                var curXCm = array.x
                array.keys.forEach { key ->
                    curXCm += key.width + array.gap
                    maxY = max(array.y + key.height, maxY)
                }
                maxX = max(curXCm - array.gap, maxX)
            }
        }
        width = (maxX + minStartX) * ratio
        height = (maxY + minStartY) * ratio
    }
}