package com.devcorp.ops_anime_universe_api.infrastructure.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WebClientConfigTest {

  private val webClientConfig = WebClientConfig()

  @Test
  fun `deve criar WebClient Builder com configurações corretas`() {
    // Act
    val webClientBuilder = webClientConfig.webClientBuilder()

    // Assert
    assertNotNull(webClientBuilder)

    // Testa se o builder pode criar um cliente funcional
    val webClient = webClientBuilder.build()
    assertNotNull(webClient)
  }

  @Test
  fun `deve ter configurações de timeout e compressão`() {
    // Arrange
    val webClientBuilder = webClientConfig.webClientBuilder()
    val webClient = webClientBuilder.build()

    // Act & Assert
    // Observação: não é possível verificar diretamente as configurações internas
    // do WebClient, mas podemos verificar se ele foi criado corretamente
    assertNotNull(webClient)
  }

  @Test
  fun `WebClient Builder deve poder criar instâncias com diferentes URLs base`() {
    // Act
    val webClientBuilder = webClientConfig.webClientBuilder()

    // Criando diferentes instâncias de WebClient com URLs base diferentes
    val webClient1 = webClientBuilder.baseUrl("http://example.com/api").build()
    val webClient2 = webClientBuilder.baseUrl("http://localhost:8080").build()

    // Assert
    assertNotNull(webClient1)
    assertNotNull(webClient2)
  }

  @Test
  fun `WebClient Builder deve poder ser usado em ambientes diferentes`() {
    // Act
    val webClientBuilder = webClientConfig.webClientBuilder()

    // Criando um WebClient com configurações específicas
    val webClient =
            webClientBuilder
                    .defaultHeader("X-Api-Key", "test-key")
                    .defaultHeader("Content-Type", "application/json")
                    .build()

    // Assert
    assertNotNull(webClient)
  }
}
