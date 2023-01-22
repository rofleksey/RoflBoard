package ru.rofleksey.roflboard.ui

import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.rofleksey.roflboard.data.KeyboardModel
import ru.rofleksey.roflboard.data.SoundType
import ru.rofleksey.roflboard.keyboard.RenderKeyboardModel
import ru.rofleksey.roflboard.sound.SoundEntry
import java.nio.charset.Charset
import kotlin.math.max
import kotlin.math.min

class KeyboardModelWindow {
    companion object {
        private const val ZOOM_FACTOR = 0.001
        private var window: Stage? = null
    }

    private val defaultModel: KeyboardModel = Json.decodeFromString(
        String(
            UiImages::class.java.getResourceAsStream("/keyboard-model-default.json")!!.readBytes(),
            charset = Charset.forName("UTF-8")
        )
    )
    private var zoom = 1.0
    private var translateX = 0.0
    private var translateY = 0.0

    private var mousePressed = false
    private var pressTranslateX = 0.0
    private var pressTranslateY = 0.0
    private var pressX = 0.0
    private var pressY = 0.0

    private val boldFont = Font.font("monospace", FontWeight.EXTRA_BOLD, 13.0)
    private val normalFont = Font.font("monospace", 12.0)
    private val italicFont = Font.font("monospace", FontPosture.ITALIC, 12.0)

    private lateinit var renderModel: RenderKeyboardModel

    private val sounds = ArrayList<SoundEntry>()

    private fun measureTextWidth(text: String, font: Font): Double {
        val theText = Text(text)
        theText.font = font
        return theText.boundsInLocal.width
    }

    private fun trimText(text: String, font: Font, maxWidth: Double): String {
        var curText = text
        var curWidth = measureTextWidth(text, font)
        while (curWidth > maxWidth && curText.isNotEmpty()) {
            curText = curText.substring(0, curText.length - 1)
            curWidth = measureTextWidth(curText, font)
        }
        return curText
    }

    private fun render(g: GraphicsContext) {
        g.save()

        g.fill = Color.WHITE
        g.fillRect(0.0, 0.0, renderModel.width, renderModel.height)
        g.fill = Color.BLACK

        g.translate(translateX, translateY)

        g.translate(renderModel.width / 2, renderModel.height / 2)
        g.scale(zoom, zoom)
        g.translate(-renderModel.width / 2, -renderModel.height / 2)


        g.strokeRect(0.0, 0.0, renderModel.width, renderModel.height)


        renderModel.rows.forEach { row ->
            row.arrays.forEach { array ->
                var curX = array.x
                array.keys.forEach { key ->
                    if (key.sounds.isNotEmpty()) {
                        g.strokeRect(
                            curX * renderModel.ratio,
                            array.y * renderModel.ratio,
                            key.width * renderModel.ratio,
                            key.height * renderModel.ratio
                        )
                        g.font = boldFont
                        var curY = (array.y + key.height / 3) * renderModel.ratio
                        val nameWidth = measureTextWidth(key.name, boldFont)
                        g.fillText(
                            key.name,
                            (curX + key.width / 2) * renderModel.ratio - nameWidth / 2,
                            curY
                        )
                        curY += 12
                        g.font = normalFont
                        key.sounds.forEach { sound ->
                            when (sound.type) {
                                SoundType.PRESSED -> {
                                    g.fill = Color.BLACK
                                }

                                SoundType.TOGGLE -> {
                                    g.fill = Color.BLUE
                                }

                                else -> {
                                    g.fill = Color.RED
                                }
                            }
                            val trimmedText = trimText(sound.name, g.font, key.width * renderModel.ratio)
                            val textWidth = measureTextWidth(trimmedText, normalFont)
                            g.fillText(
                                trimmedText,
                                (curX + key.width / 2) * renderModel.ratio - textWidth / 2,
                                curY
                            )
                            curY += 12
                        }
                        g.fill = Color.BLACK
                    }
                    curX += key.width + array.gap
                }
            }
        }

        g.restore()
    }

    private fun genRenderModel() {
        renderModel = RenderKeyboardModel.fromModelAndSounds(defaultModel, sounds).apply {
            calcLayoutDimensions()
        }
    }

    private fun fixViewPort() {
        translateX = max(-renderModel.width + 50, translateX)
        translateY = max(-renderModel.height + 50, translateY)

        translateX = min(renderModel.width - 50, translateX)
        translateY = min(renderModel.height - 50, translateY)

        zoom = max(0.5, zoom)
        zoom = min(5.0, zoom)
    }

    fun show(soundsObs: ObservableList<SoundEntry>) {
        window?.close()

        sounds.addAll(soundsObs)
        genRenderModel()

        val windowStage = Stage()
        val windowRoot = StackPane()


        val canvas = Canvas(renderModel.width, renderModel.height)
        val g = canvas.graphicsContext2D
        g.font = Font.font("monospace", 12.0)

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED) { e ->
            pressX = e.sceneX
            pressY = e.sceneY
            pressTranslateX = translateX
            pressTranslateY = translateY
            mousePressed = true
        }

        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED) { e ->
            if (mousePressed) {
                translateX = pressTranslateX + (e.sceneX - pressX)
                translateY = pressTranslateY + (e.sceneY - pressY)

                fixViewPort()
                render(g)
            }
        }

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED) { e ->
            mousePressed = false
        }

        canvas.setOnScroll { e ->
            zoom += e.deltaY * ZOOM_FACTOR

            fixViewPort()
            render(g)
        }

        windowRoot.children.addAll(canvas)

        val windowScene = Scene(windowRoot, renderModel.width, renderModel.height)

        windowStage.apply {
            scene = windowScene
            title = "Keyboard Model"
            icons.add(UiImages.LOGO)
        }

        window = windowStage

        windowStage.show()

        render(g)
    }
}