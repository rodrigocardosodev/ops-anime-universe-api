package com.devcorp.ops_anime_universe_api

import java.lang.reflect.Modifier
import kotlin.reflect.jvm.javaMethod
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

  @Test
  fun `método main existe no arquivo Application`() {
    // Em Kotlin, o método main é uma função de nível superior,
    // não um método dentro da classe Application
    val mainMethod = ::main.javaMethod

    // Verificar se o método é público e estático (como exigido para um método main)
    val isPublicStatic =
            mainMethod != null &&
                    Modifier.isPublic(mainMethod.modifiers) &&
                    Modifier.isStatic(mainMethod.modifiers)

    // Verificar se tem os parâmetros corretos
    val hasCorrectParameter =
            mainMethod != null &&
                    mainMethod.parameterCount == 1 &&
                    mainMethod.parameters[0].type == Array<String>::class.java

    assert(isPublicStatic && hasCorrectParameter) {
      "O método main não foi encontrado corretamente no arquivo Application.kt"
    }
  }
}
