package ru.rofleksey.roflboard.sound.rules

import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.utils.SilenceDetector
import java.io.DataInputStream
import java.io.File
import javax.sound.sampled.AudioSystem


class SilenceSoundRule(sounds: List<SoundEntry>) : SoundCheckRule {
    private val sounds = ArrayList(sounds)
    override fun check(): List<SoundCheckAlert> {
        val result = ArrayList<SoundCheckAlert>()
        sounds.forEach { sound ->
            sound.paths.forEach { path ->
                try {
                    val file = File(path)
                    AudioSystem.getAudioInputStream(file).use { inputStream ->
                        val format = inputStream.format
                        val audioData = ByteArray((inputStream.frameLength * format.frameSize).toInt())
                        DataInputStream(inputStream).use { dataInputStream ->
                            dataInputStream.readFully(audioData)
                        }
                        val silenceOffset = SilenceDetector.detectSilence(audioData, format.isBigEndian)
                        val startSilenceSeconds = silenceOffset.start / (format.frameSize * format.frameRate)
                        val endSilenceSeconds = silenceOffset.end / (format.frameSize * format.frameRate)
                        if (startSilenceSeconds > 0.1f) {
                            result.add(
                                SoundCheckAlert(
                                    "Sound '${sound.name}' has large silence period at the start",
                                    SoundCheckAlert.Status.WARNING
                                )
                            )
                            println("${sound.name} $startSilenceSeconds $endSilenceSeconds")
                        }
                        if (endSilenceSeconds > 1f) {
                            result.add(
                                SoundCheckAlert(
                                    "Sound '${sound.name}' has large silence period at the end",
                                    SoundCheckAlert.Status.WARNING
                                )
                            )
                            println("${sound.name} $startSilenceSeconds $endSilenceSeconds")
                        }
                    }
                } catch (ignored: Throwable) {

                }
            }
        }
        return result
    }
}