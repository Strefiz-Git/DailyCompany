package com.dailycompany.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Formato do ficheiro (.zip):
 *   manifest.json      -> app, schemaVersion, appVersion, createdAt, contagens
 *   data.json          -> BackupData (todas as entidades, ligadas por uid)
 *   images/<uuid>.jpg  -> fotos originais e miniaturas referidas em data.json
 *
 * Regras:
 *  - As referências entre entidades usam uid (UUID), nunca ids auto-incrementados.
 *  - Validar tudo antes de escrever seja o que for na base de dados.
 *  - Importação transacional: ou entra tudo, ou não entra nada.
 *  - Proteção contra Zip Slip nos nomes das entradas.
 */

const val BACKUP_SCHEMA_VERSION = 1
private const val MANIFEST = "manifest.json"
private const val DATA = "data.json"
private const val IMAGES_DIR = "images/"

@Serializable
data class BackupManifest(
    val app: String = "DailyCompany",
    @SerialName("schema_version") val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    @SerialName("app_version") val appVersion: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val counts: Map<String, Int> = emptyMap(),
)

/** DTOs de backup: independentes das entidades Room, para refactors não partirem ficheiros antigos. */
@Serializable
data class BackupData(
    val categories: List<BackupCategory> = emptyList(),
    val items: List<BackupItem> = emptyList(),
    val outfits: List<BackupOutfit> = emptyList(),
    val tasks: List<BackupTask> = emptyList(),
    val events: List<BackupEvent> = emptyList(),
    val reminders: List<BackupReminder> = emptyList(),
    @SerialName("wear_logs") val wearLogs: List<BackupWearLog> = emptyList(),
)

@Serializable data class BackupCategory(
    val uid: String, val name: String, val nameKey: String? = null,
    val parentUid: String? = null, val slot: String? = null, val sortOrder: Int = 0,
)

@Serializable data class BackupItem(
    val uid: String, val name: String, val slot: String,
    val imageFile: String? = null, val thumbFile: String? = null,
    val sizeSystem: String = "LETTER", val sizeValue: String? = null, val sizeRank: Int? = null,
    val brand: String? = null, val colors: String = "", val seasons: String = "",
    val warmth: Int = 3, val formality: String = "CASUAL", val isWaterproof: Boolean = false,
    val notes: String? = null, val isFavorite: Boolean = false, val isAvailable: Boolean = true,
    val isArchived: Boolean = false, val lastWornDate: Long? = null, val timesWorn: Int = 0,
    val createdAt: Long = 0, val updatedAt: Long = 0,
    val categoryUids: List<String> = emptyList(),
)

@Serializable data class BackupOutfitItem(val itemUid: String, val slot: String)

@Serializable data class BackupOutfit(
    val uid: String, val name: String, val notes: String? = null, val formality: String? = null,
    val isFavorite: Boolean = false, val createdAt: Long = 0, val updatedAt: Long = 0,
    val lastWornDate: Long? = null, val timesWorn: Int = 0,
    val items: List<BackupOutfitItem> = emptyList(),
)

@Serializable data class BackupTask(
    val uid: String, val title: String, val note: String? = null,
    val dueDate: Long? = null, val dueTime: Int? = null, val priority: String = "NONE",
    val tag: String? = null, val isDone: Boolean = false, val doneAt: Long? = null,
    val recurrence: String? = null, val parentTaskUid: String? = null,
    val createdAt: Long = 0, val updatedAt: Long = 0,
)

@Serializable data class BackupEvent(
    val uid: String, val title: String, val date: Long, val time: Int? = null,
    val isAllDay: Boolean = true, val location: String? = null, val notes: String? = null,
    val colorKey: String? = null, val recurrence: String? = null,
    val recurrenceEnd: Long? = null, val recurrenceCount: Int? = null,
    val createdAt: Long = 0, val updatedAt: Long = 0,
)

@Serializable data class BackupReminder(
    val uid: String, val targetType: String, val targetUid: String? = null,
    val triggerAt: Long? = null, val offsetMinutes: Int? = null, val isEnabled: Boolean = true,
    val daysOfWeek: String? = null, val label: String? = null, val vibrate: Boolean = true,
)

@Serializable data class BackupWearLog(
    val uid: String, val date: Long, val outfitUid: String? = null,
    val itemUids: List<String> = emptyList(),
)

enum class ImportStrategy { MERGE, REPLACE }

sealed interface BackupResult {
    data class Success(
        val items: Int, val outfits: Int, val tasks: Int, val events: Int,
        val missingImages: Int = 0,
    ) : BackupResult

    enum class Error { INVALID_FILE, UNSUPPORTED_VERSION, IO, UNKNOWN }
    data class Failure(val error: Error, val cause: Throwable? = null) : BackupResult
}

/** Implementado na camada de dados: lê e escreve a base de dados. */
interface BackupRepository {
    suspend fun exportData(): BackupData
    /** Tem de correr dentro de UMA transação Room. Devolve as contagens efetivamente escritas. */
    suspend fun importData(data: BackupData, strategy: ImportStrategy): BackupResult.Success
    suspend fun clearAll()
}

@Singleton
class BackupManager @Inject constructor(
    private val context: Context,
    private val repository: BackupRepository,
    private val imagesDir: File,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true      // ficheiros de versões futuras não rebentam o parser
        encodeDefaults = true
    }

    fun suggestedFileName(): String {
        val stamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm"))
        return "dailycompany-backup-$stamp.zip"
    }

    // ---------------------------------------------------------------- export

    suspend fun export(target: Uri, appVersion: String): BackupResult = withContext(ioDispatcher) {
        runCatching {
            val data = repository.exportData()
            var missingImages = 0

            val manifest = BackupManifest(
                appVersion = appVersion,
                createdAt = Instant.now().toString(),
                counts = mapOf(
                    "items" to data.items.size,
                    "outfits" to data.outfits.size,
                    "tasks" to data.tasks.size,
                    "events" to data.events.size,
                    "categories" to data.categories.size,
                ),
            )

            val output: OutputStream = context.contentResolver.openOutputStream(target)
                ?: error("Não foi possível abrir o ficheiro de destino")

            ZipOutputStream(output.buffered()).use { zip ->
                zip.writeText(MANIFEST, json.encodeToString(BackupManifest.serializer(), manifest))
                zip.writeText(DATA, json.encodeToString(BackupData.serializer(), data))

                val photoNames = data.items.flatMap { listOfNotNull(it.imageFile, it.thumbFile) }.distinct()
                for (name in photoNames) {
                    val file = File(imagesDir, name)
                    if (!file.exists()) {
                        missingImages++
                        continue
                    }
                    zip.putNextEntry(ZipEntry(IMAGES_DIR + name))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }

            BackupResult.Success(
                items = data.items.size, outfits = data.outfits.size,
                tasks = data.tasks.size, events = data.events.size,
                missingImages = missingImages,
            )
        }.getOrElse { BackupResult.Failure(BackupResult.Error.IO, it) }
    }

    // ---------------------------------------------------------------- import

    /** Lê e valida sem tocar na base de dados. Usa isto para mostrar o diálogo de estratégia. */
    suspend fun peek(source: Uri): Result<Pair<BackupManifest, BackupData>> = withContext(ioDispatcher) {
        runCatching {
            var manifest: BackupManifest? = null
            var data: BackupData? = null

            openZip(source) { entry, stream ->
                when (entry.name) {
                    MANIFEST -> manifest = json.decodeFromString(
                        BackupManifest.serializer(), stream.readBytes().decodeToString()
                    )
                    DATA -> data = json.decodeFromString(
                        BackupData.serializer(), stream.readBytes().decodeToString()
                    )
                }
            }

            val m = manifest ?: error("manifest em falta")
            val d = data ?: error("data em falta")
            require(m.app == "DailyCompany") { "ficheiro de outra app" }
            require(m.schemaVersion <= BACKUP_SCHEMA_VERSION) { "versão não suportada" }
            m to d
        }
    }

    suspend fun import(
        source: Uri,
        strategy: ImportStrategy,
    ): BackupResult = withContext(ioDispatcher) {
        val peeked = peek(source).getOrElse { throwable ->
            val error = when {
                throwable.message?.contains("versão não suportada") == true ->
                    BackupResult.Error.UNSUPPORTED_VERSION
                else -> BackupResult.Error.INVALID_FILE
            }
            return@withContext BackupResult.Failure(error, throwable)
        }
        val (_, data) = peeked

        // 1) extrair fotos para uma pasta temporária (nada é escrito no destino final ainda)
        val temp = File(context.cacheDir, "restore-${System.currentTimeMillis()}").apply { mkdirs() }
        var missingImages = 0
        try {
            val extracted = mutableSetOf<String>()
            openZip(source) { entry, stream ->
                if (entry.isDirectory || !entry.name.startsWith(IMAGES_DIR)) return@openZip
                val safeName = entry.name.removePrefix(IMAGES_DIR)
                if (safeName.isBlank() || safeName.contains("..") ||
                    safeName.contains('/') || safeName.contains('\\')
                ) return@openZip                       // proteção contra Zip Slip
                File(temp, safeName).outputStream().use { stream.copyTo(it) }
                extracted += safeName
            }
            val referenced = data.items.flatMap { listOfNotNull(it.imageFile, it.thumbFile) }.toSet()
            missingImages = (referenced - extracted).size

            // 2) base de dados, numa transação
            if (strategy == ImportStrategy.REPLACE) repository.clearAll()
            val written = repository.importData(data, strategy)

            // 3) só agora as fotos vão para o sítio definitivo
            imagesDir.mkdirs()
            if (strategy == ImportStrategy.REPLACE) {
                imagesDir.listFiles()?.forEach { it.delete() }
            }
            temp.listFiles()?.forEach { file ->
                file.copyTo(File(imagesDir, file.name), overwrite = true)
            }

            written.copy(missingImages = missingImages)
        } catch (t: Throwable) {
            BackupResult.Failure(BackupResult.Error.UNKNOWN, t)
        } finally {
            temp.deleteRecursively()
        }
    }

    // ---------------------------------------------------------------- helpers

    private inline fun openZip(source: Uri, block: (ZipEntry, InputStream) -> Unit) {
        val input = context.contentResolver.openInputStream(source)
            ?: error("Não foi possível abrir o ficheiro")
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                block(entry, zip)
                zip.closeEntry()
            }
        }
    }

    private fun ZipOutputStream.writeText(name: String, text: String) {
        putNextEntry(ZipEntry(name))
        write(text.toByteArray())
        closeEntry()
    }
}
