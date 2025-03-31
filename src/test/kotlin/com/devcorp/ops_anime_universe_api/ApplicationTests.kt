package com.devcorp.ops_anime_universe_api

import java.lang.reflect.Modifier
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest
@TestPropertySource(properties = ["spring.main.web-application-type=reactive"])
@Import(ApplicationTests.TestConfig::class)
@ActiveProfiles("test")
class ApplicationTests {

  @Configuration
  class TestConfig {
    @Bean
    @Primary
    fun testWebClientBuilder(): WebClient.Builder {
      return WebClient.builder()
    }
  }

  @Test
  fun `classe Application pode ser instanciada`() {
    // Arrange & Act
    val application = Application()

    // Assert
    assertNotNull(application)
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

  @Test
  fun `método main executa a aplicação corretamente`() {
    // Usando mockStatic para evitar que SpringApplication.run realmente inicie a aplicação
    mockStatic(SpringApplication::class.java).use { mockedStatic ->
      // Arrange - configurando o mock para retornar um mock do ApplicationContext
      val mockedContext =
              org.mockito.Mockito.mock(
                      org.springframework.context.ConfigurableApplicationContext::class.java
              )
      mockedStatic
              .`when`<Any> { SpringApplication.run(Application::class.java, *arrayOf<String>()) }
              .thenReturn(mockedContext)

      // Act - executando o método main
      main(arrayOf())

      // Assert - verificando que o método run foi chamado com os parâmetros corretos
      mockedStatic.verify { SpringApplication.run(Application::class.java, *arrayOf<String>()) }
    }
  }
}
