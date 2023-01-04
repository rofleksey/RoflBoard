package ru.rofleksey.roflboard.sound.rules

import org.jaudiotagger.audio.AudioFileIO
import ru.rofleksey.roflboard.sound.SoundEntry
import java.io.File


class DurationSoundRule(sounds: List<SoundEntry>) : SoundCheckRule {
    private val sounds = ArrayList(sounds)
    override fun check(): List<SoundCheckAlert> {
        val result = ArrayList<SoundCheckAlert>()
        sounds.forEach { sound ->
            sound.paths.forEach { path ->
                try {
                    val file = File(path)
                    val audioFile = AudioFileIO.read(file)
                    val header = audioFile.audioHeader
                    val seconds = header.trackLength
                    if (seconds < 1) {
                        result.add(
                            SoundCheckAlert(
                                "Sound '${sound.name}' is too short",
                                SoundCheckAlert.Status.WARNING
                            )
                        )
                    } else if (seconds > 60) {
                        result.add(SoundCheckAlert("Sound '${sound.name}' is too long", SoundCheckAlert.Status.WARNING))
                    }
                } catch (ignored: Throwable) {

                }
            }
        }
        return result
    }
}