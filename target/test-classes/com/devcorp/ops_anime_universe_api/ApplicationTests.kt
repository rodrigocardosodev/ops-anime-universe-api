package com.devcorp.ops_anime_universe_api

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest
@TestPropertySource(properties = ["spring.main.web-application-type=reactive"])
@Import(ApplicationTests.TestConfig::class)
class ApplicationTests {

  @Configuration
  class TestConfig {
    @Bean
    @Primary
    fun webClientBuilder(): WebClient.Builder {
      return Mockito.mock(WebClient.Builder::class.java)
    }
  }

  @Test
  fun `contexto da aplicação carrega corretamente`() {
    // Este teste verifica se o contexto da aplicação é carregado corretamente
    // Se o contexto for carregado sem erros, o teste passa
  }

  @Test
  fun `classe Application é instanciada corretamente`() {
    // Testa a instanciação da classe Application
    val application = Application()

    // Como a classe não tem métodos, apenas verificamos
    // que podemos instanciá-la sem erros
    assert(application != null)
  }

  @Test
  fun `Application deve ter anotação SpringBootApplication`() {
    // Verifica se a classe principal tem a anotação SpringBootApplication
    val annotations = Application::class.java.annotations

    // Verifica se a anotação SpringBootApplication está presente
    val hasSpringBootAnnotation =
            annotations.any { it.annotationClass.simpleName == "SpringBootApplication" }

    assert(hasSpringBootAnnotation)
  }
}
