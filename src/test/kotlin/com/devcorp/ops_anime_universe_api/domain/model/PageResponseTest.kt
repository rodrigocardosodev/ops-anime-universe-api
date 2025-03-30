package com.devcorp.ops_anime_universe_api.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PageResponseTest {

  @Test
  fun `of should calculate totalPages correctly when remainder is zero`() {
    // Arrange
    val content = listOf("A", "B", "C")
    val page = 0
    val size = 3
    val totalElements = 9L

    // Act
    val result = PageResponse.of(content, page, size, totalElements)

    // Assert
    assertEquals(3, result.totalPages) // 9 / 3 = 3 páginas completas
    assertEquals(content, result.content)
    assertEquals(page, result.page)
    assertEquals(size, result.size)
    assertEquals(totalElements, result.totalElements)
  }

  @Test
  fun `of should calculate totalPages correctly when there is a remainder`() {
    // Arrange
    val content = listOf("A", "B")
    val page = 0
    val size = 3
    val totalElements = 8L // Não divisível perfeitamente por 3

    // Act
    val result = PageResponse.of(content, page, size, totalElements)

    // Assert
    assertEquals(3, result.totalPages) // 8 / 3 = 2 com resto, então 3 páginas
    assertEquals(content, result.content)
    assertEquals(page, result.page)
    assertEquals(size, result.size)
    assertEquals(totalElements, result.totalElements)
  }

  @Test
  fun `of should handle zero size and return one page`() {
    // Arrange
    val content = listOf("A", "B")
    val page = 0
    val size = 0 // Tamanho zero
    val totalElements = 5L

    // Act
    val result = PageResponse.of(content, page, size, totalElements)

    // Assert
    assertEquals(1, result.totalPages) // Divisão por zero deve retornar 1 página
    assertEquals(content, result.content)
    assertEquals(page, result.page)
    assertEquals(size, result.size)
    assertEquals(totalElements, result.totalElements)
  }

  @Test
  fun `of should handle zero totalElements and return zero pages`() {
    // Arrange
    val content = emptyList<String>()
    val page = 0
    val size = 10
    val totalElements = 0L // Zero elementos

    // Act
    val result = PageResponse.of(content, page, size, totalElements)

    // Assert
    assertEquals(0, result.totalPages) // Sem elementos, zero páginas
    assertEquals(content, result.content)
    assertEquals(page, result.page)
    assertEquals(size, result.size)
    assertEquals(totalElements, result.totalElements)
  }
}
