package com.avito.report

import com.avito.android.Result
import java.io.File

interface ReportFileProvider {

    val rootDir: Lazy<File>

    /**
     * special string, that indicates that file should be uploaded
     * and string should be changed with url later by test runner
     */
    fun toUploadPlaceholder(file: File): String

    /**
     * @return filename with extension
     */
    fun fromUploadPlaceholder(placeholder: String): String?

    fun provideReportDir(): File

    fun provideReportFile(): File

    fun getFile(relativePath: String): File

    fun generateFile(name: String, extension: String, create: Boolean = false): Result<File>

    fun generateUniqueFile(extension: String, create: Boolean = false): Result<File>
}
