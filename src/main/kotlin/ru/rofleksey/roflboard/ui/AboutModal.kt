package ru.rofleksey.roflboard.ui

import javafx.scene.Scene
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.web.WebView
import javafx.stage.Stage
import java.nio.charset.Charset

class AboutModal {

    fun show() {
        val windowStage = Stage()
        val windowRoot = GridPane().apply {
            style = "--fx-background-color: #FFFFFF;"
        }

        val logoImage = ImageView(UiImages.LOGO).apply {
            fitWidth = 90.54
            fitHeight = 100.0
            style = "--fx-background-color: #FFFFFF;"
        }

        val heartInts = listOf(0xE2, 0x9D, 0xA4)
        val heartStr = String(ByteArray(3) { i ->
            heartInts[i].toByte()
        }, Charset.forName("UTF-8"))

        val webView = WebView().apply {
            engine.loadContent("<center><b>RoflBoard</b><br />Yet another sounboard<br />Made with $heartStr (and no warranty) by Rofleksey</center>")
        }

        windowRoot.add(logoImage, 0, 0)
        windowRoot.add(webView, 1, 0)

        val windowScene = Scene(windowRoot, 350.0, 100.0)

        windowStage.apply {
            scene = windowScene
            title = "About"
            isResizable = false
            icons.add(UiImages.LOGO)
        }
        windowStage.show()
    }
}