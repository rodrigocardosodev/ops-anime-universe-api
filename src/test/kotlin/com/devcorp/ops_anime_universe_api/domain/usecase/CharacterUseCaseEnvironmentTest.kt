package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CharacterUseCaseEnvironmentTest {

  @Mock private lateinit var characterService: CharacterService

  /** Classe de teste que estende CharacterUseCase para testar métodos protegidos */
  class TestableCharacterUseCase(services: List<CharacterService>) : CharacterUseCase(services) {
    // Expõe o método protegido para testes
    public override fun isTestEnvironment(): Boolean {
      return super.isTestEnvironment()
    }
  }

  @Test
  fun `isTestEnvironment should return true when running in test environment`() {
    // Arrange
    val characterUseCase = TestableCharacterUseCase(listOf(characterService))

    // Act
    val result = characterUseCase.isTestEnvironment()

    // Assert - Quando executado em ambiente de teste, deve retornar true
    assertTrue(result)
  }
}
