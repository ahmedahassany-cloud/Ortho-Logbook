package com.rcsed.orthologbook

import android.content.Context
import com.google.gson.Gson
import java.io.File

class BackupManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val gson = Gson()

    suspend fun exportData(): File {
        val backupDir = File(context.filesDir, "backup").apply { mkdirs() }

        val cases = db.caseDao().getAllCases()
        File(backupDir, "cases.json").writeText(gson.toJson(cases))

        val allPhotos = mutableListOf<PhotoEntity>()
        val allPdfs = mutableListOf<PdfEntity>()

        cases.forEach { case ->
            allPhotos.addAll(db.photoDao().getPhotosForCase(case.id))
            allPdfs.addAll(db.pdfDao().getPdfsForCase(case.id))
        }

        File(backupDir, "photos.json").writeText(gson.toJson(allPhotos))
        File(backupDir, "pdfs.json").writeText(gson.toJson(allPdfs))

        // Copy actual files
        val photosDir = File(backupDir, "photos").apply { mkdirs() }
        allPhotos.forEach { photo ->
            val src = File(photo.filePath)
            if (src.exists()) {
                src.copyTo(File(photosDir, photo.fileName), overwrite = true)
            }
        }

        val pdfsDir = File(backupDir, "pdfs").apply { mkdirs() }
        allPdfs.forEach { pdf ->
            val src = File(pdf.filePath)
            if (src.exists()) {
                src.copyTo(File(pdfsDir, pdf.fileName), overwrite = true)
            }
        }

        return backupDir
    }

    suspend fun importData(backupDir: File) {
        val casesJson = File(backupDir, "cases.json").readText()
        val cases = gson.fromJson(casesJson, Array<CaseEntity>::class.java).toList()
        db.caseDao().insertAll(cases)

        val photosJson = File(backupDir, "photos.json").readText()
        val photos = gson.fromJson(photosJson, Array<PhotoEntity>::class.java).toMutableList()

        val pdfsJson = File(backupDir, "pdfs.json").readText()
        val pdfs = gson.fromJson(pdfsJson, Array<PdfEntity>::class.java).toMutableList()

        val restoredPhotosDir = File(context.filesDir, "photos").apply { mkdirs() }
        photos.forEach { photo ->
            val src = File(backupDir, "photos/${photo.fileName}")
            val dest = File(restoredPhotosDir, photo.fileName)
            if (src.exists()) {
                src.copyTo(dest, overwrite = true)
                photo.filePath = dest.absolutePath
            }
        }
        db.photoDao().insertAll(photos)

        val restoredPdfsDir = File(context.filesDir, "pdfs").apply { mkdirs() }
        pdfs.forEach { pdf ->
            val src = File(backupDir, "pdfs/${pdf.fileName}")
            val dest = File(restoredPdfsDir, pdf.fileName)
            if (src.exists()) {
                src.copyTo(dest, overwrite = true)
                pdf.filePath = dest.absolutePath
            }
        }
        db.pdfDao().insertAll(pdfs)
    }
}
