package io.github.ptitjes.sqldelight.vec

import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Suppress("FunctionName")
fun JdbcSqliteVecDriver(
    /**
     * Database connection URL in the form of `jdbc:sqlite:path?key1=value1&...` where:
     * - `jdbc:sqlite:` is the prefix which instructs [DriverManager] to open a connection
     *   using the provided [org.sqlite.JDBC] Driver.
     * - `path` is a file path which instructs sqlite *where* it should open the database
     *   connection.
     * - `?key1=value1&...` is an optional query string which instruct sqlite *how* it
     *   should open the connection.
     *
     * Examples:
     * - `jdbc:sqlite:/path/to/myDatabase.db` opens a database connection, writing changes
     *   to the filesystem at the specified `path`.
     * - `jdbc:sqlite:` (i.e. an empty path) will create a temporary database whereby the
     *   temp file is deleted upon connection closure.
     * - `jdbc:sqlite::memory:` will create a purely in-memory database.
     * - `jdbc:sqlite:file:memdb1?mode=memory&cache=shared` will create a named in-memory
     *   database which can be shared across connections until all are closed.
     *
     * [sqlite.org/inmemorydb](https://www.sqlite.org/inmemorydb.html)
     */
    url: String,
    properties: Properties = Properties(),
): JdbcDriver {
    val propertiesWithLoadExtensions = Properties(properties).apply { set("enable_load_extension", "true") }
    val driver = JdbcSqliteDriver(url, propertiesWithLoadExtensions).withSqliteVec(url)
    return driver
}

private fun JdbcDriver.withSqliteVec(url: String): JdbcDriver {
    val path = url.substringBefore('?').substringAfter("jdbc:sqlite:")
    val isStatic = path.isEmpty() ||
            path == ":memory:" ||
            path == "file::memory:" ||
            path.startsWith(":resource:") ||
            url.contains("mode=memory")

    return when {
        isStatic -> this.apply { getConnection().also { with(SqliteVec) { it.loadSqliteVecModule() } } }
        else -> SqliteVecDriver(this)
    }
}

@OptIn(ExperimentalAtomicApi::class)
private class SqliteVecDriver(
    private val delegate: JdbcDriver,
) : JdbcDriver() {

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
        delegate.addListener(*queryKeys, listener = listener)
    }

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
        delegate.removeListener(*queryKeys, listener = listener)
    }

    override fun notifyListeners(vararg queryKeys: String) {
        delegate.notifyListeners(*queryKeys)
    }

    override fun getConnection(): Connection {
        return delegate.getConnection().also { with(SqliteVec) { it.loadSqliteVecModule() } }
    }

    override fun closeConnection(connection: Connection) {
        delegate.closeConnection(connection)
    }

    override fun close() {
        delegate.close()
    }
}
