package ru.rofleksey.roflboard.data

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.rofleksey.roflboard.keyboard.KeyboardUtils
import ru.rofleksey.roflboard.sound.MixerInfoCached
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.ui.SoundView
import ru.rofleksey.roflboard.voice.VoiceMixerParams
import java.io.File
import java.nio.charset.Charset
import javax.sound.sampled.Mixer

class AppData {
    private var soundIdCounter = 0

    private val configName = ReadOnlyStringWrapper(null)
    private var configFile: File? = null

    private val mixerMainChange = ReadOnlyStringWrapper(null)
    private val mixerSecondaryChange = ReadOnlyStringWrapper(null)
    private val mixerVoiceChange = ReadOnlyStringWrapper(null)
    private var mixerMainName: String? = null
    private var mixerSecondaryName: String? = null
    private var mixerVoiceName: String? = null

    private val volumeMain = SimpleFloatProperty(1.0f)
    private val volumeSecondary = SimpleFloatProperty(1.0f)
    private val volumeVoice = SimpleFloatProperty(1.0f)

    private val soundsObsList = FXCollections.observableArrayList<SoundView>()
    private val mixerObsList = FXCollections.observableArrayList<String>()
    private val soundEntryList = FXCollections.observableArrayList<SoundEntry>()
    private val activeSoundBoardMixersList = FXCollections.observableArrayList<Mixer.Info>()
    private val availableMixersData = ArrayList<MixerInfoCached>()

    private val voiceFeatureEnabled = SimpleBooleanProperty(false)
    private val voiceActive = SimpleBooleanProperty(false)
    private var voiceKey = ReadOnlyObjectWrapper<Int?>(null)
    private var voiceMixerParams = ReadOnlyObjectWrapper(VoiceMixerParams(null, null))
    private var voicePitchFactor = SimpleFloatProperty(1.0f)
    private var voiceHighPassFactor = SimpleFloatProperty(0.0f)

    fun getSoundViewList(): ObservableList<SoundView> = FXCollections.unmodifiableObservableList(soundsObsList)
    fun getSoundEntryList(): ObservableList<SoundEntry> = FXCollections.unmodifiableObservableList(soundEntryList)
    fun getAvailableMixersList(): ObservableList<String> = FXCollections.unmodifiableObservableList(mixerObsList)
    fun getActiveSoundBoardMixersList(): ObservableList<Mixer.Info> =
        FXCollections.unmodifiableObservableList(activeSoundBoardMixersList)

    fun getConfigName(): ReadOnlyStringProperty = configName.readOnlyProperty
    fun getConfigFile(): File? = configFile
    fun getMixerMainChange(): ReadOnlyStringProperty = mixerMainChange.readOnlyProperty
    fun getMixerSecondaryChange(): ReadOnlyStringProperty = mixerSecondaryChange.readOnlyProperty
    fun getMixerVoiceChange(): ReadOnlyStringProperty = mixerVoiceChange.readOnlyProperty
    fun getVolumeMain() = volumeMain
    fun getVolumeSecondary() = volumeSecondary
    fun getVolumeVoice() = volumeVoice
    fun getVoiceFeatureEnabled() = voiceFeatureEnabled
    fun getVoiceActive() = voiceActive
    fun getVoiceKey(): ReadOnlyObjectProperty<Int?> = voiceKey.readOnlyProperty
    fun getVoiceMixerParams(): ReadOnlyObjectProperty<VoiceMixerParams> = voiceMixerParams.readOnlyProperty
    fun getVoicePitchFactor() = voicePitchFactor
    fun getVoiceHighPassFactor() = voiceHighPassFactor

    private fun getMixerMain() = availableMixersData.find { info ->
        info.name == mixerMainName
    }

    private fun getMixerSecondary() = availableMixersData.find { info ->
        info.name == mixerSecondaryName
    }

    private fun getMixerVoice() = availableMixersData.find { info ->
        info.name == mixerVoiceName
    }

    private fun updateActiveMixers() {
        val result = ArrayList<Mixer.Info>(2)
        val mixerMain = getMixerMain()
        val mixerSecondary = getMixerSecondary()
        val mixerVoice = getMixerVoice()
        if (mixerMain != null) {
            result.add(mixerMain.info)
        }
        if (mixerSecondary != null) {
            result.add(mixerSecondary.info)
        }
        activeSoundBoardMixersList.setAll(result)
        voiceMixerParams.set(VoiceMixerParams(mixerVoice?.info, mixerSecondary?.info))
    }

    init {
        updateConfigName(false)
    }

    private fun updateConfigName(changed: Boolean) {
        var name = configFile?.name ?: "unnamed"
        if (configFile == null || changed) {
            name += " *"
        }
        configName.set("RoflBoard - $name")
    }

    fun setMainMixer(index: Int) {
        if ((index < 0 && mixerMainName == null) || (index >= 0 && mixerMainName == availableMixersData[index].name)) {
            return
        }
        updateConfigName(true)
        mixerMainName = if (index < 0) {
            null
        } else {
            availableMixersData[index].name
        }
        updateActiveMixers()
    }

    fun setSecondaryMixer(index: Int) {
        if ((index < 0 && mixerSecondaryName == null) || (index >= 0 && mixerSecondaryName == availableMixersData[index].name)) {
            return
        }
        updateConfigName(true)
        mixerSecondaryName = if (index < 0) {
            null
        } else {
            availableMixersData[index].name
        }
        updateActiveMixers()
    }

    fun setVoiceMixer(index: Int) {
        if ((index < 0 && mixerVoiceName == null) || (index >= 0 && mixerVoiceName == availableMixersData[index].name)) {
            return
        }
        updateConfigName(true)
        mixerVoiceName = if (index < 0) {
            null
        } else {
            availableMixersData[index].name
        }
        updateActiveMixers()
    }

    fun setVoiceKey(key: Int) {
        updateConfigName(true)
        voiceKey.set(key)
    }

    fun updateAvailableMixers(mixers: List<MixerInfoCached>) {
        val oldMixerMain = mixerMainName
        val oldMixerSecondary = mixerSecondaryName
        val oldMixerVoice = mixerVoiceName
        availableMixersData.clear()
        availableMixersData.addAll(mixers)
        mixerObsList.clear()
        mixers.forEach { info ->
            mixerObsList.add(info.name)
        }
        if (mixerMainName != null && !availableMixersData.any { it.name == mixerMainName }) {
            mixerMainName = null
            mixerMainChange.set(null)
        } else {
            mixerMainChange.set(null)
            mixerMainChange.set(oldMixerMain)
        }
        if (mixerSecondaryName != null && !availableMixersData.any { it.name == mixerSecondaryName }) {
            mixerSecondaryName = null
            mixerSecondaryChange.set(null)
        } else {
            mixerSecondaryChange.set(null)
            mixerSecondaryChange.set(oldMixerSecondary)
        }
        if (mixerVoiceName != null && !availableMixersData.any { it.name == mixerVoiceName }) {
            mixerVoiceName = null
            mixerVoiceChange.set(null)
        } else {
            mixerVoiceChange.set(null)
            mixerVoiceChange.set(oldMixerVoice)
        }
        updateActiveMixers()
    }

    fun getSound(index: Int): SoundEntry = soundEntryList[index]

    fun addSound(sound: SoundEntryJson) {
        updateConfigName(true)
        addSoundInternal(sound)
    }

    private fun addSoundInternal(sound: SoundEntryJson) {
        val newId = soundIdCounter++
        val actualSound = sound.toEntry(newId)
        soundEntryList.add(actualSound)

        val uiSound = SoundView(
            actualSound.name,
            KeyboardUtils.getKeyText(actualSound.key),
            actualSound.type.toString()
        )
        soundsObsList.add(uiSound)
    }

    fun editSound(index: Int, sound: SoundEntryJson) {
        updateConfigName(true)
        val oldSound = soundEntryList[index]
        val actualSound = sound.toEntry(oldSound.id)
        soundEntryList[index] = actualSound

        val uiSound =
            SoundView(
                actualSound.name,
                KeyboardUtils.getKeyText(actualSound.key),
                actualSound.type.toString()
            )
        soundsObsList[index] = uiSound
    }

    fun deleteSound(index: Int) {
        updateConfigName(true)
        soundEntryList.removeAt(index)
        soundsObsList.removeAt(index)
    }

    fun newConfig() {
        configFile = null
        updateConfigName(false)

        soundsObsList.clear()
        soundEntryList.clear()
    }

    fun save(file: File) {
        configFile = file

        updateConfigName(false)

        val json = Json { prettyPrint = true }
        val mixerMainJson = if (mixerMainName != null) {
            MixerJson(mixerMainName!!, volumeMain.get())
        } else {
            null
        }
        val mixerSecondaryJson = if (mixerSecondaryName != null) {
            MixerJson(mixerSecondaryName!!, volumeSecondary.get())
        } else {
            null
        }
        val mixerVoiceJson = if (mixerVoiceName != null) {
            MixerJson(mixerVoiceName!!, volumeVoice.get())
        } else {
            null
        }
        val soundBoardJson = SoundBoardJson(soundEntryList.map { it.toJson() }, mixerMainJson, mixerSecondaryJson)
        val voiceJson = VoiceJson(
            voiceFeatureEnabled.get(),
            mixerVoiceJson,
            voiceKey.get(),
            voicePitchFactor.get(),
            voiceHighPassFactor.get()
        )

        val config = ConfigJson(soundBoardJson, voiceJson)
        val str = json.encodeToString(config)

        file.writeText(str, charset = Charset.forName("UTF-8"))
    }

    fun load(file: File): ConfigJson {
        configFile = file

        updateConfigName(false)

        val str = file.readText(charset = Charset.forName("UTF-8"))
        val json: ConfigJson = Json.decodeFromString(str)

        soundsObsList.clear()
        soundEntryList.clear()

        if (json.soundBoard.mixerMain == null || !availableMixersData.any { it.name == json.soundBoard.mixerMain.name }) {
            mixerMainName = null
            mixerMainChange.set(null)
        } else {
            volumeMain.set(json.soundBoard.mixerMain.volume)
            mixerMainName = json.soundBoard.mixerMain.name
            mixerMainChange.set(json.soundBoard.mixerMain.name)
        }

        if (json.soundBoard.mixerSecondary == null || !availableMixersData.any { it.name == json.soundBoard.mixerSecondary.name }) {
            mixerSecondaryName = null
            mixerSecondaryChange.set(null)
        } else {
            volumeSecondary.set(json.soundBoard.mixerSecondary.volume)
            mixerSecondaryName = json.soundBoard.mixerSecondary.name
            mixerSecondaryChange.set(json.soundBoard.mixerSecondary.name)
        }

        if (json.voice.mixerInput == null || !availableMixersData.any { it.name == json.voice.mixerInput.name }) {
            mixerVoiceName = null
            mixerVoiceChange.set(null)
        } else {
            volumeVoice.set(json.voice.mixerInput.volume)
            mixerVoiceName = json.voice.mixerInput.name
            mixerVoiceChange.set(json.voice.mixerInput.name)
        }
        updateActiveMixers()

        json.soundBoard.sounds.forEach { sound ->
            addSoundInternal(sound)
        }

        voiceFeatureEnabled.set(json.voice.enabled)
        voiceKey.set(json.voice.key)
        voicePitchFactor.set(json.voice.pitchFactor)
        voiceHighPassFactor.set(json.voice.highPassFactor)

        return json
    }
}