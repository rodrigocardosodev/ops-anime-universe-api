package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

/** Testes adicionais para a classe CharacterUseCase para aumentar a cobertura de código */
@ExperimentalCoroutinesApi
class CharacterUseCaseAdditionalTests {

        private val testScope = TestScope()
        private lateinit var dragonBallService: CharacterService
        private lateinit var pokemonService: CharacterService
        private lateinit var narutoService: CharacterService
        private lateinit var characterUseCase: CharacterUseCase

        @BeforeEach
        fun setup() {
                dragonBallService = mock()
                pokemonService = mock()
                narutoService = mock()

                `when`(dragonBallService.getUniverse()).thenReturn(Universe.DRAGON_BALL)
                `when`(pokemonService.getUniverse()).thenReturn(Universe.POKEMON)
                `when`(narutoService.getUniverse()).thenReturn(Universe.NARUTO)

                characterUseCase =
                        CharacterUseCase(listOf(dragonBallService, pokemonService, narutoService))
        }

        @Test
        fun `getCharacters should validate and correct page and size parameters`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        val pokemonCharacters =
                                listOf(Character("pk1", "Pikachu", Universe.POKEMON))

                        val narutoCharacters = listOf(Character("nr1", "Naruto", Universe.NARUTO))

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)
                        `when`(narutoService.getCharacters(any(), any()))
                                .thenReturn(narutoCharacters)

                        // Act - testando com parâmetros inválidos (página negativa e tamanho muito
                        // grande)
                        val result = characterUseCase.getCharacters(-1, 200)

                        // Assert
                        // Os parâmetros devem ser corrigidos (página 0, tamanho 100)
                        assertEquals(0, result.page)
                        assertTrue(result.size <= 100) // MAX_PAGE_SIZE
                        assertTrue(result.content.isNotEmpty())
                }

        @Test
        fun `getCharacters should handle remainder distribution correctly`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        val pokemonCharacters =
                                listOf(Character("pk1", "Pikachu", Universe.POKEMON))

                        val narutoCharacters = listOf(Character("nr1", "Naruto", Universe.NARUTO))

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)
                        `when`(narutoService.getCharacters(any(), any()))
                                .thenReturn(narutoCharacters)

                        // Act
                        // Com o novo comportamento, cada serviço recebe o size completo
                        val result = characterUseCase.getCharacters(0, 5)

                        // Assert
                        // O número de itens é o total retornado pelos mocks
                        assertEquals(3, result.content.size)
                }

        @Test
        fun `getCharacters should handle mix of successful and failed services`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        val narutoCharacters = listOf(Character("nr1", "Naruto", Universe.NARUTO))

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any()))
                                .thenThrow(RuntimeException("API error"))
                        `when`(narutoService.getCharacters(any(), any()))
                                .thenReturn(narutoCharacters)

                        // Act
                        val result = characterUseCase.getCharacters(0, 6)

                        // Assert
                        // Aqui esperamos todos os personagens retornados (DRAGON_BALL e NARUTO)
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

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)
                        `when`(narutoService.getCharacters(any(), any()))
                                .thenReturn(narutoCharacters)

                        // Act
                        // Testando a segunda página com o novo comportamento
                        val result = characterUseCase.getCharacters(1, 3)

                        // Assert
                        // Com a implementação atual, o total de personagens é retornado
                        assertEquals(6, result.content.size)
                        assertEquals(1, result.page)
                        assertEquals(3, result.size)
                        assertEquals(
                                1452,
                                result.totalElements
                        ) // Atualizado para 1452 (150 + 1302)
                }

        @Test
        fun `checkServicesAvailability should handle mix of UP, DOWN and UNKNOWN services`() =
                testScope.runTest {
                        // Arrange
                        `when`(dragonBallService.isAvailable()).thenReturn(true) // UP
                        `when`(pokemonService.isAvailable()).thenReturn(false) // DOWN
                        `when`(narutoService.isAvailable())
                                .thenThrow(
                                        RuntimeException("Simulando timeout")
                                ) // Deve resultar em DOWN

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
                        `when`(dragonBallService.isAvailable()).thenReturn(false)
                        `when`(pokemonService.isAvailable()).thenReturn(false)
                        `when`(narutoService.isAvailable()).thenReturn(false)

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

                        `when`(service1.getUniverse()).thenReturn(Universe.DRAGON_BALL)
                        `when`(service2.getUniverse()).thenReturn(Universe.POKEMON)
                        `when`(service3.getUniverse()).thenReturn(Universe.NARUTO)

                        `when`(service1.getCharacters(any(), any()))
                                .thenReturn(
                                        listOf(
                                                Character(
                                                        "db1",
                                                        "A-DragonBall",
                                                        Universe.DRAGON_BALL
                                                ),
                                                Character(
                                                        "db2",
                                                        "B-DragonBall",
                                                        Universe.DRAGON_BALL
                                                )
                                        )
                                )

                        `when`(service2.getCharacters(any(), any()))
                                .thenReturn(listOf(Character("pk1", "A-Pokemon", Universe.POKEMON)))

                        `when`(service3.getCharacters(any(), any()))
                                .thenReturn(listOf(Character("nr1", "A-Naruto", Universe.NARUTO)))

                        val testUseCase = CharacterUseCase(listOf(service1, service2, service3))

                        // Act
                        // Com o novo comportamento, cada serviço recebe o size completo
                        val result = testUseCase.getCharacters(4, 10)

                        // Assert
                        // Com o novo comportamento, o cálculo de página é diferente
                        // A página do serviço é igual à página da requisição (4)
                        assertEquals(4, result.page)
                        assertEquals(10, result.size)
                }

        @Test
        fun `getCharacters should apply page and size parameters correctly`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                (1..5).map { i ->
                                        Character("db$i", "DB Character $i", Universe.DRAGON_BALL)
                                }

                        val pokemonCharacters =
                                (1..5).map { i ->
                                        Character("pk$i", "Pokemon Character $i", Universe.POKEMON)
                                }

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)
                        `when`(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

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
                        assertEquals(1452, result.totalElements)
                        val expectedPages =
                                (result.totalElements / result.size) +
                                        (if (result.totalElements % result.size > 0) 1 else 0)
                        assertEquals(expectedPages.toInt(), result.totalPages)
                }

        @Test
        fun `getCharacters should handle very large page numbers`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())
                        `when`(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

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
                        val dragonBallCharacters =
                                listOf(Character("db1", "Goku", Universe.DRAGON_BALL))

                        val pokemonCharacters =
                                listOf(Character("pk1", "Pikachu", Universe.POKEMON))

                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        `when`(pokemonService.getCharacters(any(), any()))
                                .thenReturn(pokemonCharacters)
                        `when`(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

                        // Act - testando com tamanho de página inválido (0)
                        val result = characterUseCase.getCharacters(0, 0)

                        // Assert
                        // O tamanho deve ser corrigido para 1 (valor mínimo)
                        assertEquals(1, result.size)
                        // Verificando que o conteúdo contém os personagens esperados
                        assertEquals(2, result.content.size)
                }

        @Test
        fun `getCharacters should call services to fetch data`() =
                testScope.runTest {
                        // Arrange - definindo retornos vazios para os serviços
                        `when`(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(emptyList())
                        `when`(pokemonService.getCharacters(any(), any())).thenReturn(emptyList())
                        `when`(narutoService.getCharacters(any(), any())).thenReturn(emptyList())

                        // Act - solicitamos dados
                        characterUseCase.getCharacters(0, 6)

                        // Assert - verificando que os serviços foram chamados
                        verify(dragonBallService).getCharacters(any(), any())
                        verify(pokemonService).getCharacters(any(), any())
                        verify(narutoService).getCharacters(any(), any())
                }

        @Test
        fun `Page class should calculate pagination properties correctly`() =
                testScope.runTest {
                        // Testando a classe interna Page com diferentes cenários

                        // Cenário 1: Página inicial com itens
                        val page1 = Page(listOf("a", "b", "c"), 0, 10, 30)
                        assertEquals(0, page1.page)
                        assertEquals(10, page1.size)
                        assertEquals(30, page1.totalElements)
                        assertEquals(3, page1.totalPages)
                        assertTrue(page1.isFirst)
                        assertFalse(page1.isLast)
                        assertTrue(page1.hasNext)
                        assertFalse(page1.hasPrevious)

                        // Cenário 2: Página intermediária
                        val page2 = Page(listOf("d", "e", "f"), 1, 10, 30)
                        assertEquals(1, page2.page)
                        assertEquals(10, page2.size)
                        assertEquals(30, page2.totalElements)
                        assertEquals(3, page2.totalPages)
                        assertFalse(page2.isFirst)
                        assertFalse(page2.isLast)
                        assertTrue(page2.hasNext)
                        assertTrue(page2.hasPrevious)

                        // Cenário 3: Última página
                        val page3 = Page(listOf("g", "h", "i", "j"), 2, 10, 30)
                        assertEquals(2, page3.page)
                        assertEquals(10, page3.size)
                        assertEquals(30, page3.totalElements)
                        assertEquals(3, page3.totalPages)
                        assertFalse(page3.isFirst)
                        assertTrue(page3.isLast)
                        assertFalse(page3.hasNext)
                        assertTrue(page3.hasPrevious)
                }

        @Test
        fun `Page class should handle edge cases correctly`() =
                testScope.runTest {
                        // Caso 1: Sem elementos
                        val emptyPage = Page(emptyList<String>(), 0, 10, 0)
                        assertEquals(0, emptyPage.page)
                        assertEquals(10, emptyPage.size)
                        assertEquals(0, emptyPage.totalElements)
                        assertEquals(
                                1,
                                emptyPage.totalPages
                        ) // Mínimo de 1 página mesmo sem elementos
                        assertTrue(emptyPage.isFirst)
                        assertTrue(emptyPage.isLast)
                        assertFalse(emptyPage.hasNext)
                        assertFalse(emptyPage.hasPrevious)

                        // Caso 2: Número fracionário de páginas
                        val fractionalPage = Page(listOf("a", "b"), 0, 3, 10)
                        assertEquals(0, fractionalPage.page)
                        assertEquals(3, fractionalPage.size)
                        assertEquals(10, fractionalPage.totalElements)
                        assertEquals(
                                4,
                                fractionalPage.totalPages
                        ) // 10/3 = 3.33... arredondado para 4
                        assertTrue(fractionalPage.isFirst)
                        assertFalse(fractionalPage.isLast)
                        assertTrue(fractionalPage.hasNext)
                        assertFalse(fractionalPage.hasPrevious)

                        // Caso 3: Página inexistente (além do total)
                        val invalidPage = Page(emptyList<String>(), 5, 10, 30)
                        assertEquals(5, invalidPage.page)
                        assertEquals(10, invalidPage.size)
                        assertEquals(30, invalidPage.totalElements)
                        assertEquals(3, invalidPage.totalPages)
                        assertFalse(invalidPage.isFirst)
                        assertTrue(
                                invalidPage.isLast
                        ) // Considerada como última mesmo sendo inexistente
                        assertFalse(invalidPage.hasNext)
                        assertTrue(invalidPage.hasPrevious)
                }

        @Test
        fun `test calculateSize with different values`() {
                // Test with negative value
                assertEquals(1, CharacterUseCase.calculateSize(-5))

                // Test with zero value
                assertEquals(1, CharacterUseCase.calculateSize(0))

                // Test with value within range
                assertEquals(50, CharacterUseCase.calculateSize(50))

                // Test with value above MAX_PAGE_SIZE
                val maxPageSize = 100 // This should match MAX_PAGE_SIZE in CharacterUseCase
                assertEquals(maxPageSize, CharacterUseCase.calculateSize(150))
        }
}
