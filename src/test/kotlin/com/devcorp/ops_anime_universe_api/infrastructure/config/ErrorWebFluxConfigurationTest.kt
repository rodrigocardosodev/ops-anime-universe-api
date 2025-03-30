package com.devcorp.ops_anime_universe_api.infrastructure.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.web.WebProperties

/** Testes unitários para ErrorWebFluxConfiguration */
class ErrorWebFluxConfigurationTest {

  /** Testa a criação do bean de resources */
  @Test
  fun `deve criar resources corretamente`() {
    // Arrange
    val config = ErrorWebFluxConfiguration()

    // Act
    val resources = config.resources()

    // Assert
    assertNotNull(resources)
    assertEquals(WebProperties.Resources::class.java, resources.javaClass)
  }

  /** Testa a criação do bean de serverCodecConfigurer */
  @Test
  fun `deve criar serverCodecConfigurer corretamente`() {
    // Arrange
    val config = ErrorWebFluxConfiguration()

    // Act
    val codecConfigurer = config.serverCodecConfigurer()

    // Assert
    assertNotNull(codecConfigurer)
    assertNotNull(codecConfigurer.readers)
    assertNotNull(codecConfigurer.writers)
  }
}
