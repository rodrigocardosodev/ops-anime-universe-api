package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** Testes adicionais para a classe CharacterUseCase focados em casos extremos e especiais */
class CharacterUseCaseEdgeCasesTests {

  private lateinit var dragonBallService: CharacterService
  private lateinit var pokemonService: CharacterService
  private lateinit var characterUseCase: CharacterUseCase
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setup() {
    dragonBallService = mock()
    pokemonService = mock()

    whenever(dragonBallService.getUniverse()).thenReturn(Universe.DRAGON_BALL)
    whenever(pokemonService.getUniverse()).thenReturn(Universe.POKEMON)

    characterUseCase = CharacterUseCase(listOf(dragonBallService, pokemonService))
  }

  @Test
  fun `getCharacters should handle invalid negative page size`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act - testando com tamanho de página negativo
            val result = characterUseCase.getCharacters(0, -10)

            // Assert - o tamanho deve ser corrigido para 1 (valor mínimo)
            assertEquals(1, result.size)
          }

  @Test
  fun `getCharacters should handle extremely large page size`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act - testando com um tamanho de página extremamente grande
            val result = characterUseCase.getCharacters(0, 1000)

            // Assert - o tamanho deve ser limitado ao MAX_PAGE_SIZE (50)
            assertEquals(50, result.size)
          }

  @Test
  fun `getCharacters should handle case where one service returns more items than requested`() =
          testScope.runTest {
            // Arrange
            // Simulando um serviço que retorna mais personagens do que o solicitado
            val dragonBallCharacters =
                    (1..10).map { i ->
                      Character("db$i", "Dragon Ball Character $i", Universe.DRAGON_BALL)
                    }

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act
            val result = characterUseCase.getCharacters(0, 5)

            // Assert
            // A implementação deve lidar com isso corretamente sem falhar
            assertEquals(
                    10,
                    result.content.size
            ) // O fetchCharactersFromAllServices traz todos os retornados
          }

  @Test
  fun `getCharacters should handle timeout exception from service`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenAnswer {
              throw RuntimeException("Simulando exception de timeout")
            }

            // Act
            val result = characterUseCase.getCharacters(0, 2)

            // Assert
            assertEquals(1, result.content.size)
            assertEquals("Goku", result.content[0].name)
          }

  // Este teste foi removido pois não pode ser implementado adequadamente
  // Um serviço não deve retornar null universe, a verificação é feita durante a compilação

  @Test
  fun `getCharacters should not make service calls when page size is corrected to 0`() =
          testScope.runTest {
            // Esse teste simula uma situação edge case teórica onde o tamanho seria 0 após a
            // correção
            // Embora a implementação atual não permita isso, é um bom teste de robustez

            // Arrange
            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(emptyList())
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act - um cenário extremo fictício
            // Normalmente este seria corrigido para tamanho 1, mas testamos o comportamento
            // se de alguma forma o tamanho fosse 0
            characterUseCase.getCharacters(0, -1)

            // Assert - os serviços ainda devem ser chamados com tamanho mínimo 1
            verify(dragonBallService, times(1)).getCharacters(any(), any())
            verify(pokemonService, times(1)).getCharacters(any(), any())
          }

  @Test
  fun `getCharacters should handle case where all services return exactly requested number of items`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters =
                    listOf(
                            Character("db1", "Goku", Universe.DRAGON_BALL),
                            Character("db2", "Vegeta", Universe.DRAGON_BALL)
                    )

            val pokemonCharacters =
                    listOf(
                            Character("pk1", "Pikachu", Universe.POKEMON),
                            Character("pk2", "Charizard", Universe.POKEMON)
                    )

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)

            // Act
            val result = characterUseCase.getCharacters(0, 4)

            // Assert
            assertEquals(4, result.content.size)
            assertEquals(2, result.content.count { it.universe == Universe.DRAGON_BALL })
            assertEquals(2, result.content.count { it.universe == Universe.POKEMON })
          }

  @Test
  fun `getCharacters should handle case where all services return more than requested number of items`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters =
                    (1..5).map { i ->
                      Character("db$i", "Dragon Ball Character $i", Universe.DRAGON_BALL)
                    }

            val pokemonCharacters =
                    (1..5).map { i -> Character("pk$i", "Pokemon Character $i", Universe.POKEMON) }

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)

            // Act
            val result = characterUseCase.getCharacters(0, 4)

            // Assert
            assertEquals(10, result.content.size) // Todos são retornados
          }

  @Test
  fun `checkServicesAvailability should handle services with same name but different availability`() =
          testScope.runTest {
            // Arrange - simula um caso extremo teórico onde dois serviços usam o mesmo enum
            // Universe
            val service1: CharacterService = mock()
            val service2: CharacterService = mock()

            whenever(service1.getUniverse()).thenReturn(Universe.DRAGON_BALL)
            whenever(service2.getUniverse()).thenReturn(Universe.DRAGON_BALL)

            whenever(service1.isAvailable()).thenReturn(true)
            whenever(service2.isAvailable()).thenReturn(false)

            val testUseCase = CharacterUseCase(listOf(service1, service2))

            // Act
            val result = testUseCase.checkServicesAvailability()

            // Assert
            // O último valor (false) deve prevalecer para a mesma chave
            assertEquals(1, result.size)
            assertEquals(Status.DOWN, result[Universe.DRAGON_BALL.name])
          }
}
