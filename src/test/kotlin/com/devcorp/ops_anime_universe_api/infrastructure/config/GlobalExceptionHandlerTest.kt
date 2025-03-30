package com.devcorp.ops_anime_universe_api.infrastructure.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

class GlobalExceptionHandlerTest {

  private val exceptionHandler = GlobalExceptionHandler()

  @Test
  fun `deve tratar exceção genérica corretamente`() {
    // Arrange
    val exception = RuntimeException("Erro de teste")

    // Act
    val responseEntity = exceptionHandler.handleException(exception)

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.status)
    assertEquals("Erro de teste", errorResponse.message)
  }

  @Test
  fun `deve tratar exceção genérica sem mensagem corretamente`() {
    // Arrange
    val exception = RuntimeException() // Sem mensagem de erro

    // Act
    val responseEntity = exceptionHandler.handleException(exception)

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.status)
    assertEquals("Erro interno no servidor", errorResponse.message)
  }

  @Test
  fun `deve tratar ResponseStatusException corretamente`() {
    // Arrange
    val statusException = ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado")

    // Act
    val responseEntity = exceptionHandler.handleResponseStatusException(statusException)

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.status)
    assertTrue(errorResponse.message.contains("Recurso não encontrado"))
  }

  @Test
  fun `deve tratar ResponseStatusException sem mensagem corretamente`() {
    // Arrange
    val statusException = ResponseStatusException(HttpStatus.NOT_FOUND) // Sem mensagem de erro

    // Act
    val responseEntity = exceptionHandler.handleResponseStatusException(statusException)

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.status)
    assertTrue(errorResponse.message.contains(HttpStatus.NOT_FOUND.toString()))
  }

  @Test
  fun `deve tratar HttpRequestMethodNotSupportedException corretamente`() {
    // Arrange
    // Utilizando o método mock para evitar o construtor privado
    val exception = mock(HttpRequestMethodNotSupportedException::class.java)
    `when`(exception.method).thenReturn("PUT")
    `when`(exception.supportedMethods).thenReturn(arrayOf("GET", "POST"))

    // Act
    val responseEntity = exceptionHandler.handleMethodNotSupported(exception)

    // Assert
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), errorResponse.status)
    assertTrue(errorResponse.message.contains("PUT"))
    assertTrue(errorResponse.message.contains("GET, POST"))
  }

  @Test
  fun `deve tratar HttpRequestMethodNotSupportedException com métodos suportados nulos`() {
    // Arrange
    val exception = mock(HttpRequestMethodNotSupportedException::class.java)
    `when`(exception.method).thenReturn("PUT")
    `when`(exception.supportedMethods).thenReturn(null) // Sem métodos suportados

    // Act
    val responseEntity = exceptionHandler.handleMethodNotSupported(exception)

    // Assert
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), errorResponse.status)
    assertTrue(errorResponse.message.contains("PUT"))
    assertFalse(errorResponse.message.contains("métodos suportados:"))
  }

  @Test
  fun `deve tratar WebClientResponseException corretamente`() {
    // Arrange
    val webClientException = mock(WebClientResponseException::class.java)
    `when`(webClientException.statusCode).thenReturn(HttpStatus.BAD_GATEWAY)
    `when`(webClientException.statusText).thenReturn("Bad Gateway")

    // Act
    val responseEntity = exceptionHandler.handleWebClientException(webClientException)

    // Assert
    assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.BAD_GATEWAY.value(), errorResponse.status)
    assertTrue(errorResponse.message.contains("Bad Gateway"))
  }

  @Test
  fun `deve tratar outras WebClientException corretamente`() {
    // Arrange
    // Use uma implementação genérica de WebClientException, não WebClientResponseException
    val webClientException = mock(WebClientException::class.java)
    `when`(webClientException.message).thenReturn("Erro de conexão")

    // Act
    val responseEntity = exceptionHandler.handleWebClientException(webClientException)

    // Assert
    assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.BAD_GATEWAY.value(), errorResponse.status)
    assertTrue(errorResponse.message.contains("Erro de conexão"))
  }

  @Test
  fun `deve tratar WebClientException sem mensagem corretamente`() {
    // Arrange
    val webClientException = mock(WebClientException::class.java)
    `when`(webClientException.message).thenReturn(null) // Sem mensagem de erro

    // Act
    val responseEntity = exceptionHandler.handleWebClientException(webClientException)

    // Assert
    assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.statusCode)
    val errorResponse = responseEntity.body!!
    assertEquals(HttpStatus.BAD_GATEWAY.value(), errorResponse.status)
    assertEquals("Erro ao chamar API externa: null", errorResponse.message)
  }
}
