package ru.rofleksey.roflboard.utils

import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

class FileChooserBuilder private constructor(private val fileChooser: FileChooser, private val prefKey: String) {
    companion object {
        fun new(key: String): FileChooserBuilder {
            val prefKey = "dir_$key"
            val oldDir = Preferences.INSTANCE.getString(prefKey)
            val fileChooser = FileChooser()
            if (oldDir != null) {
                val oldDirFile = File(oldDir)
                if (oldDirFile.exists()) {
                    fileChooser.initialDirectory = File(oldDir)
                }
            }
            return FileChooserBuilder(fileChooser, prefKey)
        }
    }

    fun setTitle(title: String): FileChooserBuilder {
        fileChooser.title = title
        return this
    }

    fun addExtensionFilters(vararg filters: FileChooser.ExtensionFilter): FileChooserBuilder {
        fileChooser.extensionFilters.addAll(filters)
        return this
    }

    fun showOpenDialog(window: Window): File? {
        return fileChooser.showOpenDialog(window).also { file ->
            if (file == null) {
                return@also
            }
            val parent = file.parentFile
            if (parent != null) {
                Preferences.INSTANCE.putString(prefKey, parent.absolutePath).save()
            }
        }
    }

    fun showSaveDialog(window: Window): File? {
        return fileChooser.showSaveDialog(window).also { file ->
            if (file == null) {
                return@also
            }
            val parent = file.parentFile
            if (parent != null) {
                Preferences.INSTANCE.putString(prefKey, parent.absolutePath).save()
            }
        }
    }

    fun showOpenMultipleDialog(window: Window): List<File>? {
        return fileChooser.showOpenMultipleDialog(window).also { files ->
            if (files == null || files.isEmpty()) {
                return@also
            }
            val parent = files[0].parentFile
            if (parent != null) {
                Preferences.INSTANCE.putString(prefKey, parent.absolutePath).save()
            }
        }
    }
}