package de.fitplan.app.core.data

import java.io.File

/** Speichert die Daten als JSON-Datei. Geschrieben wird über eine temporäre Datei, damit nie halbe Daten entstehen. */
class FileStorage(private val file: File) : DataStorage {

    override fun read(): String? {
        val tmp = tmpFile()
        // Absturz zwischen Schreiben und Umbenennen: die vollständige temporäre Datei übernehmen.
        if (!file.exists() && tmp.exists()) tmp.renameTo(file)
        return if (file.exists()) file.readText() else null
    }

    override fun write(content: String) {
        file.parentFile?.mkdirs()
        val tmp = tmpFile()
        tmp.writeText(content)
        if (!tmp.renameTo(file)) {
            file.delete()
            check(tmp.renameTo(file)) { "Daten konnten nicht gespeichert werden" }
        }
    }

    override fun backupCorrupt(content: String) {
        File(file.parentFile, "${file.nameWithoutExtension}-defekt-${System.currentTimeMillis()}.json").writeText(content)
    }

    private fun tmpFile() = File(file.parentFile, "${file.name}.tmp")
}
