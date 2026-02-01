package com.example.btlearninglab.data.ble

data class ScaleData(
    val weight: Float,
    val isStable: Boolean,
    val rawHex: String
)

object DecentScaleDataParser {
    private const val MODEL_BYTE = 0x03.toByte()
    private const val STABLE_TYPE = 0xCE.toByte()
    private const val UNSTABLE_TYPE = 0xCA.toByte()

    fun parse(bytes: ByteArray): ScaleData {
        require(bytes.size >= 7) { "Data too short: expected 7 bytes, got ${bytes.size}" }

        // Generate raw hex string
        val rawHex = bytes.joinToString(" ") { "%02X".format(it) }

        // Verify XOR checksum
        val calculatedXor = calculateXor(bytes)
        val receivedXor = bytes[6]
        require(calculatedXor == receivedXor) {
            "XOR verification failed: calculated ${"%02X".format(calculatedXor)}, received ${"%02X".format(receivedXor)}"
        }

        // Verify model byte
        require(bytes[0] == MODEL_BYTE) {
            "Invalid model byte: expected ${"%02X".format(MODEL_BYTE)}, got ${"%02X".format(bytes[0])}"
        }

        // Check stability
        val isStable = when (bytes[1]) {
            STABLE_TYPE -> true
            UNSTABLE_TYPE -> false
            else -> throw IllegalArgumentException(
                "Invalid type byte: expected ${"%02X".format(STABLE_TYPE)} or ${"%02X".format(UNSTABLE_TYPE)}, got ${"%02X".format(bytes[1])}"
            )
        }

        // Calculate weight from bytes[2-3] as signed short
        val weightRaw = ((bytes[2].toInt() shl 8) or (bytes[3].toInt() and 0xFF)).toShort()
        val weight = weightRaw / 10.0f

        return ScaleData(
            weight = weight,
            isStable = isStable,
            rawHex = rawHex
        )
    }

    private fun calculateXor(bytes: ByteArray): Byte {
        var xor = 0
        for (i in 0 until bytes.size - 1) {
            xor = xor xor bytes[i].toInt()
        }
        return xor.toByte()
    }
}
