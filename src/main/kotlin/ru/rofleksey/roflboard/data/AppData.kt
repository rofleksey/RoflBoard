package ru.rofleksey.roflboard.data

import javafx.beans.property.ReadOnlyFloatWrapper
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.rofleksey.roflboard.keyboard.KeyPressed
import ru.rofleksey.roflboard.sound.MixerInfoCached
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.ui.SoundView
import java.io.File
import java.nio.charset.Charset
import javax.sound.sampled.Mixer

class AppData {

    private val configName = ReadOnlyStringWrapper(null)
    private var configFile: File? = null
    private var soundIdCounter = 0

    private val mixerMainChange = ReadOnlyStringWrapper(null)
    private val mixerSecondaryChange = ReadOnlyStringWrapper(null)
    private val volumeMain = ReadOnlyFloatWrapper(1.0f)
    private val volumeSecondary = ReadOnlyFloatWrapper(1.0f)

    private val soundsObsList = FXCollections.observableArrayList<SoundView>()
    private val mixerObsList = FXCollections.observableArrayList<String>()
    private val soundEntryList = FXCollections.observableArrayList<SoundEntry>()
    private val activeMixersList = FXCollections.observableArrayList<Mixer.Info>()

    private var mixerMainName: String? = null
    private var mixerSecondaryName: String? = null
    private val availableMixersData = ArrayList<MixerInfoCached>()

    fun getSoundViewList(): ObservableList<SoundView> = FXCollections.unmodifiableObservableList(soundsObsList)
    fun getSoundEntryList(): ObservableList<SoundEntry> = FXCollections.unmodifiableObservableList(soundEntryList)
    fun getAvailableMixersList(): ObservableList<String> = FXCollections.unmodifiableObservableList(mixerObsList)
    fun getActiveMixersList(): ObservableList<Mixer.Info> = FXCollections.unmodifiableObservableList(activeMixersList)
    fun getConfigName(): ReadOnlyStringProperty = configName.readOnlyProperty
    fun getConfigFile(): File? = configFile
    fun getMixerMainChange(): ReadOnlyStringProperty = mixerMainChange.readOnlyProperty
    fun getMixerSecondaryChange(): ReadOnlyStringProperty = mixerSecondaryChange.readOnlyProperty
    fun getVolumeMain() = volumeMain
    fun getVolumeSecondary() = volumeSecondary

    private fun getMixerMain() = availableMixersData.find { info ->
        info.name == mixerMainName
    }

    private fun getMixerSecondary() = availableMixersData.find { info ->
        info.name == mixerSecondaryName
    }

    private fun updateActiveMixers() {
        val result = ArrayList<Mixer.Info>(2)
        val mixerMain = getMixerMain()
        val mixerSecondary = getMixerSecondary()
        if (mixerMain != null) {
            result.add(mixerMain.info)
        }
        if (mixerSecondary != null) {
            result.add(mixerSecondary.info)
        }
        activeMixersList.setAll(result)
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

    fun setVolumeMain(volume: Float) {
        if (volume == volumeMain.get()) {
            return
        }
        volumeMain.set(volume)
    }

    fun setVolumeSecondary(volume: Float) {
        if (volume == volumeSecondary.get()) {
            return
        }
        volumeSecondary.set(volume)
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

    fun updateAvailableMixers(mixers: List<MixerInfoCached>) {
        val oldMixerMain = mixerMainName
        val oldMixerSecondary = mixerSecondaryName
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
        updateActiveMixers()
    }

    fun getSound(index: Int): SoundEntry = soundEntryList[index]

    fun addSound(sound: SoundEntryJson) {
        updateConfigName(true)
        val newId = soundIdCounter++
        val actualSound = sound.toEntry(newId)
        soundEntryList.add(actualSound)

        val uiSound = SoundView(
            actualSound.name,
            actualSound.keys.joinToString("+") { KeyPressed.fromCode(it).name },
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
                actualSound.keys.joinToString("+") { KeyPressed.fromCode(it).name },
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
        configName.set(file.name)
        configFile = file

        updateConfigName(false)

        val json = Json { prettyPrint = true }
        val config = ConfigJson(
            sounds = soundEntryList.map { it.toJson() },
            mixerMain = mixerMainName,
            mixerSecondary = mixerSecondaryName,
            volumeMain = volumeMain.get(),
            volumeSecondary = volumeSecondary.get(),
        )
        val str = json.encodeToString(config)

        file.writeText(str, charset = Charset.forName("UTF-8"))
    }

    fun load(file: File): ConfigJson {
        configName.set(file.name)
        configFile = file

        updateConfigName(false)

        val str = file.readText(charset = Charset.forName("UTF-8"))
        val json: ConfigJson = Json.decodeFromString(str)

        if (json.mixerMain == null || !availableMixersData.any { it.name == json.mixerMain }) {
            mixerMainName = null
            mixerMainChange.set(null)
        } else {
            mixerMainName = json.mixerMain
            mixerMainChange.set(json.mixerMain)
        }
        if (json.mixerSecondary == null || !availableMixersData.any { it.name == json.mixerSecondary }) {
            mixerSecondaryName = null
            mixerSecondaryChange.set(null)
        } else {
            mixerSecondaryName = json.mixerSecondary
            mixerSecondaryChange.set(json.mixerSecondary)
        }
        updateActiveMixers()


        soundsObsList.clear()
        soundEntryList.clear()
        json.sounds.forEach { sound ->
            addSound(sound)
        }

        volumeMain.set(json.volumeMain)
        volumeSecondary.set(json.volumeSecondary)

        return json
    }
}