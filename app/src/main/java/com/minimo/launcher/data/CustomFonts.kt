package com.minimo.launcher.data

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import com.minimo.launcher.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest

data class CustomFont(val value: String, val name: String)

class FontImportException(val messageRes: Int) : IOException()

object CustomFonts {
    private const val PREFIX = "custom:"
    private const val MAX_BYTES = 10 * 1024 * 1024
    private val typefaces = LruCache<String, Typeface>(16)
    private val fileNamePattern = Regex("[a-f0-9]{64}_[^/\\\\\\p{Cntrl}]+\\.ttf")

    private fun directory(context: Context) = File(context.filesDir, "fonts")

    private fun font(file: File) = CustomFont(
        value = PREFIX + file.name,
        name = file.nameWithoutExtension.substringAfter('_')
    )

    fun list(context: Context): List<CustomFont> = directory(context).listFiles()
        .orEmpty()
        .filter { fileNamePattern.matches(it.name) && it.isFile }
        .map(::font)
        .sortedBy { it.name.lowercase() }

    suspend fun typeface(context: Context, value: String): Typeface? = withContext(Dispatchers.IO) {
        readTypeface(context, value)
    }

    private fun readTypeface(context: Context, value: String): Typeface? {
        if (!value.startsWith(PREFIX)) return null
        val name = value.removePrefix(PREFIX)
        if (!fileNamePattern.matches(name)) return null
        val file = File(directory(context), name)
        return try {
            if (!file.isFile || file.length() !in 1..MAX_BYTES.toLong()) return null
            typefaces.get(file.absolutePath) ?: Typeface.Builder(file).build()?.also {
                typefaces.put(file.absolutePath, it)
            }
        } catch (_: Exception) {
            null // A missing or damaged restored font must never prevent the launcher opening.
        }
    }

    // Called on Dispatchers.IO. Copying avoids depending on a provider or a URI grant later.
    fun importFont(context: Context, uri: Uri): CustomFont {
        val resolver = context.contentResolver
        val displayName = resolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        } ?: throw FontImportException(R.string.font_import_invalid)
        if (!displayName.endsWith(".ttf", ignoreCase = true)) {
            throw FontImportException(R.string.font_import_invalid)
        }

        val dir = directory(context)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Cannot create font directory")
        val temporary = File.createTempFile("import-", ".tmp", dir)
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            resolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_BYTES) {
                            throw FontImportException(R.string.font_import_too_large)
                        }
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                    }
                }
            } ?: throw IOException("Cannot read font")

            // Check the TrueType signature as well as asking Android to parse the font.
            if (temporary.length() < 12) throw FontImportException(R.string.font_import_invalid)
            val signature = DataInputStream(temporary.inputStream()).use { it.readInt() }
            if (signature != 0x00010000 && signature != 0x74727565) {
                throw FontImportException(R.string.font_import_invalid)
            }
            val parsedTypeface = Typeface.Builder(temporary).build()
                ?: throw FontImportException(R.string.font_import_invalid)

            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            val existing = dir.listFiles().orEmpty()
                .firstOrNull { it.name.startsWith("${hash}_") && fileNamePattern.matches(it.name) }
            if (existing != null && readTypeface(context, PREFIX + existing.name) != null) {
                return font(existing)
            }

            val baseName = displayName.dropLast(4)
                .replace(Regex("[/\\\\\\p{Cntrl}]"), " ")
                .trim().take(40).ifBlank { "Font" }
            val names = dir.listFiles().orEmpty().filter { fileNamePattern.matches(it.name) }
                .map { font(it).name }.toSet()
            var name = baseName
            var suffix = 2
            while (name in names) name = "$baseName (${suffix++})"
            val destination = File(dir, "${hash}_$name.ttf")
            if (!temporary.renameTo(destination)) throw IOException("Cannot save font")
            typefaces.put(destination.absolutePath, parsedTypeface)
            return font(destination)
        } finally {
            temporary.delete()
        }
    }
}
