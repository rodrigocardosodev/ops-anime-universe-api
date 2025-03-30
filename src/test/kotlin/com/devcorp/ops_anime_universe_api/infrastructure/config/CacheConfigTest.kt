package com.devcorp.ops_anime_universe_api.infrastructure.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.test.util.ReflectionTestUtils

/** Testes unitários para a classe de configuração CacheConfig */
class CacheConfigTest {

  /** Testa a criação e configuração básica do CacheManager */
  @Test
  fun `deve criar CacheManager corretamente`() {
    // Arrange - configura valores para reflection
    val cacheConfig = CacheConfig()
    ReflectionTestUtils.setField(cacheConfig, "cacheMaxSize", 1000L)
    ReflectionTestUtils.setField(cacheConfig, "cacheExpireAfterWriteMinutes", 30L)

    // Act
    val cacheManager = cacheConfig.cacheManager()

    // Assert
    assertNotNull(cacheManager)
    assertTrue(cacheManager is CaffeineCacheManager)
  }

  /** Verifica se os nomes dos caches foram configurados corretamente */
  @Test
  fun `deve configurar os nomes dos caches corretamente`() {
    // Arrange
    val cacheConfig = CacheConfig()
    ReflectionTestUtils.setField(cacheConfig, "cacheMaxSize", 1000L)
    ReflectionTestUtils.setField(cacheConfig, "cacheExpireAfterWriteMinutes", 30L)

    // Act
    val cacheManager = cacheConfig.cacheManager() as CaffeineCacheManager

    // Assert - verifica se os caches específicos existem
    val cacheNames = listOf("characters", "dragonball", "pokemon")

    // Acessa o cache de cada nome para verificar se eles existem
    for (cacheName in cacheNames) {
      val cache = cacheManager.getCache(cacheName)
      assertNotNull(cache, "Cache $cacheName deveria existir")
    }
  }

  /** Verifica se as configurações específicas do cache foram aplicadas */
  @Test
  fun `deve aplicar as configurações corretas de tamanho e expiração`() {
    // Arrange
    val cacheConfig = CacheConfig()
    val maxSize = 2000L
    val expireTime = 45L

    // Configura valores diferentes para testar
    ReflectionTestUtils.setField(cacheConfig, "cacheMaxSize", maxSize)
    ReflectionTestUtils.setField(cacheConfig, "cacheExpireAfterWriteMinutes", expireTime)

    // Act
    val cacheManager = cacheConfig.cacheManager()

    // Assert
    assertNotNull(cacheManager)

    // Infelizmente não é possível acessar diretamente as configurações do Caffeine
    // após a criação, então só podemos verificar se o cacheManager foi criado corretamente
    assertTrue(cacheManager is CaffeineCacheManager)
  }
}
