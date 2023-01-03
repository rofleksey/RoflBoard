package ru.rofleksey.roflboard.voice

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import javax.sound.sampled.*

class VoicePlayer(private val format: AudioFormat, mixerInfo: Mixer.Info, bufferSize: Int) : AudioProcessor {
    private val line: SourceDataLine

    init {
        val info = DataLine.Info(SourceDataLine::class.java, format, bufferSize)
        line = AudioSystem.getMixer(mixerInfo).getLine(info) as SourceDataLine
        line.open(format, 2 * bufferSize)
        line.start()
    }

    override fun process(audioEvent: AudioEvent): Boolean {
        var byteOverlap = audioEvent.overlap * format.frameSize
        var byteStepSize = audioEvent.bufferSize * format.frameSize - byteOverlap
        if (audioEvent.timeStamp == 0.0) {
            byteOverlap = 0
            byteStepSize = audioEvent.bufferSize * format.frameSize
        }
        val bytesWritten = line.write(audioEvent.byteBuffer, byteOverlap, byteStepSize)
        if (bytesWritten != byteStepSize) {
            System.err.println(
                String.format(
                    "Expected to write %d bytes but only wrote %d bytes",
                    byteStepSize,
                    bytesWritten
                )
            )
        }
        return true
    }

    override fun processingFinished() {
        line.stop()
        line.close()
    }
}