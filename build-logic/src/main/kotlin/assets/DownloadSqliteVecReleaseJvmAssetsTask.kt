package assets

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

abstract class DownloadSqliteVecReleaseJvmAssetsTask : DefaultTask() {
    @get:Inject
    abstract val fs: FileSystemOperations

    @get:Inject
    abstract val archives: ArchiveOperations

    @get:Input
    abstract val sqliteVecVersion: Property<String>

    @get:Input
    abstract val osArchPairs: ListProperty<Pair<String, String>>

    @get:OutputDirectory
    val destinationDirectory = project.layout.projectDirectory.dir("src/jvmMain/resources/asg017")

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun downloadAssets() {

        val releaseUrl = "http://github.com/asg017/sqlite-vec/releases/download/v${sqliteVecVersion.get()}"

        val tempExtractionDirectory = Files.createTempDirectory("sqlite-vec-downloads")

        try {
            osArchPairs.get().forEach { (os, arch) ->
                val downloadUrl = "$releaseUrl/sqlite-vec-${sqliteVecVersion.get()}-loadable-$os-$arch.tar.gz"
                val temporaryPath = tempExtractionDirectory.resolve("$os-$arch.tar.gz")
                val destinationPath = destinationDirectory.dir("$os/$arch")

                download(downloadUrl, temporaryPath)

                Files.createDirectories(destinationPath.asFile.toPath())
                fs.copy {
                    from(archives.tarTree(archives.gzip(temporaryPath)))
                    into(destinationPath)
                }
            }
        } finally {
            tempExtractionDirectory.deleteRecursively()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun download(downloadUrl: String, destination: Path) {
        val url = URI.create(downloadUrl).toURL()
        val connection = url.openConnection() as HttpURLConnection

        connection.connect()

        if (connection.responseCode in 300..<400) {
            download(connection.getHeaderField("Location"), destination)
        } else {
            connection.getInputStream().use { input ->
                Files.copy(input, destination)
            }
        }
    }
}
