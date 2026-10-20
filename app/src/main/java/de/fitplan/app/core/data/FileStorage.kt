package de.fitplan.app.core.data

import java.io.File
import java.io.FileOutputStream

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
        FileOutputStream(tmp).use { out ->
            out.write(content.toByteArray())
            // Erst auf den Datenträger bringen, dann umbenennen – sonst kann nach einem Stromausfall eine leere Datei bleiben.
            out.fd.sync()
        }
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
