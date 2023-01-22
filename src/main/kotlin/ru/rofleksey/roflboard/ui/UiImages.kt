package ru.rofleksey.roflboard.ui

import javafx.scene.image.Image

class UiImages {
    companion object {
        val LOGO = Image(UiImages::class.java.getResourceAsStream("/logo.jpg"))
        val REFRESH = Image(UiImages::class.java.getResourceAsStream("/refresh.png"))
        val PLUS = Image(UiImages::class.java.getResourceAsStream("/plus.png"))
        val FILE = Image(UiImages::class.java.getResourceAsStream("/file.png"))
        val CHECK = Image(UiImages::class.java.getResourceAsStream("/check.png"))
        val EYE = Image(UiImages::class.java.getResourceAsStream("/eye.png"))
        val ALERT = Image(UiImages::class.java.getResourceAsStream("/alert.png"))
        val EXCLAMATION = Image(UiImages::class.java.getResourceAsStream("/exclamation.png"))
        val KEYBOARD = Image(UiImages::class.java.getResourceAsStream("/keyboard.png"))
        val KEYBOARD_OFF = Image(UiImages::class.java.getResourceAsStream("/keyboard-off.png"))
        val TRASH = Image(UiImages::class.java.getResourceAsStream("/trash.png"))
    }
}