package ru.rofleksey.roflboard.data

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.rofleksey.roflboard.keyboard.KeyboardUtils
import ru.rofleksey.roflboard.sound.MixerInfoCached
import ru.rofleksey.roflboard.sound.SoundCheckService
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.ui.SoundView
import ru.rofleksey.roflboard.utils.Preferences
import ru.rofleksey.roflboard.voice.VoiceMixerParams
import java.io.File
import java.nio.charset.Charset
import javax.sound.sampled.Mixer

class AppData {
    companion object {
        const val LAST_CONFIG_PREF = "last_config"
    }

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

    private val soundViewList = FXCollections.observableArrayList<SoundView>()
    private val outputMixerObsList = FXCollections.observableArrayList<String>()
    private val inputMixerObsList = FXCollections.observableArrayList<String>()
    private val soundEntryList = FXCollections.observableArrayList<SoundEntry>()
    private val activeSoundBoardMixersList = FXCollections.observableArrayList<Mixer.Info>()

    private val availableOutputMixersData = ArrayList<MixerInfoCached>()
    private val availableInputMixersData = ArrayList<MixerInfoCached>()

    private val voiceFeatureEnabled = SimpleBooleanProperty(false)
    private val voiceActive = SimpleBooleanProperty(false)
    private var voiceKey = ReadOnlyObjectWrapper<KeyPressed?>(null)
    private var voiceMixerParams = ReadOnlyObjectWrapper(VoiceMixerParams(null, null))
    private var voicePitchFactor = SimpleFloatProperty(1.0f)
    private var voiceHighPassFactor = SimpleFloatProperty(0.0f)

    fun getSoundViewList(): ObservableList<SoundView> = soundViewList
    fun getSoundEntryList(): ObservableList<SoundEntry> = FXCollections.unmodifiableObservableList(soundEntryList)
    fun getAvailableOutputMixersList(): ObservableList<String> =
        FXCollections.unmodifiableObservableList(outputMixerObsList)

    fun getAvailableInputMixersList(): ObservableList<String> =
        FXCollections.unmodifiableObservableList(inputMixerObsList)

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
    fun getVoiceKey(): ReadOnlyObjectProperty<KeyPressed?> = voiceKey.readOnlyProperty
    fun getVoiceMixerParams(): ReadOnlyObjectProperty<VoiceMixerParams> = voiceMixerParams.readOnlyProperty
    fun getVoicePitchFactor() = voicePitchFactor
    fun getVoiceHighPassFactor() = voiceHighPassFactor

    private fun getMixerMain() = availableOutputMixersData.find { info ->
        info.name == mixerMainName
    }

    private fun getMixerSecondary() = availableOutputMixersData.find { info ->
        info.name == mixerSecondaryName
    }

    private fun getMixerVoice() = availableInputMixersData.find { info ->
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
        if ((index < 0 && mixerMainName == null) || (index >= 0 && mixerMainName == availableOutputMixersData[index].name)) {
            return
        }
        updateConfigName(true)
        mixerMainName = if (index < 0) {
            null
        } else {
            availableOutputMixersData[index].name
        }
        updateActiveMixers()
    }

    fun setSecondaryMixer(index: Int) {
        if ((index < 0 && mixerSecondaryName == null) || (index >= 0 && mixerSecondaryName == availableOutputMixersData[index].name)) {
            return
        }
        updateConfigName(true)
        mixerSecondaryName = if (index < 0) {
            null
        } else {
            availableOutputMixersData[index].name
        }
        updateActiveMixers()
    }

    fun setVoiceMixer(index: Int) {
        if ((index < 0 && mixerVoiceName == null) || (index >= 0 && mixerVoiceName == availableInputMixersData[index].name)) {
            return
        }
        updateConfigName(true)
        mixerVoiceName = if (index < 0) {
            null
        } else {
            availableInputMixersData[index].name
        }
        updateActiveMixers()
    }

    fun setVoiceKey(key: KeyPressed) {
        updateConfigName(true)
        voiceKey.set(key)
    }

    private fun updateAvailableMixersImpl(oldMixerMain: String?, oldMixerSecondary: String?, oldMixerVoice: String?) {
        if (mixerMainName != null && !availableOutputMixersData.any { it.name == mixerMainName }) {
            mixerMainName = null
            mixerMainChange.set(null)
        } else {
            mixerMainChange.set(null)
            mixerMainChange.set(oldMixerMain)
        }
        if (mixerSecondaryName != null && !availableOutputMixersData.any { it.name == mixerSecondaryName }) {
            mixerSecondaryName = null
            mixerSecondaryChange.set(null)
        } else {
            mixerSecondaryChange.set(null)
            mixerSecondaryChange.set(oldMixerSecondary)
        }
        if (mixerVoiceName != null && !availableInputMixersData.any { it.name == mixerVoiceName }) {
            mixerVoiceName = null
            mixerVoiceChange.set(null)
        } else {
            mixerVoiceChange.set(null)
            mixerVoiceChange.set(oldMixerVoice)
        }
        updateActiveMixers()
    }

    fun updateAvailableOutputMixers(mixers: List<MixerInfoCached>) {
        val oldMixerMain = mixerMainName
        val oldMixerSecondary = mixerSecondaryName
        val oldMixerVoice = mixerVoiceName
        availableOutputMixersData.clear()
        availableOutputMixersData.addAll(mixers)
        outputMixerObsList.clear()
        mixers.forEach { info ->
            outputMixerObsList.add(info.name)
        }
        updateAvailableMixersImpl(oldMixerMain, oldMixerSecondary, oldMixerVoice)
    }

    fun updateAvailableInputMixers(mixers: List<MixerInfoCached>) {
        val oldMixerMain = mixerMainName
        val oldMixerSecondary = mixerSecondaryName
        val oldMixerVoice = mixerVoiceName
        availableInputMixersData.clear()
        availableInputMixersData.addAll(mixers)
        inputMixerObsList.clear()
        mixers.forEach { info ->
            inputMixerObsList.add(info.name)
        }
        updateAvailableMixersImpl(oldMixerMain, oldMixerSecondary, oldMixerVoice)
    }

    fun getSound(viewIndex: Int): SoundEntry {
        val soundView = soundViewList[viewIndex]
        return soundEntryList.first { it.id == soundView.id }
    }

    fun addSound(sound: SoundEntryJson) {
        updateConfigName(true)
        addSoundInternal(sound)
        SoundCheckService.INSTANCE.clear()
        SoundCheckService.INSTANCE.checkHeavy(soundEntryList)
    }

    private fun addSoundInternal(sound: SoundEntryJson) {
        val newId = soundIdCounter++
        val actualSound = sound.toEntry(newId)
        soundEntryList.add(actualSound)

        val uiSound = SoundView(
            newId,
            actualSound.name,
            KeyboardUtils.getKeyText(actualSound.keys),
            actualSound.type.toString()
        )
        soundViewList.add(uiSound)
    }

    fun editSound(viewIndex: Int, sound: SoundEntryJson) {
        updateConfigName(true)
        var soundView = soundViewList[viewIndex]
        val oldRealSoundIndex = soundEntryList.indexOfFirst { it.id == soundView.id }
        val newRealSound = sound.toEntry(soundView.id)
        soundEntryList[oldRealSoundIndex] = newRealSound

        soundView =
            SoundView(
                newRealSound.id,
                newRealSound.name,
                KeyboardUtils.getKeyText(newRealSound.keys),
                newRealSound.type.toString()
            )
        soundViewList[viewIndex] = soundView

        SoundCheckService.INSTANCE.clear()
        SoundCheckService.INSTANCE.checkHeavy(soundEntryList)
    }

    fun deleteSound(viewIndex: Int) {
        updateConfigName(true)
        val soundView = soundViewList.removeAt(viewIndex)
        val realSoundIndex = soundEntryList.indexOfFirst { it.id == soundView.id }
        soundEntryList.removeAt(realSoundIndex)
        SoundCheckService.INSTANCE.clear()
        SoundCheckService.INSTANCE.checkHeavy(soundEntryList)
    }

    fun newConfig() {
        configFile = null
        updateConfigName(false)

        soundViewList.clear()
        soundEntryList.clear()
        SoundCheckService.INSTANCE.clear()
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

    fun loadLast() {
        val lastConfig = Preferences.INSTANCE.getString(LAST_CONFIG_PREF) ?: return
        val file = File(lastConfig)
        if (!file.exists()) {
            return
        }
        load(file)
    }

    fun load(file: File) {
        configFile = file

        updateConfigName(false)

        val str = file.readText(charset = Charset.forName("UTF-8"))
        val json: ConfigJson = Json.decodeFromString(str)

        soundViewList.clear()
        soundEntryList.clear()

        if (json.soundBoard.mixerMain == null || !availableOutputMixersData.any { it.name == json.soundBoard.mixerMain.name }) {
            mixerMainName = null
            mixerMainChange.set(null)
        } else {
            volumeMain.set(json.soundBoard.mixerMain.volume)
            mixerMainName = json.soundBoard.mixerMain.name
            mixerMainChange.set(json.soundBoard.mixerMain.name)
        }

        if (json.soundBoard.mixerSecondary == null || !availableOutputMixersData.any { it.name == json.soundBoard.mixerSecondary.name }) {
            mixerSecondaryName = null
            mixerSecondaryChange.set(null)
        } else {
            volumeSecondary.set(json.soundBoard.mixerSecondary.volume)
            mixerSecondaryName = json.soundBoard.mixerSecondary.name
            mixerSecondaryChange.set(json.soundBoard.mixerSecondary.name)
        }

        if (json.voice.mixerInput == null || !availableInputMixersData.any { it.name == json.voice.mixerInput.name }) {
            mixerVoiceName = null
            mixerVoiceChange.set(null)
        } else {
            volumeVoice.set(json.voice.mixerInput.volume)
            mixerVoiceName = json.voice.mixerInput.name
            mixerVoiceChange.set(json.voice.mixerInput.name)
        }
        updateActiveMixers()

        SoundCheckService.INSTANCE.clear()
        json.soundBoard.sounds.forEach { sound ->
            addSoundInternal(sound)
        }
        SoundCheckService.INSTANCE.checkHeavy(soundEntryList)

        voiceFeatureEnabled.set(json.voice.enabled)
        voiceKey.set(json.voice.key)
        voicePitchFactor.set(json.voice.pitchFactor)
        voiceHighPassFactor.set(json.voice.highPassFactor)

        Preferences.INSTANCE.putString(LAST_CONFIG_PREF, file.absolutePath).save()
    }
}