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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class CustomFont(val value: String, val name: String)

/** The settings a custom font can be chosen for. */
enum class FontUse { App, Clock }

class FontImportException(val messageRes: Int) : IOException()

object CustomFonts {
    private const val PREFIX = "custom:"
    private const val EXTENSION = ".ttf"
    private const val MAX_BYTES = 10 * 1024 * 1024
    private const val MAX_NAME_LENGTH = 40
    private const val MAX_CACHED_TYPEFACES = 16
    private const val TEMPORARY_PREFIX = "import-"
    private const val TEMPORARY_SUFFIX = ".tmp"
    private val STALE_TEMPORARY_AGE_MS = TimeUnit.HOURS.toMillis(1)

    // A font file is identified by the hash of its contents: 32 bytes, written as 64 hex digits.
    private const val HASH_ALGORITHM = "SHA-256"
    private const val HASH_HEX_LENGTH = 64

    // A TrueType file starts with a 12-byte header. Its first four bytes say what kind of font
    // follows: version 1.0 for a regular TrueType font, or the letters "true" for an Apple one.
    private const val TRUETYPE_HEADER_BYTES = 12
    private const val TRUETYPE_VERSION_1 = 0x00010000
    private const val TRUETYPE_APPLE = 0x74727565

    // Keyed by file name, so a lookup never touches the disk.
    private val typefaces = LruCache<String, Typeface>(MAX_CACHED_TYPEFACES)

    // The fonts in use, as file name and typeface. They are also held here so that previewing many
    // fonts in settings cannot evict one and bring back the first-frame flash of the default font.
    private val activeTypefaces = ConcurrentHashMap<FontUse, Pair<String, Typeface>>()

    // A stored font is named "<hash of the file>_<name shown in the font list>.ttf".
    private val fileNamePattern =
        Regex("[a-f0-9]{$HASH_HEX_LENGTH}_[^/\\\\\\p{Cntrl}]+${Regex.escape(EXTENSION)}")

    // What a font's name cannot keep: path separators, control characters, and unpaired
    // surrogates (\p{Cs}), which cannot be stored in a file name intact.
    private val unsafeNameCharacters = Regex("[/\\\\\\p{Cntrl}\\p{Cs}]")

    private fun directory(context: Context) = File(context.filesDir, "fonts")

    private fun font(file: File) = CustomFont(
        value = PREFIX + file.name,
        name = file.nameWithoutExtension.substringAfter('_')
    )

    fun isCustom(value: String) = value.startsWith(PREFIX)

    private fun fileName(value: String): String? =
        value.removePrefix(PREFIX).takeIf { isCustom(value) && fileNamePattern.matches(it) }

    fun list(context: Context): List<CustomFont> {
        val files = directory(context).listFiles().orEmpty()
        deleteStaleTemporaryFiles(files)
        return files
            .filter { fileNamePattern.matches(it.name) && it.isFile }
            .map(::font)
            .sortedBy { it.name.lowercase() }
    }

    // A process killed mid-import leaves its copy behind. An import in progress keeps writing
    // to its copy, so only old ones are removed.
    private fun deleteStaleTemporaryFiles(files: Array<out File>) {
        val cutoff = System.currentTimeMillis() - STALE_TEMPORARY_AGE_MS
        files.filter {
            it.name.startsWith(TEMPORARY_PREFIX) && it.name.endsWith(TEMPORARY_SUFFIX) &&
                    it.lastModified() < cutoff
        }.forEach { it.delete() }
    }

    private fun loadedTypeface(name: String): Typeface? =
        activeTypefaces.values.firstOrNull { it.first == name }?.second ?: typefaces.get(name)

    // Safe on the main thread: returns the font only if it has already been loaded.
    fun cachedTypeface(value: String): Typeface? = fileName(value)?.let(::loadedTypeface)

    suspend fun typeface(context: Context, value: String): Typeface? {
        val name = fileName(value) ?: return null
        return loadedTypeface(name) ?: withContext(Dispatchers.IO) { readTypeface(context, name) }
    }

    // Loads [value] and keeps it loaded for as long as it is the font chosen for [use].
    suspend fun activate(context: Context, use: FontUse, value: String?) {
        val name = value?.let(::fileName)
        val typeface = value?.let { typeface(context, it) }
        if (name != null && typeface != null) activeTypefaces[use] = name to typeface
        else activeTypefaces.remove(use)
    }

    private fun readTypeface(context: Context, name: String): Typeface? {
        val file = File(directory(context), name)
        return try {
            if (!file.isFile || file.length() !in 1..MAX_BYTES.toLong()) return null
            typefaces.get(name) ?: Typeface.Builder(file).build()?.also { typefaces.put(name, it) }
        } catch (_: Exception) {
            null // A missing or damaged restored font must never prevent the launcher opening.
        }
    }

    // Called on Dispatchers.IO.
    fun delete(context: Context, value: String) {
        val name = fileName(value) ?: return
        val file = File(directory(context), name)
        if (file.exists() && !file.delete()) throw IOException("Cannot delete font")
        typefaces.remove(name)
        activeTypefaces.values.removeAll { it.first == name }
    }

    // Called on Dispatchers.IO. Copying avoids depending on a provider or a URI grant later.
    // [reservedNames] are labels already shown in the font list, which an import must not reuse.
    fun importFont(context: Context, uri: Uri, reservedNames: Collection<String>): CustomFont {
        val resolver = context.contentResolver
        // Some providers do not report a name; the signature check below still validates the file.
        val displayName = resolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
        if (displayName != null && !displayName.endsWith(EXTENSION, ignoreCase = true)) {
            throw FontImportException(R.string.font_import_invalid)
        }

        val dir = directory(context)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Cannot create font directory")
        val temporary = File.createTempFile(TEMPORARY_PREFIX, TEMPORARY_SUFFIX, dir)
        try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
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
            if (!isTrueType(temporary)) throw FontImportException(R.string.font_import_invalid)
            val parsedTypeface = Typeface.Builder(temporary).build()
                ?: throw FontImportException(R.string.font_import_invalid)

            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            val fontFiles = dir.listFiles().orEmpty().filter { fileNamePattern.matches(it.name) }
            val existing = fontFiles.firstOrNull { it.name.startsWith("${hash}_") }
            if (existing != null && readTypeface(context, existing.name) != null) {
                return font(existing)
            }

            val baseName = displayName.orEmpty().dropLast(EXTENSION.length)
                .replace(unsafeNameCharacters, " ")
                .trim().takeCodePoints(MAX_NAME_LENGTH).trim().ifBlank { "Font" }
            val takenNames = (fontFiles.map { font(it).name } + reservedNames)
                .mapTo(HashSet()) { it.lowercase() }
            var name = baseName
            var suffix = 2
            while (name.lowercase() in takenNames) name = "$baseName (${suffix++})"
            val destination = File(dir, "${hash}_$name$EXTENSION")
            if (!temporary.renameTo(destination)) throw IOException("Cannot save font")
            typefaces.put(destination.name, parsedTypeface)
            return font(destination)
        } finally {
            temporary.delete()
        }
    }

    private fun isTrueType(file: File): Boolean {
        if (file.length() < TRUETYPE_HEADER_BYTES) return false
        val signature = DataInputStream(file.inputStream()).use { it.readInt() }
        return signature == TRUETYPE_VERSION_1 || signature == TRUETYPE_APPLE
    }

    // Unlike take(), never splits a surrogate pair such as an emoji.
    private fun String.takeCodePoints(count: Int): String =
        if (codePointCount(0, length) <= count) this
        else substring(0, offsetByCodePoints(0, count))
}
