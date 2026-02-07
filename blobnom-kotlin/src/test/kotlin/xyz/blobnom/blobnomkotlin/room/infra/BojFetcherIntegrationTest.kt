package xyz.blobnom.blobnomkotlin.room.infra

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xyz.blobnom.blobnomkotlin.room.infra.external.BojProblemFetcher

@SpringBootTest
class BojFetcherIntegrationTest {
    @Autowired
    private lateinit var bojProblemFetcher: BojProblemFetcher

    @Test
    fun testFetch() = runBlocking {
        // When
        val result = bojProblemFetcher.fetch("욘세이대학교", 1)

        // Then
        println(result)
        assert(result.isNotEmpty())
        assert(result[0].id == "34827")
    }
}