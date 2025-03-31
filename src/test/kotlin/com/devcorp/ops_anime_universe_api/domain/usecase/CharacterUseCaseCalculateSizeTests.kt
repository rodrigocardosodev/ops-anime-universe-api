package com.devcorp.ops_anime_universe_api.domain.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/** Testes específicos para o método calculateSize da classe CharacterUseCase */
class CharacterUseCaseCalculateSizeTests {

  @Test
  fun `calculateSize should return 1 when input is less than 1`() {
    // Arrange - valores negativos e zero
    val sizes = listOf(-100, -10, -1, 0)

    // Act & Assert - todos devem retornar 1
    sizes.forEach { size ->
      val result = CharacterUseCase.calculateSize(size)
      assertEquals(1, result, "Para o valor de entrada $size, o resultado deveria ser 1")
    }
  }

  @Test
  fun `calculateSize should return input when between 1 and MAX_PAGE_SIZE`() {
    // Arrange - valores entre 1 e MAX_PAGE_SIZE (100)
    val validSizes = listOf(1, 10, 50, 99, 100)

    // Act & Assert - cada entrada deve retornar o mesmo valor
    validSizes.forEach { size ->
      val result = CharacterUseCase.calculateSize(size)
      assertEquals(size, result, "Para o valor de entrada $size, o resultado deveria ser $size")
    }
  }

  @Test
  fun `calculateSize should return MAX_PAGE_SIZE when input is greater than MAX_PAGE_SIZE`() {
    // Arrange - valores maiores que MAX_PAGE_SIZE (100)
    val largeSizes = listOf(101, 500, 1000, Int.MAX_VALUE)

    // Act & Assert - todos devem retornar MAX_PAGE_SIZE (100)
    largeSizes.forEach { size ->
      val result = CharacterUseCase.calculateSize(size)
      assertEquals(
              CharacterUseCase.MAX_PAGE_SIZE,
              result,
              "Para o valor de entrada $size, o resultado deveria ser ${CharacterUseCase.MAX_PAGE_SIZE}"
      )
    }
  }

  @ParameterizedTest
  @ValueSource(ints = [-5, 0, 1, 50, 100, 101, 500])
  fun `calculateSize should handle various inputs correctly`(size: Int) {
    // Arrange
    val expected =
            when {
              size < 1 -> 1
              size > CharacterUseCase.MAX_PAGE_SIZE -> CharacterUseCase.MAX_PAGE_SIZE
              else -> size
            }

    // Act
    val result = CharacterUseCase.calculateSize(size)

    // Assert
    assertEquals(
            expected,
            result,
            "Para o valor $size, esperava-se $expected, mas obteve-se $result"
    )
  }
}
