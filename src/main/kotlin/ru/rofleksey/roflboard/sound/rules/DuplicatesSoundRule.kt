package ru.rofleksey.roflboard.sound.rules

import ru.rofleksey.roflboard.data.KeyPressed
import ru.rofleksey.roflboard.keyboard.KeyboardUtils
import ru.rofleksey.roflboard.sound.SoundEntry

class DuplicatesSoundRule(sounds: List<SoundEntry>) : SoundCheckRule {
    private val sounds = ArrayList(sounds)
    override fun check(): List<SoundCheckAlert> {
        val result = ArrayList<SoundCheckAlert>()
        val nameMap = HashMap<String, MutableList<SoundEntry>>()
        val keysMap = HashMap<List<KeyPressed>, MutableList<SoundEntry>>()
        val pathsMap = HashMap<String, MutableList<SoundEntry>>()
        sounds.forEach { sound ->
            nameMap.compute(sound.name) { _, list ->
                list?.apply { add(sound) } ?: mutableListOf(sound)
            }
            keysMap.compute(sound.keys) { _, list ->
                list?.apply { add(sound) } ?: mutableListOf(sound)
            }
            sound.paths.forEach { path ->
                pathsMap.compute(path) { _, list ->
                    list?.apply { add(sound) } ?: mutableListOf(sound)
                }
            }
        }
        nameMap.filterValues { it.size > 1 }.forEach { entry ->
            result.add(
                SoundCheckAlert(
                    "Duplicate name '${entry.key}'",
                    SoundCheckAlert.Status.WARNING
                )
            )
        }
        keysMap.filterValues { it.size > 1 }.forEach { entry ->
            result.add(
                SoundCheckAlert(
                    "Multiple bindings found for keys ${KeyboardUtils.getKeyText(entry.key)}: ${
                        entry.value.joinToString(
                            ", "
                        ) { "'${it.name}'" }
                    }", SoundCheckAlert.Status.WARNING
                )
            )
        }
        pathsMap.filterValues { it.size > 1 }.forEach { entry ->
            result.add(
                SoundCheckAlert(
                    "Multiple sounds found for path ${entry.key}: ${
                        entry.value.joinToString(
                            ", "
                        ) { "'${it.name}'" }
                    }", SoundCheckAlert.Status.WARNING
                )
            )
        }
        return result
    }
}