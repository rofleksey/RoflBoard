package ru.rofleksey.roflboard.ui

import javafx.scene.image.Image

class UiUtils {
    companion object {
        val LOGO = Image(UiUtils::class.java.getResourceAsStream("/logo.jpg"))
    }
}