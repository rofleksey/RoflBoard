package ru.rofleksey.roflboard.ui

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import ru.rofleksey.roflboard.data.SoundType
import ru.rofleksey.roflboard.keyboard.GlobalEventsManager
import ru.rofleksey.roflboard.keyboard.KeyboardListener
import ru.rofleksey.roflboard.keyboard.KeyboardUtils
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.sound.SoundFacade
import java.io.File

class SoundModal {
    companion object {

        data class SoundResult(val name: String, val files: List<File>, val type: SoundType, val keys: Int)

        fun show(
            mainStage: Stage,
            soundFacade: SoundFacade,
            soundInfo: SoundEntry?,
            callback: (newSound: SoundResult?) -> Unit
        ) {
            var curFiles = soundInfo?.paths?.map { File(it) }
            var curKey = soundInfo?.key
            var recording = false

            val modalStage = Stage()
            val modalRoot = StackPane()
            val modalContent = GridPane().apply {
                hgap = 20.0
                vgap = 8.0
                columnConstraints.addAll(
                    ColumnConstraints(120.0),
                    ColumnConstraints().apply { hgrow = Priority.ALWAYS })
            }
            StackPane.setMargin(modalContent, Insets(8.0, 8.0, 8.0, 8.0))

            val typeObsList = FXCollections.observableArrayList(SoundType.FULL, SoundType.PRESSED, SoundType.TOGGLE)

            val fileLabel = Label("Select files").apply {
                textOverrun = OverrunStyle.LEADING_ELLIPSIS
            }
            val fileButton = Button("Select")

            val nameLabel = Label("Name")
            val nameEdit = TextField().apply {
                text = soundInfo?.name ?: ""
            }

            fileButton.setOnAction {
                val fileChooser = FileChooser()
                fileChooser.title = "Select sounds"
                fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("WAV", "*.wav"))
                val files: List<File>? = fileChooser.showOpenMultipleDialog(modalStage)
                if (files != null) {
                    try {
                        soundFacade.tryLoad(files)
                    } catch (e: Exception) {
                        Alert(Alert.AlertType.ERROR).apply {
                            headerText = "File open error"
                            contentText = e.toString()
                            showAndWait()
                        }
                        return@setOnAction
                    }
                    curFiles = files
                    fileLabel.text = files.joinToString(",") { it.name }
                    nameEdit.text = files.joinToString(",") { it.nameWithoutExtension }
                }
            }

            val typeLabel = Label("Type")
            val typeComboBox = ComboBox(typeObsList).apply {
                selectionModel.select(soundInfo?.type ?: SoundType.FULL)
            }

            val keyLabel = Label().apply {
                text = KeyboardUtils.getDefaultKeyText(soundInfo?.key, "Key")
            }
            val keyButton = Button("Record")

            val listener = object : KeyboardListener {
                override fun onKeyPressed(key: Int) {
                    recording = false
                    keyButton.text = "Record"
                    GlobalEventsManager.INSTANCE.unregister(this)
                    if (key == NativeKeyEvent.VC_ESCAPE) {
                        curKey = soundInfo?.key
                        keyLabel.text = KeyboardUtils.getDefaultKeyText(curKey, "Key")
                        return
                    }
                    curKey = key
                    keyLabel.text = KeyboardUtils.getDefaultKeyText(curKey, "Key")
                }

                override fun onKeyReleased(key: Int) {

                }
            }

            keyButton.setOnAction {
                recording = !recording
                if (recording) {
                    keyLabel.text = "Press any key"
                    keyButton.text = "Stop"
                    GlobalEventsManager.INSTANCE.register(listener)
                } else {
                    keyLabel.text = KeyboardUtils.getDefaultKeyText(curKey, "Key")
                    keyButton.text = "Record"
                    GlobalEventsManager.INSTANCE.unregister(listener)
                }
            }

            val submitButtonText = if (soundInfo == null) {
                "Add"
            } else {
                "Apply"
            }
            val submitButton = Button(submitButtonText)
            submitButton.setOnAction {
                val newName = nameEdit.text.trim()
                val newFiles = curFiles?.map { it.absoluteFile }
                val newType = typeComboBox.value

                if (newName.isBlank()) {
                    Alert(Alert.AlertType.WARNING).apply {
                        contentText = "Name is blank"
                        showAndWait()
                    }
                    return@setOnAction
                }

                if (newFiles == null) {
                    Alert(Alert.AlertType.WARNING).apply {
                        contentText = "File is not selected"
                        showAndWait()
                    }
                    return@setOnAction
                }

                if (curKey == null) {
                    Alert(Alert.AlertType.WARNING).apply {
                        contentText = "Keys are not recorded"
                        showAndWait()
                    }
                    return@setOnAction
                }

                val newSound = SoundResult(newName, newFiles, newType, curKey!!)
                callback(newSound)
                modalStage.close()
            }

            modalContent.add(fileLabel, 0, 0)
            modalContent.add(fileButton, 1, 0)
            modalContent.add(nameLabel, 0, 1)
            modalContent.add(nameEdit, 1, 1)
            modalContent.add(typeLabel, 0, 2)
            modalContent.add(typeComboBox, 1, 2)
            modalContent.add(keyLabel, 0, 3)
            modalContent.add(keyButton, 1, 3)
            modalContent.add(submitButton, 1, 4)

            modalRoot.children.add(modalContent)

            val modalScene = Scene(modalRoot, 400.0, 175.0)

            modalStage.apply {
                scene = modalScene
                title = if (soundInfo == null) {
                    "Add sound"
                } else {
                    "Edit sound"
                }
                icons.add(UiUtils.LOGO)
                isResizable = false
                initModality(Modality.WINDOW_MODAL)
                initOwner(mainStage)
                setOnHiding {
                    GlobalEventsManager.INSTANCE.unregister(listener)
                }
            }
            modalStage.show()
        }
    }
}