package com.devcorp.ops_anime_universe_api.infrastructure.config

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuração do Cache usando Caffeine para otimizar o acesso a dados frequentemente requisitados
 * e reduzir a carga nas APIs externas.
 */
@Configuration
@EnableCaching
class CacheConfig {

  @Value("\${cache.max-size:1000}") private val cacheMaxSize: Long = 1000

  @Value("\${cache.expire-after-write-minutes:30}")
  private val cacheExpireAfterWriteMinutes: Long = 30

  /**
   * Configura o gerenciador de cache Caffeine com os parâmetros:
   * - maximumSize: número máximo de entradas no cache
   * - expireAfterWrite: tempo de expiração após a gravação
   * - recordStats: habilitação de estatísticas do cache
   */
  @Bean
  fun cacheManager(): CacheManager {
    val caffeineCacheManager = CaffeineCacheManager()
    caffeineCacheManager.setCaffeine(
            Caffeine.newBuilder()
                    .maximumSize(cacheMaxSize)
                    .expireAfterWrite(cacheExpireAfterWriteMinutes, TimeUnit.MINUTES)
                    .recordStats()
    )

    // Registra caches específicos
    caffeineCacheManager.setCacheNames(
            listOf(
                    "characters", // Cache para consultas de personagens
                    "dragonball", // Cache para consultas na API do Dragon Ball
                    "pokemon" // Cache para consultas na API do Pokemon
            )
    )

    return caffeineCacheManager
  }
}
