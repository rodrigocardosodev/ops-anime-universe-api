package com.devcorp.ops_anime_universe_api.infrastructure.config

import java.net.URI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.ServerRequest

/** Testes unitários para CustomErrorAttributes */
class CustomErrorAttributesTest {

  private lateinit var customErrorAttributes: CustomErrorAttributes
  private lateinit var mockRequest: ServerRequest

  @BeforeEach
  fun setup() {
    customErrorAttributes = CustomErrorAttributes()
    mockRequest = MockServerRequest.builder().uri(URI.create("/api/recurso-inexistente")).build()
  }

  /** Verifica se a classe CustomErrorAttributes está configurada corretamente */
  @Test
  fun `deve criar CustomErrorAttributes corretamente`() {
    // Act & Assert
    assertNotNull(customErrorAttributes)
    assertTrue(customErrorAttributes is DefaultErrorAttributes)
  }

  /** Verifica se o método getErrorAttributes está sobrescrito */
  @Test
  fun `deve ter o método getErrorAttributes sobrescrito`() {
    // Act & Assert
    val methods = CustomErrorAttributes::class.java.declaredMethods
    val hasGetErrorAttributes =
            methods.any {
              it.name == "getErrorAttributes" &&
                      it.parameterCount == 2 &&
                      it.parameters[0].type == ServerRequest::class.java &&
                      it.parameters[1].type == ErrorAttributeOptions::class.java
            }

    assertTrue(hasGetErrorAttributes)
  }
}
