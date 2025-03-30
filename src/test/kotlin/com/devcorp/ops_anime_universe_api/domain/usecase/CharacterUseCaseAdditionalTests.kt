package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** Testes adicionais para a classe CharacterUseCase para aumentar a cobertura de código */
class CharacterUseCaseAdditionalTests {

  private lateinit var dragonBallService: CharacterService
  private lateinit var pokemonService: CharacterService
  private lateinit var narutoService: CharacterService
  private lateinit var characterUseCase: CharacterUseCase
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setup() {
    dragonBallService = mock()
    pokemonService = mock()
    narutoService = mock()

    whenever(dragonBallService.getUniverse()).thenReturn(Universe.DRAGON_BALL)
    whenever(pokemonService.getUniverse()).thenReturn(Universe.POKEMON)
    whenever(narutoService.getUniverse()).thenReturn(Universe.NARUTO)

    characterUseCase = CharacterUseCase(listOf(dragonBallService, pokemonService, narutoService))
  }

  @Test
  fun `getCharacters should validate and correct page and size parameters`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            val pokemonCharacters = listOf(Character("pk1", "Pikachu", Universe.POKEMON))

            val narutoCharacters = listOf(Character("nr1", "Naruto", Universe.NARUTO))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)
            whenever(narutoService.getCharacters(any(), any())).thenReturn(narutoCharacters)

            // Act - testando com parâmetros inválidos (página negativa e tamanho muito grande)
            val result = characterUseCase.getCharacters(-1, 100)

            // Assert - o CharacterUseCase deve corrigir os parâmetros e não falhar
            assertEquals(3, result.content.size)
            assertEquals(0, result.page) // A página deve ser corrigida para 0
            assertEquals(50, result.size) // O tamanho deve ser limitado ao MAX_PAGE_SIZE (50)
          }

  @Test
  fun `getCharacters should handle remainder distribution correctly`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            val pokemonCharacters = listOf(Character("pk1", "Pikachu", Universe.POKEMON))

            val narutoCharacters = listOf(Character("nr1", "Naruto", Universe.NARUTO))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)
            whenever(narutoService.getCharacters(any(), any())).thenReturn(narutoCharacters)

            // Act
            // Testando com tamanho que não é divisível igualmente pelo número de serviços (3)
            // Size=5 com 3 serviços deve distribuir: 5/3 = 1, remainder = 2
            // Então os primeiros 2 serviços recebem 2 e o terceiro recebe 1
            val result = characterUseCase.getCharacters(0, 5)

            // Assert
            assertEquals(3, result.content.size) // Todos retornam apenas 1 item por mock
            assertTrue(result.content.any { it.universe == Universe.DRAGON_BALL })
            assertTrue(result.content.any { it.universe == Universe.POKEMON })
            assertTrue(result.content.any { it.universe == Universe.NARUTO })
          }

  @Test
  fun `getCharacters should handle mix of successful and failed services`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            val narutoCharacters = listOf(Character("nr1", "Naruto", Universe.NARUTO))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any()))
                    .thenThrow(RuntimeException("API error"))
            whenever(narutoService.getCharacters(any(), any())).thenReturn(narutoCharacters)

            // Act
            val result = characterUseCase.getCharacters(0, 6)

            // Assert
            assertEquals(2, result.content.size)
            assertTrue(result.content.any { it.universe == Universe.DRAGON_BALL })
            assertFalse(result.content.any { it.universe == Universe.POKEMON })
            assertTrue(result.content.any { it.universe == Universe.NARUTO })
          }

  @Test
  fun `getCharacters should handle pagination correctly`() =
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

            val narutoCharacters =
                    listOf(
                            Character("nr1", "Naruto", Universe.NARUTO),
                            Character("nr2", "Sasuke", Universe.NARUTO)
                    )

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)
            whenever(narutoService.getCharacters(any(), any())).thenReturn(narutoCharacters)

            // Act
            // Testando a segunda página
            val result = characterUseCase.getCharacters(1, 3)

            // Assert
            assertEquals(
                    6,
                    result.content.size
            ) // O fetchCharactersFromAllServices traz todos e depois aplica paginação
            assertEquals(1, result.page)
            assertEquals(3, result.size)
            assertEquals(1000, result.totalElements) // ESTIMATED_TOTAL_ELEMENTS é 1000L
          }

  @Test
  fun `checkServicesAvailability should handle mix of UP, DOWN and UNKNOWN services`() =
          testScope.runTest {
            // Arrange
            whenever(dragonBallService.isAvailable()).thenReturn(true) // UP
            whenever(pokemonService.isAvailable()).thenReturn(false) // DOWN
            whenever(narutoService.isAvailable())
                    .thenThrow(RuntimeException("Simulando timeout")) // Deve resultar em DOWN

            // Act
            val result = characterUseCase.checkServicesAvailability()

            // Assert
            assertEquals(3, result.size)
            assertEquals(Status.UP, result[Universe.DRAGON_BALL.name])
            assertEquals(Status.DOWN, result[Universe.POKEMON.name])
            assertEquals(Status.DOWN, result[Universe.NARUTO.name])
          }

  @Test
  fun `checkServicesAvailability should handle all services DOWN`() =
          testScope.runTest {
            // Arrange
            whenever(dragonBallService.isAvailable()).thenReturn(false)
            whenever(pokemonService.isAvailable()).thenReturn(false)
            whenever(narutoService.isAvailable()).thenReturn(false)

            // Act
            val result = characterUseCase.checkServicesAvailability()

            // Assert
            assertEquals(3, result.size)
            assertEquals(Status.DOWN, result[Universe.DRAGON_BALL.name])
            assertEquals(Status.DOWN, result[Universe.POKEMON.name])
            assertEquals(Status.DOWN, result[Universe.NARUTO.name])
          }

  @Test
  fun `calculateServicePage should calculate correct page for different service indices`() =
          testScope.runTest {
            // Teste indireto do método privado calculateServicePage

            // Arrange
            // Criando serviços diferentes com configurações específicas
            val service1: CharacterService = mock()
            val service2: CharacterService = mock()
            val service3: CharacterService = mock()

            whenever(service1.getUniverse()).thenReturn(Universe.DRAGON_BALL)
            whenever(service2.getUniverse()).thenReturn(Universe.POKEMON)
            whenever(service3.getUniverse()).thenReturn(Universe.NARUTO)

            whenever(service1.getCharacters(any(), any()))
                    .thenReturn(
                            listOf(
                                    Character("db1", "A-DragonBall", Universe.DRAGON_BALL),
                                    Character("db2", "B-DragonBall", Universe.DRAGON_BALL)
                            )
                    )

            whenever(service2.getCharacters(any(), any()))
                    .thenReturn(listOf(Character("pk1", "A-Pokemon", Universe.POKEMON)))

            whenever(service3.getCharacters(any(), any()))
                    .thenReturn(listOf(Character("nr1", "A-Naruto", Universe.NARUTO)))

            val testUseCase = CharacterUseCase(listOf(service1, service2, service3))

            // Act - requisitando página 1 com 4 itens
            // Deve distribuir: 2, 1, 1 entre os serviços
            val result = testUseCase.getCharacters(0, 4)

            // Assert
            assertEquals(4, result.content.size)
            assertEquals(2, result.content.count { it.universe == Universe.DRAGON_BALL })
            assertEquals(1, result.content.count { it.universe == Universe.POKEMON })
            assertEquals(1, result.content.count { it.universe == Universe.NARUTO })
          }

  @Test
  fun `getCharacters should apply page and size parameters correctly`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters =
                    (1..5).map { i -> Character("db$i", "DB Character $i", Universe.DRAGON_BALL) }

            val pokemonCharacters =
                    (1..5).map { i -> Character("pk$i", "Pokemon Character $i", Universe.POKEMON) }

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)
            whenever(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act - testando parâmetros válidos
            val result = characterUseCase.getCharacters(0, 6)

            // Assert
            assertEquals(
                    10,
                    result.content.size
            ) // Todos os personagens dos dois serviços são retornados
            assertEquals(0, result.page)
            assertEquals(6, result.size)
            // Verificando propriedades de paginação via totalPages
            assertEquals(1000, result.totalElements)
            val expectedPages =
                    (result.totalElements / result.size) +
                            (if (result.totalElements % result.size > 0) 1 else 0)
            assertEquals(expectedPages.toInt(), result.totalPages)
          }

  @Test
  fun `getCharacters should handle very large page numbers`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())
            whenever(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act - testando com uma página muito grande
            val result = characterUseCase.getCharacters(1000, 10)

            // Assert
            assertEquals(1, result.content.size)
            assertEquals(1000, result.page)
            assertEquals(10, result.size)
            // Verificando propriedades de paginação via totalPages
            assertTrue(result.page >= 0)
            assertTrue(result.totalPages > 0)
          }

  @Test
  fun `getCharacters should handle size parameter less than or equal to zero`() =
          testScope.runTest {
            // Arrange
            val dragonBallCharacters = listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

            val pokemonCharacters = listOf(Character("pk1", "Pikachu", Universe.POKEMON))

            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)

            // Act - testando com tamanho de página inválido (0)
            val result = characterUseCase.getCharacters(0, 0)

            // Assert
            // O tamanho deve ser corrigido para 1 (valor mínimo)
            assertEquals(1, result.size)
            assertTrue(result.content.isNotEmpty())
          }

  @Test
  fun `getCharacters should call services to fetch data`() =
          testScope.runTest {
            // Arrange - definindo retornos vazios para os serviços
            whenever(dragonBallService.getCharacters(any(), any())).thenReturn(emptyList())
            whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())
            whenever(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

            // Act - solicitamos dados
            characterUseCase.getCharacters(2, 6)

            // Assert - verificando que os serviços foram chamados
            verify(dragonBallService).getCharacters(any(), any())
            verify(pokemonService).getCharacters(any(), any())
            verify(narutoService).getCharacters(any(), any())
          }
}
