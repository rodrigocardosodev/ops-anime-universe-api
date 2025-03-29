package com.devcorp.ops_anime_universe_api.infrastructure.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import java.time.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider

/**
 * Configuração do WebClient para chamadas a APIs externas com otimizações de performance e
 * resiliência
 */
@Configuration
class WebClientConfig {
        private val logger = LoggerFactory.getLogger(WebClientConfig::class.java)

        // Tamanho do buffer de memória em MB
        private val maxInMemorySize = 16 * 1024 * 1024

        // Configurações de timeout padrão em millisegundos
        private val connectTimeoutMs = 10000
        private val readTimeoutSeconds = 30L
        private val writeTimeoutSeconds = 30L
        private val responseTimeoutMs = 30000L

        // Configurações de pool de conexões
        private val maxConnections = 500
        private val maxIdleTimeSeconds = 60L
        private val pendingAcquireTimeoutSeconds = 30L
        private val evictInBackgroundSeconds = 120L

        /**
         * Configura o WebClient.Builder com memória otimizada para manipular respostas grandes e
         * configurações de performance
         */
        @Bean
        fun webClientBuilder(): WebClient.Builder {
                // Aumenta o tamanho máximo de memória para o buffer do WebClient
                val exchangeStrategies =
                        ExchangeStrategies.builder()
                                .codecs { configurer ->
                                        configurer.defaultCodecs().maxInMemorySize(maxInMemorySize)
                                }
                                .build()

                // Configura o ConnectionProvider com pool otimizado
                val provider =
                        ConnectionProvider.builder("anime-universe-pool")
                                .maxConnections(maxConnections)
                                .maxIdleTime(Duration.ofSeconds(maxIdleTimeSeconds))
                                .pendingAcquireTimeout(
                                        Duration.ofSeconds(pendingAcquireTimeoutSeconds)
                                )
                                .evictInBackground(Duration.ofSeconds(evictInBackgroundSeconds))
                                .build()

                // Cria um HttpClient com timeout configurado e outras otimizações
                val httpClient =
                        HttpClient.create(provider)
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                                .option(ChannelOption.SO_KEEPALIVE, true)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                                .doOnConnected { conn ->
                                        conn.addHandlerFirst(
                                                        "readTimeout",
                                                        io.netty.handler.timeout.ReadTimeoutHandler(
                                                                readTimeoutSeconds,
                                                                TimeUnit.SECONDS
                                                        )
                                                )
                                                .addHandlerFirst(
                                                        "writeTimeout",
                                                        io.netty.handler.timeout
                                                                .WriteTimeoutHandler(
                                                                        writeTimeoutSeconds,
                                                                        TimeUnit.SECONDS
                                                                )
                                                )
                                }
                                .compress(
                                        true
                                ) // Adiciona compressão para reduzir o tamanho das respostas

                return WebClient.builder()
                        .clientConnector(ReactorClientHttpConnector(httpClient))
                        .exchangeStrategies(exchangeStrategies)
                        .filter(logRequest())
        }

        /** Cria um filtro para logar requisições (útil para debug) */
        private fun logRequest(): ExchangeFilterFunction {
                return ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                        if (logger.isDebugEnabled) {
                                logger.debug(
                                        "Request: {} {}",
                                        clientRequest.method(),
                                        clientRequest.url()
                                )
                        }
                        Mono.just(clientRequest)
                }
        }
}
