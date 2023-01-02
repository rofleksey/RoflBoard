package ru.rofleksey.roflboard

import javafx.application.Application
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
import ru.rofleksey.roflboard.controller.ComplexSoundController
import ru.rofleksey.roflboard.controller.KeyboardSoundController
import ru.rofleksey.roflboard.controller.NetworkSoundController
import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.data.SoundEntryJson
import ru.rofleksey.roflboard.keyboard.GlobalEventsManager
import ru.rofleksey.roflboard.sound.ClipSetFactory
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundFacade
import ru.rofleksey.roflboard.sound.SoundsUtils
import ru.rofleksey.roflboard.ui.SoundModal
import ru.rofleksey.roflboard.ui.SoundView
import ru.rofleksey.roflboard.ui.UiUtils


open class Main : Application() {
    private val appData = AppData()
    private val soundEngine = SoundEngine()
    private val soundController = ComplexSoundController(listOf(KeyboardSoundController(), NetworkSoundController()))
    private val clipSetFactory = ClipSetFactory()
    private val soundFacade = SoundFacade(
        soundEngine,
        clipSetFactory,
        soundController,
        appData.getSoundEntryList(),
        appData.getActiveMixersList(),
        appData.getVolumeMain(),
        appData.getVolumeSecondary()
    )

    override fun start(primaryStage: Stage) {
        appData.updateAvailableMixers(SoundsUtils.listMixers())
        GlobalEventsManager.INSTANCE.init()
        soundController.register(soundEngine)
        soundFacade.init()

        val root = BorderPane()

        initMenu(primaryStage, root)
        initContent(primaryStage, root)

        val mainScene = Scene(root, 375.0, 500.0)

        primaryStage.apply {
            title = appData.getConfigName().get()
            icons.add(UiUtils.LOGO)
            titleProperty().bind(appData.getConfigName())
            scene = mainScene
            setOnHiding {
                GlobalEventsManager.INSTANCE.dispose()
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
                val fileChooser = FileChooser()
                fileChooser.title = "Open config"
                fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("JSON", "*.json"))
                val configFile = fileChooser.showOpenDialog(stage)
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
                    val fileChooser = FileChooser()
                    fileChooser.title = "Save config"
                    fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("JSON", "*.json"))
                    outputFile = fileChooser.showSaveDialog(stage)
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
                val fileChooser = FileChooser()
                fileChooser.title = "Save config"
                fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("JSON", "*.json"))
                val outputFile = fileChooser.showSaveDialog(stage)
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

        val aboutMenu = Menu("About")

        val menuBar = MenuBar().apply {
            menus.addAll(configMenu, aboutMenu)
        }

        root.top = menuBar
    }

    private fun initContent(stage: Stage, root: BorderPane) {
        val tabPane = TabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        }

        val soundsRegion = initSounds(stage)
        val soundsTab = Tab("SoundBoard", soundsRegion)


        val spamTab = Tab("Spam", Label("Not implemented yet"))

        tabPane.tabs.addAll(soundsTab, spamTab)

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
                val newSound = SoundEntryJson(template.name, template.file.absolutePath, template.type, template.keys)
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
                            SoundEntryJson(template.name, template.file.absolutePath, template.type, template.keys)
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
                val newSound = SoundEntryJson(template.name, template.file.absolutePath, template.type, template.keys)
                appData.addSound(newSound)
            }
        }

        val stopAllButton = Button("Stop all")
        stopAllButton.setOnAction {
            soundEngine.stopAllSounds()
        }

        val controlsSpacer = Region()

        HBox.setHgrow(controlsSpacer, Priority.ALWAYS)
        tableControls.children.addAll(addButton, controlsSpacer, stopAllButton)

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
            appData.updateAvailableMixers(SoundsUtils.listMixers())
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
}

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}