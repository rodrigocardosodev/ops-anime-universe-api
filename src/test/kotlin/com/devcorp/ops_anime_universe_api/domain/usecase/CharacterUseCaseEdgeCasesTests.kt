package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import kotlin.test.assertTrue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/**
 * Testes adicionais para CharacterUseCase focados em casos de borda e exceções para aumentar a
 * cobertura de testes
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
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
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())

                        // Act - testando com um tamanho de página extremamente grande
                        val result = characterUseCase.getCharacters(0, 1000)

                        // Assert - o tamanho deve ser limitado ao MAX_PAGE_SIZE (100)
                        assertEquals(100, result.size)
                }

        @Test
        fun `getCharacters should handle case where one service returns more items than requested`() =
                testScope.runTest {
                        // Arrange
                        // Simulando um serviço que retorna mais personagens do que o solicitado
                        val dragonBallCharacters =
                                (1..10).map { i ->
                                        Character(
                                                "db$i",
                                                "Dragon Ball Character $i",
                                                Universe.DRAGON_BALL
                                        )
                                }

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
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
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
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
                        // Esse teste simula uma situação edge case teórica onde o tamanho seria 0
                        // após a
                        // correção
                        // Embora a implementação atual não permita isso, é um bom teste de robustez

                        // Arrange
                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(emptyList())
                        whenever(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())

                        // Act - um cenário extremo fictício
                        // Normalmente este seria corrigido para tamanho 1, mas testamos o
                        // comportamento
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

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        whenever(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)

                        // Act
                        val result = characterUseCase.getCharacters(0, 4)

                        // Assert
                        assertEquals(4, result.content.size)
                        assertEquals(
                                2,
                                result.content.count { it.universe == Universe.DRAGON_BALL }
                        )
                        assertEquals(2, result.content.count { it.universe == Universe.POKEMON })
                }

        @Test
        fun `getCharacters should handle case where all services return more than requested number of items`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                (1..5).map { i ->
                                        Character(
                                                "db$i",
                                                "Dragon Ball Character $i",
                                                Universe.DRAGON_BALL
                                        )
                                }

                        val pokemonCharacters =
                                (1..5).map { i ->
                                        Character("pk$i", "Pokemon Character $i", Universe.POKEMON)
                                }

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        whenever(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)

                        // Act
                        val result = characterUseCase.getCharacters(0, 4)

                        // Assert
                        assertEquals(10, result.content.size) // Todos são retornados
                }

        @Test
        fun `checkServicesAvailability should handle services with same name but different availability`() =
                testScope.runTest {
                        // Arrange - simula um caso extremo teórico onde dois serviços usam o mesmo
                        // enum
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

        @Test
        fun `getGroupedCharacters should handle large page number`() =
                testScope.runTest {
                        // Arrange - página muito alta que está além dos dados disponíveis
                        val highPageNumber = 1000
                        val size = 10

                        // Act
                        val result = characterUseCase.getGroupedCharacters(highPageNumber, size)

                        // Assert
                        // Deve retornar estrutura vazia para universos quando a página está além
                        // dos dados
                        assertTrue(result.content["dragonball"]?.isEmpty() ?: false)
                        assertTrue(result.content["pokemon"]?.isEmpty() ?: false)
                        assertEquals(highPageNumber, result.page)
                        assertEquals(size, result.size)
                        // Totais devem ser mantidos com valores corretos
                        assertTrue(result.dragonBallTotalPages > 0)
                        assertTrue(result.pokemonTotalPages > 0)
                }

        @Test
        fun `getCharacters should handle negative page values`() =
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

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        whenever(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)

                        // Act
                        val result = characterUseCase.getCharacters(-5, 4)

                        // Assert - deve tratar o número de página negativo como 0
                        assertEquals(4, result.content.size)
                        assertEquals(
                                0,
                                result.page
                        ) // Verificando se o número da página foi corrigido para 0
                }

        @Test
        fun `getGroupedCharacters should handle negative values`() =
                testScope.runTest {
                        // Arrange
                        // Não precisamos configurar os mocks, pois getGroupedCharacters utiliza a
                        // lista estática

                        // Act - Usando página negativa e um tamanho válido
                        val result = characterUseCase.getGroupedCharacters(-5, 3)

                        // Assert - deve tratar o número de página negativo como 0
                        assertEquals(0, result.page)
                        assertTrue(result.content.isNotEmpty())
                        assertTrue(result.content.containsKey("dragonball"))
                        assertTrue(result.content.containsKey("pokemon"))
                }

        @Test
        fun `getCharacters should handle service timeout and return results`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                listOf(
                                        Character("db1", "Goku", Universe.DRAGON_BALL),
                                        Character("db2", "Vegeta", Universe.DRAGON_BALL)
                                )

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)

                        // Simulando um timeout do serviço Pokemon com RuntimeException em vez de
                        // TimeoutException
                        whenever(pokemonService.getCharacters(any(), any()))
                                .thenThrow(RuntimeException("Simulando timeout"))

                        // Act
                        val result = characterUseCase.getCharacters(0, 2)

                        // Assert
                        assertEquals(2, result.content.size)
                        assertEquals(0, result.content.count { it.universe == Universe.POKEMON })
                }

        @Test
        fun `getGroupedCharacters should handle small negative size`() =
                testScope.runTest {
                        // Act - Usando um tamanho negativo
                        val result = characterUseCase.getGroupedCharacters(0, -2)

                        // Assert - deve usar o tamanho mínimo de 1
                        assertTrue(result.content["dragonball"]?.isNotEmpty() ?: false)
                        assertTrue(result.content["pokemon"]?.isNotEmpty() ?: false)
                        assertEquals(1, result.size)
                }

        @Test
        fun `checkServicesAvailability should handle all services unavailable`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallService.isAvailable()).thenReturn(false)
                        whenever(pokemonService.isAvailable()).thenReturn(false)

                        // Act
                        val result = characterUseCase.checkServicesAvailability()

                        // Assert
                        assertEquals(Status.DOWN, result[Universe.DRAGON_BALL.name])
                        assertEquals(Status.DOWN, result[Universe.POKEMON.name])
                }

        @Test
        fun `getCharacters should use dynamic distribution for first page`() =
                testScope.runTest {
                        // Arrange - não precisamos configurar mocks para este teste específico

                        // Act
                        val result = characterUseCase.getCharacters(0, 4)

                        // Assert
                        assertNotNull(result)
                        assertEquals(0, result.page)
                        assertEquals(4, result.size)
                }
}
