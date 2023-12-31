package ru.rofleksey.roflboard.controller

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.websocket.WsContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.rofleksey.roflboard.data.AppData
import ru.rofleksey.roflboard.data.ConfigJson
import ru.rofleksey.roflboard.data.SoundType
import ru.rofleksey.roflboard.sound.SoundCheckService
import ru.rofleksey.roflboard.sound.SoundEngine
import ru.rofleksey.roflboard.sound.SoundEntry
import ru.rofleksey.roflboard.voice.VoiceEngine
import java.time.Duration
import java.util.logging.Logger

class NetworkController : Controller {
    companion object {
        private const val PORT = 3910
        private var log: Logger = Logger.getLogger(NetworkController::class.java.name)
        @Serializable
        private data class SoundLink(val id: Int, val name: String, val type: SoundType)
        @Serializable
        private data class WsListMessage(val type: String, val data: List<SoundLink>)
        @Serializable
        private data class WsIdMessage(val type: String, val data: Int)
    }

    private lateinit var soundEngine: SoundEngine
    private val soundMap = ArrayList<SoundLink>()

    @Volatile
    private var wsContext: WsContext? = null

    private var server = Javalin.create { cfg ->
        cfg.jetty.wsFactoryConfig { wsCfg ->
            wsCfg.idleTimeout = Duration.ZERO
        }
        cfg.staticFiles.add("www", Location.EXTERNAL)
    }.ws("/ws") { ws ->
        ws.onConnect { ctx ->
            log.info("Websocket connected, sending " + soundMap.size + " sounds")
            wsContext = ctx
            sendSoundList()
        }

        ws.onMessage { ctx ->
            val text = ctx.message()
            val msg: WsIdMessage = Json.decodeFromString(text)
            val id = msg.data

            when (msg.type) {
                "start" -> soundEngine.startSound(id)
                "stop" -> soundEngine.stopSound(id)
            }
        }

        ws.onError { ctx ->
            ctx.error()?.printStackTrace()
        }

        ws.onClose {
            log.info("Websocket disconnected")
            soundEngine.stopAllSounds()
        }
    }

    override fun register(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {
        this.soundEngine = soundEngine
        server?.start(PORT)
    }

    override fun unregister(soundEngine: SoundEngine, voiceEngine: VoiceEngine, appData: AppData) {
        server?.stop()
    }

    override fun loadSound(sound: SoundEntry) {
        val link = SoundLink(sound.id, sound.name, sound.type)
        soundMap.add(link)
        sendSoundList()
    }

    override fun unloadSound(sound: SoundEntry) {
        soundMap.removeIf { it.id == sound.id }
        sendSoundList()
    }

    private fun sendSoundList() {
        val msg = Json.encodeToString(WsListMessage("list", soundMap))
        wsContext?.send(msg)
    }
}