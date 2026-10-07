package io.github.vinaooo.vinkit.core

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A game's state as a short text, for bug reports: its JSON, gzipped and in Base64, about a thousand characters,
 * short enough for a GitHub issue link. The debug build turns it back into the state to replay the report.
 */
class GameCodec<T>(private val serializer: KSerializer<T>) {
    fun encode(state: T): String {
        val bytes = ByteArrayOutputStream()
        GZIPOutputStream(bytes).use { it.write(json.encodeToString(serializer, state).toByteArray()) }
        return Base64.getEncoder().encodeToString(bytes.toByteArray())
    }

    /** Line breaks and spaces, as an issue or an email may add, are ignored. */
    fun decode(code: String): T {
        val zipped = Base64.getMimeDecoder().decode(code.trim())
        return json.decodeFromString(serializer, GZIPInputStream(zipped.inputStream()).use { String(it.readBytes()) })
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
