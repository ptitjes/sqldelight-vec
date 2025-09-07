package io.github.ptitjes.sqldelight.vec

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class JdbcSqliteVecDriverTest {

    @Test
    fun `simple memory sqlite with vec extension test`() {
        JdbcSqliteVecDriver("jdbc:sqlite::memory:").use { driver ->
            driver.executeTestWithVecExtension()
        }
    }

    @Test
    fun `simple file sqlite with vec extension test`() {
        val path = Files.createTempFile("test", ".db")
        try {
            JdbcSqliteVecDriver("jdbc:sqlite:$path").use { driver ->
                driver.executeTestWithVecExtension()
            }
        } finally {
            Files.deleteIfExists(path)
        }
    }

    private fun JdbcDriver.executeTestWithVecExtension() {
        execute(
            identifier = null,
            sql = """
                        CREATE VIRTUAL TABLE movie USING vec0 (
                            movie_id integer primary key,
                            synopsis_embedding float[8]
                        );
                    """.trimIndent(),
            parameters = 0,
        )

        execute(
            identifier = null,
            sql = """
                        INSERT INTO movie (movie_id, synopsis_embedding)
                        VALUES
                            (1, '[-0.200, 0.250, 0.341, -0.211, 0.645, 0.935, -0.316, -0.924]'),
                            (2, '[0.443, -0.501, 0.355, -0.771, 0.707, -0.708, -0.185, 0.362]'),
                            (3, '[0.716, -0.927, 0.134, 0.052, -0.669, 0.793, -0.634, -0.162]'),
                            (4, '[-0.710, 0.330, 0.656, 0.041, -0.990, 0.726, 0.385, -0.958]');
                    """.trimIndent(),
            parameters = 0,
        )

        val embedding = listOf(0.443, -0.501, 0.355, -0.771, 0.707, -0.708, -0.185, 0.362)
            .joinToString(prefix = "[", separator = ",", postfix = "]")

        val result = executeQuery(
            identifier = null,
            sql = """
                        SELECT
                            movie_id,
                            1.0 - vec_distance_cosine(?, synopsis_embedding) AS similarity
                        FROM movie
                        WHERE similarity >= ?
                        ORDER BY similarity DESC
                        LIMIT IFNULL(?, 10);
                    """.trimIndent(),
            parameters = 3,
            mapper = { cursor ->
                val result = mutableListOf<Pair<Long?, Double?>>()
                while (cursor.next().value) {
                    result.add(cursor.getLong(0) to cursor.getDouble(1))
                }
                QueryResult.Value(result)
            }
        ) {
            bindString(0, embedding)
            bindDouble(1, -0.5)
            bindLong(2, null)
        }

        assertEquals(
            listOf<Pair<Long?, Double?>>(
                2L to 1.0,
                3L to -0.07178997993469238,
                1L to -0.17035603523254395,
            ),
            result.value,
        )
    }
}
