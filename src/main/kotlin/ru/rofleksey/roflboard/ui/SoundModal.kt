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
import ru.rofleksey.roflboard.keyboard.KeyPressed
import ru.rofleksey.roflboard.keyboard.KeyboardListener
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.sound.SoundFacade
import java.io.File

class SoundModal {
    companion object {

        data class SoundResult(val name: String, val file: File, val type: SoundType, val keys: Set<Int>)

        fun show(
            mainStage: Stage,
            soundFacade: SoundFacade,
            soundInfo: SoundEntry?,
            callback: (newSound: SoundResult?) -> Unit
        ) {
            var curFile = if (soundInfo != null) {
                File(soundInfo.path)
            } else {
                null
            }
            var curKeys = if (soundInfo != null) {
                LinkedHashSet(soundInfo.keys)
            } else {
                null
            }
            var recording = false

            if (soundInfo != null) {
                curFile = File(soundInfo.path)
            }

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

            val fileLabel = Label("Select file").apply {
                textOverrun = OverrunStyle.LEADING_ELLIPSIS
            }
            val fileButton = Button("Select")

            val nameLabel = Label("Name")
            val nameEdit = TextField().apply {
                text = soundInfo?.name ?: ""
            }

            fileButton.setOnAction {
                val fileChooser = FileChooser()
                fileChooser.title = "Select sound"
                fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("WAV", "*.wav"))
                val file = fileChooser.showOpenDialog(modalStage)
                if (file != null) {
                    try {
                        soundFacade.tryLoad(file)
                    } catch (e: Exception) {
                        Alert(Alert.AlertType.ERROR).apply {
                            headerText = "File open error"
                            contentText = e.toString()
                            showAndWait()
                        }
                        return@setOnAction
                    }
                    curFile = file
                    fileLabel.text = file.name
                    nameEdit.text = file.nameWithoutExtension
                }
            }

            val typeLabel = Label("Type")
            val typeComboBox = ComboBox(typeObsList).apply {
                selectionModel.select(soundInfo?.type ?: SoundType.FULL)
            }

            val keyLabel = Label().apply {
                text = curKeys?.joinToString("+") { KeyPressed.fromCode(it).name } ?: "Key"
            }
            val keyButton = Button("Record")

            val listener = object : KeyboardListener {
                override fun onKeysPressed(curKey: Int, keys: Set<Int>) {
                    if (curKey == NativeKeyEvent.VC_ESCAPE) {
                        recording = false
                        keyButton.text = "Record"
                        GlobalEventsManager.INSTANCE.unregister(this)
                        return
                    }
                    curKeys = LinkedHashSet(keys)
                    keyLabel.text = curKeys?.joinToString("+") { KeyPressed.fromCode(it).name }
                }

                override fun onKeysReleased(curKey: Int, keys: Set<Int>) {

                }
            }

            keyButton.setOnAction {
                recording = !recording
                if (recording) {
                    keyLabel.text = "Press any key"
                    keyButton.text = "Stop"
                    GlobalEventsManager.INSTANCE.register(listener)
                } else {
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
                val newFile = curFile?.absoluteFile
                val newType = typeComboBox.value

                if (newName.isBlank()) {
                    Alert(Alert.AlertType.WARNING).apply {
                        contentText = "Name is blank"
                        showAndWait()
                    }
                    return@setOnAction
                }

                if (newFile == null) {
                    Alert(Alert.AlertType.WARNING).apply {
                        contentText = "File is not selected"
                        showAndWait()
                    }
                    return@setOnAction
                }

                if (curKeys == null) {
                    Alert(Alert.AlertType.WARNING).apply {
                        contentText = "Keys are not recorded"
                        showAndWait()
                    }
                    return@setOnAction
                }

                val newSound = SoundResult(newName, newFile, newType, LinkedHashSet(curKeys!!))
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