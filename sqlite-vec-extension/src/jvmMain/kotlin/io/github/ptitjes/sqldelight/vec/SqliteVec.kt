package io.github.ptitjes.sqldelight.vec

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.sql.Connection

object SqliteVec {
    fun Connection.loadSqliteVecModule() {
        createStatement().use { statement ->
            statement.execute("SELECT load_extension('${libraryPath}')")
        }
    }

    private val libraryPath = extractLibrary(System.getProperty("java.io.tmpdir"))

    private fun extractLibrary(temporaryDirectory: String): Path {
        val libraryPath = getSqliteVecLibraryPath()

        val resourcePath = "asg017/$libraryPath"

        val extractedLibraryPath = Paths.get(temporaryDirectory, resourcePath)

        Files.createDirectories(extractedLibraryPath.parent)

        try {
            val resource = SqliteVec::class.java.getResourceAsStream("/$resourcePath")!!
            resource.use { input ->
                Files.copy(input, extractedLibraryPath, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            extractedLibraryPath.toFile().deleteOnExit()
        }

        extractedLibraryPath.toFile().setReadable(true)
        extractedLibraryPath.toFile().setExecutable(true)

        return extractedLibraryPath
    }

    private fun getSqliteVecLibraryPath(): String {
        val osName = getOsName()
        val archName = getArchName()
        val libraryName = System.mapLibraryName("vec0").removePrefix("lib")
        return "$osName/$archName/$libraryName"
    }

    private fun getOsName(): String {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("windows") -> "windows"
            osName.contains("mac") || osName.contains("darwin") -> "macos"
            osName.contains("linux") -> "linux"
            else -> throw UnsupportedOperationException("Unsupported OS: $osName")
        }
    }

    private fun getArchName(): String {
        val archName = System.getProperty("os.arch").lowercase()
        return when (archName) {
            "aarch64" -> "aarch64"
            "amd64", "x86_64" -> "x86_64"
            else -> throw UnsupportedOperationException("Unsupported architecture: $archName")
        }
    }
}
