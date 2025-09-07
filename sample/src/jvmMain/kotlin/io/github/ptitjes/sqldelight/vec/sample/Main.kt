package io.github.ptitjes.sqldelight.vec.sample

import io.github.ptitjes.sqldelight.vec.JdbcSqliteVecDriver

fun main() {
    JdbcSqliteVecDriver("jdbc:sqlite:", schema = TestDb.Schema).use { driver ->
        val database = TestDb(driver)
        val movieQueries = database.movieQueries

        val movies = listOf(
            1L to listOf(-0.200, 0.250, 0.341, -0.211, 0.645, 0.935, -0.316, -0.924),
            2L to listOf(0.443, -0.501, 0.355, -0.771, 0.707, -0.708, -0.185, 0.362),
            3L to listOf(0.716, -0.927, 0.134, 0.052, -0.669, 0.793, -0.634, -0.162),
            4L to listOf(-0.710, 0.330, 0.656, 0.041, -0.990, 0.726, 0.385, -0.958),
        )

        movies.forEach { (movieId, embedding) ->
            movieQueries.insertMovie(movieId = movieId, embedding = embedding)
        }

        val matching = movieQueries.matchingMovies(
            embedding = listOf(0.443, -0.501, 0.355, -0.771, 0.707, -0.708, -0.185, 0.362),
            minSimilarity = -0.5,
            count = null,
        )

        matching.executeAsList().forEach { println(it) }
    }
}
