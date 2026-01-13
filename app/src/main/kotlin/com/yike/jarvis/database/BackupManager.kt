package com.yike.jarvis.database

import android.content.Context
import com.yike.jarvis.BuildConfig
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {

    private val dbName = "task_database_" + BuildConfig.BUILD_TYPE

    /**
     * 将数据库备份为一个 ZIP 文件流
     */
    fun exportDatabase(outputStream: OutputStream): Result<Unit> {
        return try {
            // 1. 关闭数据库以确保数据完整性并合并 WAL
            AppDatabase.closeDatabase()
            
            val dbFile = context.getDatabasePath(dbName)
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")
            
            // 2. 将相关的数据库文件打包进 ZIP
            ZipOutputStream(outputStream).use { zos ->
                listOf(dbFile, shmFile, walFile).forEach { file ->
                    if (file.exists()) {
                        zos.putNextEntry(ZipEntry(file.name))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 ZIP 文件流恢复数据库
     */
    fun importDatabase(inputStream: InputStream): Result<Unit> {
        return try {
            // 1. 关闭当前数据库连接
            AppDatabase.closeDatabase()
            
            val dbFile = context.getDatabasePath(dbName)
            val dbDir = dbFile.parentFile ?: throw IOException("Database directory not found")
            
            // 2. 解压并覆盖现有文件
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    // 确保文件名符合预期，防止路径遍历攻击 (虽然是备份文件，但也应保持习惯)
                    val fileName = File(entry.name).name
                    val targetFile = File(dbDir, fileName)
                    
                    targetFile.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
