package ru.rofleksey.roflboard.keyboard

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import javafx.application.Platform
import javafx.scene.control.Alert
import kotlin.system.exitProcess


class GlobalEventsManager private constructor() : NativeKeyListener {
    companion object {
        val INSTANCE = GlobalEventsManager()
    }

    private val listeners = ArrayList<KeyboardListener>()
    private val keysPressed = LinkedHashSet<Int>()

    fun init() {
        try {
            GlobalScreen.setEventDispatcher(SwingDispatchService())
            GlobalScreen.registerNativeHook()
        } catch (e: NativeHookException) {
            e.printStackTrace()
            Alert(Alert.AlertType.ERROR).apply {
                title = "Error injecting keyboard hook"
                contentText = e.toString()
                showAndWait()
            }
            exitProcess(1)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            GlobalScreen.unregisterNativeHook()
        })
        GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
            override fun nativeKeyPressed(event: NativeKeyEvent) {
                Platform.runLater {
                    keysPressed.add(event.keyCode)
                    listeners.forEach { listener ->
                        listener.onKeysPressed(event.keyCode, keysPressed)
                    }
                }
            }

            override fun nativeKeyReleased(event: NativeKeyEvent) {
                Platform.runLater {
                    listeners.forEach { listener ->
                        listener.onKeysReleased(event.keyCode, keysPressed)
                    }
                    keysPressed.remove(event.keyCode)
                }
            }
        })
    }

    fun dispose() {
        GlobalScreen.unregisterNativeHook()
        System.runFinalization()
    }

    fun register(listener: KeyboardListener) {
        Platform.runLater {
            listeners.add(listener)
        }
    }

    fun unregister(listener: KeyboardListener) {
        Platform.runLater {
            listeners.remove(listener)
        }
    }
}