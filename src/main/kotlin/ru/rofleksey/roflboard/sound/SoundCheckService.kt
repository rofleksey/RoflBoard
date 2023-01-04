package ru.rofleksey.roflboard.sound

import javafx.application.Platform
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import ru.rofleksey.roflboard.sound.rules.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger

class SoundCheckService private constructor() {
    companion object {
        val INSTANCE = SoundCheckService()
        private var log: Logger = Logger.getLogger(SoundCheckService::class.java.name)
    }

    private val waitExecutor = Executors.newSingleThreadExecutor()
    private val checkExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val isCheckingProperty = ReadOnlyBooleanWrapper(false)
    private val alerts = FXCollections.observableArrayList<SoundCheckAlert>()
    private var task: Future<*>? = null

    fun getAlertList(): ObservableList<SoundCheckAlert> = FXCollections.unmodifiableObservableList(alerts)

    fun addManual(newAlert: SoundCheckAlert) {
        alerts.add(newAlert)
    }

    fun isChecking(): ReadOnlyBooleanProperty = isCheckingProperty.readOnlyProperty

    fun clear() {
        cancel()
        alerts.clear()
        log.info("Alerts cleared")
    }

    fun checkHeavy(sounds: List<SoundEntry>) {
        cancel()
        if (sounds.isEmpty()) {
            return
        }
        val rules = listOf(DuplicatesSoundRule(sounds), SilenceSoundRule(sounds), DurationSoundRule(sounds))
        checkInBackground(rules)
    }

    private fun checkInBackground(rules: List<SoundCheckRule>) {
        log.info("Started check")
        isCheckingProperty.set(true)
        val futures = rules.map { rule ->
            checkExecutor.submit(Callable {
                try {
                    rule.check().also { result ->
                        if (result.isEmpty()) {
                            log.info("${rule::class.qualifiedName} - OK")
                        } else {
                            log.info("${rule::class.qualifiedName} - ${result.size} warnings")
                        }
                    }
                } catch (ignored: InterruptedException) {
                    listOf()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    listOf(SoundCheckAlert(e.toString(), SoundCheckAlert.Status.ERROR))
                }
            })
        }
        task = waitExecutor.submit {
            try {
                val newAlerts = ArrayList<SoundCheckAlert>()
                futures.forEach { future ->
                    newAlerts.addAll(future.get())
                }
                log.info("Check finished: ${newAlerts.size} new alerts")
                Platform.runLater {
                    alerts.addAll(newAlerts)
                }
            } catch (ignored: InterruptedException) {

            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                futures.forEach { future ->
                    future.cancel(true)
                }
                Platform.runLater {
                    isCheckingProperty.set(false)
                }
            }
        }
    }

    private fun cancel() {
        task?.cancel(true)
        task?.get()
        isCheckingProperty.set(false)
    }
}