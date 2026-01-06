package xyz.blobnom.blobnomkotlin.common.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig {
    val httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(5))

    @Bean
    fun webClient(): WebClient =
        WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { it.defaultCodecs().maxInMemorySize(50 * 1024 * 1024) }.build()
}