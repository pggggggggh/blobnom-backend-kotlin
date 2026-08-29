package xyz.blobnom.blobnomkotlin

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
abstract class AbstractIntegrationTest {

    companion object {

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { TestcontainersConfig.postgres.jdbcUrl }
            registry.add("spring.datasource.username") { TestcontainersConfig.postgres.username }
            registry.add("spring.datasource.password") { TestcontainersConfig.postgres.password }
            registry.add("spring.data.redis.url") {
                "redis://${TestcontainersConfig.redis.host}:${TestcontainersConfig.redis.getMappedPort(6379)}"
            }
        }
    }
}
