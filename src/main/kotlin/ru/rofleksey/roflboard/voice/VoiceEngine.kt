package ru.rofleksey.roflboard.voice

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.GainProcessor
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd
import be.tarsos.dsp.filters.HighPass
import be.tarsos.dsp.io.jvm.JVMAudioInputStream
import be.tarsos.dsp.resample.RateTransposer
import com.adonax.audiocue.AudioCue
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.value.ChangeListener
import ru.rofleksey.roflboard.sound.SoundEngine
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger
import javax.sound.sampled.*

class VoiceEngine(
    private val voiceFeatureEnabled: SimpleBooleanProperty,
    private val voiceActive: SimpleBooleanProperty,
    private val voiceMixerParams: ReadOnlyObjectProperty<VoiceMixerParams>,
    private val maxGain: SimpleFloatProperty,
    private val pitchFactor: SimpleFloatProperty,
    private val highPassFactor: SimpleFloatProperty,
    private val voiceMusic: ReadOnlyStringProperty,
    private val voiceMusicVolume: SimpleFloatProperty
) {
    companion object {
        private const val SAMPLE_RATE = 48000.0
        private const val SAMPLE_SIZE_IN_BITS = 16
        private const val CHANNELS = 1
        private val AUDIO_FORMAT = AudioFormat(SAMPLE_RATE.toFloat(), SAMPLE_SIZE_IN_BITS, CHANNELS, true, true)
        private var log: Logger = Logger.getLogger(SoundEngine::class.java.name)
    }

    private val executor = Executors.newCachedThreadPool()

    private var dispatcher: AudioDispatcher? = null
    private var wsola: WaveformSimilarityBasedOverlapAdd? = null
    private var gainProcessor: GainProcessor? = null
    private var rateTransposer: RateTransposer? = null
    private var highPass: HighPass? = null
    private var line: TargetDataLine? = null
    private var voiceMusicPlayer: AudioCue? = null
    private var voiceMusicInstanceId = -1
    private var task: Future<*>? = null

    private val voiceFeatureEnabledListener = ChangeListener<Boolean> { _, _, curEnabled ->
        restart()
    }

    private val mixerParamsListener = ChangeListener<VoiceMixerParams> { _, _, _ ->
        restart()
    }

    private val voiceOnListener = ChangeListener<Boolean> { _, _, _ ->
        applyGain()
    }

    private val maxGainListener = ChangeListener<Number> { _, _, _ ->
        applyGain()
    }

    private val voiceMusicVolumeListener = ChangeListener<Number> { _, _, _ ->
        applyGain()
    }

    private val pitchFactorListener = ChangeListener<Number> { _, _, factor ->
        wsola?.setParameters(
            WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(
                factor.toDouble(),
                SAMPLE_RATE
            )
        )
        rateTransposer?.setFactor(factor.toDouble())
    }

    private val highPassFactorListener = ChangeListener<Number> { _, _, factor ->
        highPass?.setFrequency(factor.toFloat())
    }

    private val voiceMusicListener = ChangeListener<String> { _, _, path ->
        restart()
    }

    fun init() {
        voiceFeatureEnabled.addListener(voiceFeatureEnabledListener)
        voiceMixerParams.addListener(mixerParamsListener)
        voiceActive.addListener(voiceOnListener)
        maxGain.addListener(maxGainListener)
        pitchFactor.addListener(pitchFactorListener)
        highPassFactor.addListener(highPassFactorListener)
        voiceMusic.addListener(voiceMusicListener)
        voiceMusicVolume.addListener(voiceMusicVolumeListener)
        restart()
    }

    private fun restart() {
        stop()
        start()
    }

    fun dispose() {
        voiceFeatureEnabled.removeListener(voiceFeatureEnabledListener)
        voiceMixerParams.removeListener(mixerParamsListener)
        voiceActive.removeListener(voiceOnListener)
        maxGain.removeListener(maxGainListener)
        pitchFactor.removeListener(pitchFactorListener)
        highPassFactor.removeListener(highPassFactorListener)
        voiceMusic.removeListener(voiceMusicListener)
        voiceMusicVolume.removeListener(voiceMusicVolumeListener)
        stop()
    }

    private fun applyGain() {
        if (voiceActive.get()) {
            log.info("Voice ON")
            gainProcessor?.setGain(maxGain.get().toDouble())
            voiceMusicPlayer?.setVolume(voiceMusicInstanceId, voiceMusicVolume.get().toDouble())
        } else {
            log.info("Voice OFF")
            gainProcessor?.setGain(0.0)
            voiceMusicPlayer?.setVolume(voiceMusicInstanceId, 0.0)
        }
    }

    private fun start() {
        if (!voiceFeatureEnabled.get() || !voiceMixerParams.get().isProper()) {
            return
        }
        wsola = WaveformSimilarityBasedOverlapAdd(
            WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(
                pitchFactor.get().toDouble(),
                SAMPLE_RATE
            )
        )
        dispatcher = getAudioDispatcher(voiceMixerParams.get().mixerInput!!, wsola!!.inputBufferSize, wsola!!.overlap)
        wsola!!.setDispatcher(dispatcher)
        dispatcher!!.addAudioProcessor(wsola!!)

        rateTransposer = RateTransposer(pitchFactor.get().toDouble())
        gainProcessor = GainProcessor(0.0)
        highPass = HighPass(5000.toFloat(), SAMPLE_RATE.toFloat())

        dispatcher!!.addAudioProcessor(rateTransposer)
        dispatcher!!.addAudioProcessor(highPass)
        dispatcher!!.addAudioProcessor(gainProcessor)
        val player = VoicePlayer(AUDIO_FORMAT, voiceMixerParams.get().mixerOutput!!, wsola!!.inputBufferSize)
        dispatcher!!.addAudioProcessor(player)

        task = executor.submit(dispatcher!!)

        startMusic()

        log.info("Voice engine started with mixers '${voiceMixerParams.get().mixerInput!!.name}' -> '${voiceMixerParams.get().mixerOutput!!.name}'")
    }

    private fun startMusic() {
        val musicPath = voiceMusic.get() ?: return
        val musicFile = File(musicPath)
        if (!musicFile.exists()) {
            return
        }
        try {
            val mixer = AudioSystem.getMixer(voiceMixerParams.get().mixerOutput)
            voiceMusicPlayer = AudioCue.makeStereoCue(musicFile.toURI().toURL(), 1)
            voiceMusicPlayer!!.open(mixer, 1024, Thread.MAX_PRIORITY)
            voiceMusicInstanceId = voiceMusicPlayer!!.play()
            voiceMusicPlayer!!.setVolume(voiceMusicInstanceId, 0.0)
            voiceMusicPlayer!!.setLooping(voiceMusicInstanceId, -1)
        } catch (e: Throwable) {
            log.warning("Failed to load bg music: ${e.message}")
        }
    }

    private fun stop() {
        task?.cancel(true)
        dispatcher?.stop()
        line?.stop()
        line?.close()
        try {
            voiceMusicPlayer?.close()
        } catch (ignored: Throwable) {

        }
        log.info("Voice engine stopped")
    }

    private fun getAudioDispatcher(mixerInfo: Mixer.Info, audioBufferSize: Int, bufferOverlap: Int): AudioDispatcher {
        val info = DataLine.Info(TargetDataLine::class.java, AUDIO_FORMAT, audioBufferSize)
        val line = AudioSystem.getMixer(mixerInfo).getLine(info) as TargetDataLine
        line.open(AUDIO_FORMAT, audioBufferSize)
        line.start()
        val stream = AudioInputStream(line)
        val audioStream = JVMAudioInputStream(stream)
        return AudioDispatcher(audioStream, audioBufferSize, bufferOverlap)
    }
}