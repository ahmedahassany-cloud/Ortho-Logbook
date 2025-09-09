package com.rcsed.orthologbook

import android.content.Context
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.client.http.FileContent
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DriveSyncManager(private val context: Context, private val driveService: Drive) {

    suspend fun backupToDrive() {
        val backupManager = BackupManager(context)
        val backupDir = backupManager.exportData()

        // Create ZIP
        val zipFile = java.io.File(context.cacheDir, "backup_package.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            backupDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = file.relativeTo(backupDir).path
                    zos.putNextEntry(ZipEntry(entryName))
                    file.inputStream().copyTo(zos)
                    zos.closeEntry()
                }
            }
        }

        // Upload ZIP to Google Drive
        val metadata = File()
            .setName("ortho_logbook_backup.zip")
            .setMimeType("application/zip")

        val fileContent = FileContent("application/zip", zipFile)
        driveService.files().create(metadata, fileContent).execute()
    }

    suspend fun restoreFromDrive(fileId: String) {
        val outputZip = java.io.File(context.cacheDir, "restore_package.zip")
        driveService.files().get(fileId).executeMediaAndDownloadTo(FileOutputStream(outputZip))

        // Unzip
        val restoreDir = java.io.File(context.cacheDir, "restore_tmp").apply { mkdirs() }
        ZipInputStream(FileInputStream(outputZip)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = java.io.File(restoreDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    newFile.outputStream().use { zis.copyTo(it) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        // Import data
        val backupManager = BackupManager(context)
        backupManager.importData(restoreDir)
    }
}
