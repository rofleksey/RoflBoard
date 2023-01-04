package ru.rofleksey.roflboard.utils

class SilenceDetector {
    data class SilenceOffset(val start: Int, val end: Int)
    companion object {

        fun detectSilence(data: ByteArray, isBigEndian: Boolean): SilenceOffset {
            if (isBigEndian) {
                val startIndex = getStartIndexBigEndian(data)
                if (startIndex == -1) {
                    return SilenceOffset(data.size, 0)
                }
                val endIndex = getEndIndexBigEndian(data)
                return SilenceOffset(startIndex, data.size - endIndex - 1)
            }
            val startIndex = getStartIndexLittleEndian(data)
            if (startIndex == -1) {
                return SilenceOffset(data.size, 0)
            }
            val endIndex = getEndIndexLittleEndian(data)
            return SilenceOffset(startIndex, data.size - endIndex - 1)
        }

        private fun getStartIndexBigEndian(data: ByteArray): Int {
            var index = -1
            var i = 0
            while (i < data.size - 1) {
                var bigHalf = data[i].toShort()
                var smallHalf = data[i + 1].toShort()
                bigHalf = (bigHalf.toInt() and 0xff shl 8).toShort()
                smallHalf = (smallHalf.toInt() and 0xff).toShort()
                val sampleValue = (bigHalf + smallHalf).toShort()

                // Non-zero sample found, we've reached some sound.
                if (sampleValue.toInt() != 0) {
                    index = i
                    return index
                }
                i += 2
            }
            return index
        }

        private fun getEndIndexBigEndian(data: ByteArray): Int {
            var index = -1
            var i = data.size - 2
            while (i >= 0) {
                var bigHalf = data[i].toShort()
                var smallHalf = data[i + 1].toShort()
                bigHalf = (bigHalf.toInt() and 0xff shl 8).toShort()
                smallHalf = (smallHalf.toInt() and 0xff).toShort()
                val sampleValue = (bigHalf + smallHalf).toShort()

                // Non-zero sample found, we've reached some sound.
                // index + 2 as we want to keep this current sample when cutting.
                if (sampleValue.toInt() != 0) {
                    index = i + 2
                    return index
                }
                i -= 2
            }
            return index
        }

        private fun getStartIndexLittleEndian(data: ByteArray): Int {
            var index = -1
            var i = 0
            while (i < data.size - 1) {
                var bigHalf = data[i + 1].toShort()
                var smallHalf = data[i].toShort()
                bigHalf = (bigHalf.toInt() and 0xff shl 8).toShort()
                smallHalf = (smallHalf.toInt() and 0xff).toShort()
                val sampleValue = (bigHalf + smallHalf).toShort()

                // Non-zero sample found, we've reached some sound.
                if (sampleValue > 0) {
                    index = i
                    return index
                }
                i += 2
            }
            return index
        }

        private fun getEndIndexLittleEndian(data: ByteArray): Int {
            var index = -1
            var i = data.size - 2
            while (i >= 0) {
                var bigHalf = data[i + 1].toShort()
                var smallHalf = data[i].toShort()
                bigHalf = (bigHalf.toInt() and 0xff shl 8).toShort()
                smallHalf = (smallHalf.toInt() and 0xff).toShort()
                val sampleValue = (bigHalf + smallHalf).toShort()

                // Non-zero sample found, we've reached some sound.
                // index + 2 as we want to keep this current sample when cutting.
                if (sampleValue > 0) {
                    index = i + 2
                    return index
                }
                i -= 2
            }
            return index
        }
    }
}