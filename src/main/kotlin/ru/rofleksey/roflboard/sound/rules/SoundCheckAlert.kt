package ru.rofleksey.roflboard.sound.rules

data class SoundCheckAlert(val message: String, val status: Status) {
    enum class Status {
        WARNING, ERROR
    }
}