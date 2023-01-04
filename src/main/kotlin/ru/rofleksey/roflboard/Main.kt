package ru.rofleksey.roflboard

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Stage
import ru.rofleksey.roflboard.controller.ComplexController
import ru.rofleksey.roflboard.controller.KeyboardController
import ru.rofleksey.roflboard.controller.NetworkController
import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.data.SoundEntryJson
import ru.rofleksey.roflboard.keyboard.GlobalEventsManager
import ru.rofleksey.roflboard.keyboard.KeyboardListener
import ru.rofleksey.roflboard.keyboard.KeyboardUtils
import ru.rofleksey.roflboard.sound.ClipSetRotationFactory
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundFacade
import ru.rofleksey.roflboard.sound.SoundUtils
import ru.rofleksey.roflboard.ui.SoundModal
import ru.rofleksey.roflboard.ui.SoundView
import ru.rofleksey.roflboard.ui.UiUtils
import ru.rofleksey.roflboard.utils.FileChooserBuilder
import ru.rofleksey.roflboard.voice.VoiceEngine
import java.awt.Desktop
import java.net.URL
import kotlin.system.exitProcess


open class Main : Application() {
    private val appData = AppData()
    private val soundEngine = SoundEngine()
    private val soundController = ComplexController(listOf(KeyboardController(), NetworkController()))
    private val clipSetRotationFactory = ClipSetRotationFactory()
    private val soundFacade = SoundFacade(
        soundEngine,
        clipSetRotationFactory,
        soundController,
        appData.getSoundEntryList(),
        appData.getActiveSoundBoardMixersList(),
        appData.getVolumeMain(),
        appData.getVolumeSecondary()
    )
    private val voiceEngine = VoiceEngine(
        appData.getVoiceFeatureEnabled(),
        appData.getVoiceActive(),
        appData.getVoiceMixerParams(),
        appData.getVolumeVoice(),
        appData.getVoicePitchFactor(),
        appData.getVoiceHighPassFactor()
    )

    override fun start(primaryStage: Stage) {
        appData.updateAvailableMixers(SoundUtils.listMixers())
        GlobalEventsManager.INSTANCE.init()
        soundController.register(soundEngine, voiceEngine, appData)
        soundFacade.init()
        voiceEngine.init()

        val root = BorderPane()

        initMenu(primaryStage, root)
        initContent(primaryStage, root)

        val mainScene = Scene(root, 375.0, 500.0)

        primaryStage.apply {
            title = appData.getConfigName().get()
            icons.add(UiUtils.LOGO)
            isResizable = false
            titleProperty().bind(appData.getConfigName())
            scene = mainScene
            setOnHiding {
                GlobalEventsManager.INSTANCE.dispose()
                voiceEngine.dispose()
                Platform.runLater {
                    exitProcess(0)
                }
            }
            show()
        }
    }

    private fun initMenu(stage: Stage, root: BorderPane) {
        val configMenu = Menu("Config").apply {
            val newItem = MenuItem("New").apply {
                accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
            }
            newItem.setOnAction {
                appData.newConfig()
            }

            val loadItem = MenuItem("Open...").apply {
                accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
            }
            loadItem.setOnAction {
                val configFile = FileChooserBuilder
                    .new("config")
                    .setTitle("Open config")
                    .addExtensionFilters(FileChooser.ExtensionFilter("JSON", "*.json"))
                    .showOpenDialog(stage)
                if (configFile != null) {
                    try {
                        appData.load(configFile)
                    } catch (e: Exception) {
                        Alert(AlertType.ERROR).apply {
                            title = "Error loading config"
                            contentText = e.toString()
                            showAndWait()
                        }
                    }
                }
            }

            val saveItem = MenuItem("Save").apply {
                accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
            }
            saveItem.setOnAction {
                var outputFile = appData.getConfigFile()
                if (outputFile == null) {
                    outputFile = FileChooserBuilder
                        .new("config")
                        .setTitle("Save config")
                        .addExtensionFilters(FileChooser.ExtensionFilter("JSON", "*.json"))
                        .showSaveDialog(stage)
                }
                if (outputFile != null) {
                    try {
                        appData.save(outputFile)
                    } catch (e: Exception) {
                        Alert(AlertType.ERROR).apply {
                            title = "Error saving config"
                            contentText = e.toString()
                            showAndWait()
                        }
                    }
                }
            }

            val saveAsItem = MenuItem("Save As...").apply {
                accelerator = KeyCombination.keyCombination("CTRL+SHIFT+S")
            }
            saveAsItem.setOnAction {
                val outputFile = FileChooserBuilder
                    .new("config")
                    .setTitle("Save config")
                    .addExtensionFilters(FileChooser.ExtensionFilter("JSON", "*.json"))
                    .showSaveDialog(stage)
                if (outputFile != null) {
                    try {
                        appData.save(outputFile)
                    } catch (e: Exception) {
                        Alert(AlertType.ERROR).apply {
                            title = "Error saving config"
                            contentText = e.toString()
                            showAndWait()
                        }
                    }
                }
            }
            items.addAll(newItem, loadItem, saveItem, saveAsItem)
        }

        val helpMenu = Menu("Help")

        val aboutMenuItem = MenuItem("About")
        aboutMenuItem.setOnAction {

        }

        val motivationMenuItem = MenuItem("Motivation")
        motivationMenuItem.setOnAction {
            try {
                Desktop.getDesktop().browse(URL("https://youtu.be/NOZONW-UK0w?t=23").toURI())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        helpMenu.items.addAll(aboutMenuItem, motivationMenuItem)

        val menuBar = MenuBar().apply {
            menus.addAll(configMenu, helpMenu)
        }

        root.top = menuBar
    }

    private fun initContent(stage: Stage, root: BorderPane) {
        val tabPane = TabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        }

        val soundsRegion = initSounds(stage)
        val voiceRegion = initVoice(stage, tabPane.selectionModel.selectedItemProperty())

        val soundsTab = Tab("SoundBoard", soundsRegion)
        val voiceTab = Tab("Voice", voiceRegion)
        val spamTab = Tab("Spam", Label("Not implemented yet"))

        tabPane.tabs.addAll(soundsTab, voiceTab)

        root.center = tabPane
    }

    private fun initSounds(stage: Stage): Region {
        val soundsRoot = BorderPane()

        val table = TableView(appData.getSoundViewList()).apply {
            placeholder = Label("No sound clips added yet")
            setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY)
        }

        val editMenuItem = MenuItem("Edit")
        editMenuItem.setOnAction {
            val index = table.selectionModel.selectedIndex
            val sound = appData.getSound(index)
            SoundModal.show(stage, soundFacade, sound) { template ->
                if (template == null) {
                    return@show
                }
                val newSound =
                    SoundEntryJson(template.name, template.files.map { it.absolutePath }, template.type, template.keys)
                appData.editSound(index, newSound)
            }
        }

        val deleteMenuItem = MenuItem("Delete")
        deleteMenuItem.setOnAction {
            val index = table.selectionModel.selectedIndex
            val sound = appData.getSound(index)
            val alert = Alert(AlertType.CONFIRMATION)
            alert.title = "Delete sound"
            alert.contentText = "Do you really want to delete sound '${sound.name}' ?"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                appData.deleteSound(index)
            }
        }

        val menu = ContextMenu()
        menu.items.addAll(editMenuItem, deleteMenuItem)
        table.contextMenu = menu

        table.setRowFactory {
            val row = TableRow<SoundView>()
            row.setOnMouseClicked { event ->
                if (event.clickCount == 2 && !row.isEmpty) {
                    val index = row.index
                    val sound = appData.getSound(index)
                    SoundModal.show(stage, soundFacade, sound) { template ->
                        if (template == null) {
                            return@show
                        }
                        val newSound =
                            SoundEntryJson(
                                template.name,
                                template.files.map { it.absolutePath },
                                template.type,
                                template.keys
                            )
                        appData.editSound(index, newSound)
                    }
                }
            }
            row
        }

        val nameCol = TableColumn<SoundView, String>("Sound Clip").apply {
            cellValueFactory = PropertyValueFactory("name")
            isSortable = false
            isReorderable = false
        }

        val keyCol = TableColumn<SoundView, String>("Key Map").apply {
            cellValueFactory = PropertyValueFactory("keys")
            isSortable = false
            isReorderable = false
        }

        val typeCol = TableColumn<SoundView, String>("Type").apply {
            cellValueFactory = PropertyValueFactory("type")
            isSortable = false
            isReorderable = false
        }

        table.columns.addAll(nameCol, keyCol, typeCol)

        soundsRoot.center = table

        val controlsRoot = initSoundControls(stage, soundsRoot)

        BorderPane.setMargin(table, Insets(8.0, 8.0, 8.0, 8.0))
        BorderPane.setMargin(controlsRoot, Insets(0.0, 8.0, 8.0, 8.0))

        return soundsRoot
    }

    private fun initSoundControls(stage: Stage, soundsRoot: BorderPane): Region {
        val controlsRoot = VBox(3.0)

        val tableControls = HBox(5.0)

        val addButton = Button("Add")
        addButton.setOnAction {
            SoundModal.show(stage, soundFacade, null) { template ->
                if (template == null) {
                    return@show
                }
                val newSound =
                    SoundEntryJson(template.name, template.files.map { it.absolutePath }, template.type, template.keys)
                appData.addSound(newSound)
            }
        }

        val reloadAllButton = Button("Reload all")
        reloadAllButton.setOnAction {
            soundFacade.reloadAll()
        }

        val stopAllButton = Button("Stop all")
        stopAllButton.setOnAction {
            soundEngine.stopAllSounds()
        }

        val controlsSpacer = Region()

        HBox.setHgrow(controlsSpacer, Priority.ALWAYS)
        tableControls.children.addAll(addButton, controlsSpacer, reloadAllButton, stopAllButton)

        val mainMixerLabel = Label("First output (e.g. speakers)")
        val mainMixerHBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
            //style = "-fx-background-color: grey;"
        }
        val mainMixerComboBox = ComboBox(appData.getAvailableMixersList())
        mainMixerComboBox.selectionModel.selectedIndexProperty().addListener { _, _, newIndex ->
            if (newIndex.toInt() >= 0) {
                appData.setMainMixer(newIndex.toInt())
            }
        }
        appData.getMixerMainChange().addListener { _, _, mixerMain ->
            mainMixerComboBox.selectionModel.select(mixerMain)
        }
        val refreshMixersButton = Button("Refresh")
        refreshMixersButton.setOnAction {
            appData.updateAvailableMixers(SoundUtils.listMixers())
        }
        mainMixerComboBox.maxWidth = Double.MAX_VALUE
        HBox.setHgrow(mainMixerComboBox, Priority.ALWAYS)
        mainMixerHBox.children.addAll(mainMixerComboBox, refreshMixersButton)

        val mainVolumeSlider = Slider(0.0, 1.0, appData.getVolumeMain().get().toDouble())
        mainVolumeSlider.valueProperty().bindBidirectional(appData.getVolumeMain())

        val secondaryMixerLabel = Label("Second output (e.g. Virtual Audio Cable Input)")
        val secondaryMixerHBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val secondaryMixerComboBox = ComboBox(appData.getAvailableMixersList())
        secondaryMixerComboBox.selectionModel.selectedIndexProperty().addListener { _, _, newIndex ->
            if (newIndex.toInt() >= 0) {
                appData.setSecondaryMixer(newIndex.toInt())
            }
        }
        appData.getMixerSecondaryChange().addListener { _, _, mixerSecondary ->
            secondaryMixerComboBox.selectionModel.select(mixerSecondary)
        }
        val secondaryMixerCheckBox = CheckBox("Use").apply {
            isSelected = true
        }
        secondaryMixerCheckBox.setOnAction {
            if (secondaryMixerCheckBox.isSelected) {
                appData.setSecondaryMixer(secondaryMixerComboBox.selectionModel.selectedIndex)
                secondaryMixerComboBox.isDisable = false
            } else {
                appData.setSecondaryMixer(-1)
                secondaryMixerComboBox.isDisable = true
            }
        }
        secondaryMixerComboBox.maxWidth = Double.MAX_VALUE
        HBox.setHgrow(secondaryMixerComboBox, Priority.ALWAYS)
        secondaryMixerHBox.children.addAll(secondaryMixerComboBox, secondaryMixerCheckBox)

        val secondaryVolumeSlider = Slider(0.0, 1.0, appData.getVolumeSecondary().get().toDouble())
        secondaryVolumeSlider.valueProperty().bindBidirectional(appData.getVolumeSecondary())

        val separator = Separator()

        VBox.setMargin(tableControls, Insets(0.0, 0.0, 8.0, 0.0))
        controlsRoot.children.addAll(
            tableControls,
            separator,
            mainMixerLabel,
            mainMixerHBox,
            mainVolumeSlider,
            secondaryMixerLabel,
            secondaryMixerHBox,
            secondaryVolumeSlider
        )

        soundsRoot.bottom = controlsRoot

        return controlsRoot
    }

    fun initVoice(stage: Stage, tabProperty: ReadOnlyObjectProperty<Tab>): Region {
        var recording = false

        val voiceRoot = StackPane()

        val voiceContent = VBox(10.0).apply {
            isFillWidth = true
        }
        StackPane.setMargin(voiceContent, Insets(8.0, 8.0, 8.0, 8.0))

        val voiceCheckBox = CheckBox("Enabled").apply {
            selectedProperty().bindBidirectional(appData.getVoiceFeatureEnabled())
        }

        val voiceSettings = VBox(10.0).apply {
            isFillWidth = true
            disableProperty().bind(appData.getVoiceFeatureEnabled().not())
        }

        val voiceMixerLabel = Label("Main input (real mic)")
        val voiceMixerHBox = HBox(5.0)
        val voiceMixerComboBox = ComboBox(appData.getAvailableMixersList())
        voiceMixerComboBox.selectionModel.selectedIndexProperty().addListener { _, _, newIndex ->
            if (newIndex.toInt() >= 0) {
                appData.setVoiceMixer(newIndex.toInt())
            }
        }
        appData.getMixerVoiceChange().addListener { _, _, mixerVoice ->
            voiceMixerComboBox.selectionModel.select(mixerVoice)
        }
        val refreshMixersButton = Button("Refresh")
        refreshMixersButton.setOnAction {
            appData.updateAvailableMixers(SoundUtils.listMixers())
        }
        voiceMixerComboBox.maxWidth = Double.MAX_VALUE
        HBox.setHgrow(voiceMixerComboBox, Priority.ALWAYS)
        voiceMixerHBox.children.addAll(voiceMixerComboBox, refreshMixersButton)

        val voiceKeyHBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val voiceKeyLabel = Label(KeyboardUtils.getDefaultKeyText(appData.getVoiceKey().get(), "Key"))
        appData.getVoiceKey().addListener { _, _, key ->
            voiceKeyLabel.text = KeyboardUtils.getDefaultKeyText(key, "Key")
        }
        val voiceKeyButton = Button("Record")

        val listener = object : KeyboardListener {
            override fun afterKeyPressed(key: Int, curPressed: List<Int>) {
                recording = false
                voiceKeyButton.text = "Record"
                GlobalEventsManager.INSTANCE.unregister(this)
                if (key == NativeKeyEvent.VC_ESCAPE) {
                    voiceKeyLabel.text = KeyboardUtils.getDefaultKeyText(appData.getVoiceKey().get(), "Key")
                    return
                }
                appData.setVoiceKey(key)
            }

            override fun beforeKeyReleased(key: Int, curPressed: List<Int>) {

            }
        }
        voiceKeyButton.setOnAction {
            recording = !recording
            if (recording) {
                voiceKeyLabel.text = "Press any key"
                voiceKeyButton.text = "Apply"
                GlobalEventsManager.INSTANCE.register(listener)
                tabProperty.addListener { _, _, _ ->
                    recording = false
                    voiceKeyButton.text = "Record"
                    voiceKeyLabel.text = KeyboardUtils.getDefaultKeyText(appData.getVoiceKey().get(), "Key")
                    GlobalEventsManager.INSTANCE.unregister(listener)
                }
            } else {
                voiceKeyButton.text = "Record"
                voiceKeyLabel.text = KeyboardUtils.getDefaultKeyText(appData.getVoiceKey().get(), "Key")
                GlobalEventsManager.INSTANCE.unregister(listener)
            }
        }

        voiceKeyHBox.children.addAll(voiceKeyLabel, voiceKeyButton)

        val voiceVolumeHBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val voiceVolumeLabel = Label("Gain")
        val voiceVolumeSlider = Slider(1.0, 50.0, appData.getVolumeVoice().get().toDouble())
        voiceVolumeSlider.valueProperty().bindBidirectional(appData.getVolumeVoice())
        val voiceVolumeReset = Button("Reset")
        voiceVolumeReset.setOnAction {
            appData.getVolumeVoice().set(1.0f)
        }
        HBox.setHgrow(voiceVolumeSlider, Priority.ALWAYS)
        voiceVolumeHBox.children.addAll(voiceVolumeLabel, voiceVolumeSlider, voiceVolumeReset)

        val voicePitchHBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val voicePitchLabel = Label("Pitch")
        val voicePitchSlider = Slider(0.1, 1.5, appData.getVoicePitchFactor().get().toDouble())
        voicePitchSlider.valueProperty().bindBidirectional(appData.getVoicePitchFactor())
        val voicePitchReset = Button("Reset")
        voicePitchReset.setOnAction {
            appData.getVoicePitchFactor().set(1.0f)
        }
        HBox.setHgrow(voicePitchSlider, Priority.ALWAYS)
        voicePitchHBox.children.addAll(voicePitchLabel, voicePitchSlider, voicePitchReset)

        val voiceHighPassHBox = HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val voiceHighPassLabel = Label("HighPass Freq")
        val voiceHighPassSlider = Slider(0.0, 100000.0, appData.getVoiceHighPassFactor().get().toDouble())
        voiceHighPassSlider.valueProperty().bindBidirectional(appData.getVoiceHighPassFactor())
        val voiceHighPassReset = Button("Reset")
        voiceHighPassReset.setOnAction {
            appData.getVoiceHighPassFactor().set(0.0f)
        }
        HBox.setHgrow(voiceHighPassSlider, Priority.ALWAYS)
        voiceHighPassHBox.children.addAll(voiceHighPassLabel, voiceHighPassSlider, voiceHighPassReset)

        voiceSettings.children.addAll(
            voiceMixerLabel,
            voiceMixerHBox,
            voiceKeyHBox,
            voiceVolumeHBox,
            voicePitchHBox,
            voiceHighPassHBox
        )

        voiceContent.children.addAll(voiceCheckBox, voiceSettings)

        voiceRoot.children.add(voiceContent)

        return voiceRoot
    }
}

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}