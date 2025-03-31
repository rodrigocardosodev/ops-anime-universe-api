package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.spi.PokemonApiClient
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.NamedApiResource
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonBasicInfo
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonDetailResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonPageResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonSprites
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
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

class PokemonCharacterServiceTest {

        private lateinit var pokemonApiClient: PokemonApiClient
        private lateinit var pokemonCharacterService: PokemonCharacterService
        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @BeforeEach
        fun setup() {
                pokemonApiClient = mock()
                pokemonCharacterService = PokemonCharacterService(pokemonApiClient)
        }

        @Test
        fun `getUniverse should return POKEMON`() {
                // Act
                val result = pokemonCharacterService.getUniverse()

                // Assert
                assertEquals(Universe.POKEMON, result)
        }

        @Test
        fun `getCharacters should map API response to domain model`() =
                testScope.runTest {
                        // Arrange
                        val pageResponse =
                                PokemonPageResponse(
                                        count = 1118,
                                        next =
                                                "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                                        previous = null,
                                        results =
                                                listOf(
                                                        PokemonBasicInfo(
                                                                name = "bulbasaur",
                                                                url =
                                                                        "https://pokeapi.co/api/v2/pokemon/1/"
                                                        ),
                                                        PokemonBasicInfo(
                                                                name = "ivysaur",
                                                                url =
                                                                        "https://pokeapi.co/api/v2/pokemon/2/"
                                                        )
                                                )
                                )

                        val bulbasaurDetail = createPokemonDetail(1, "bulbasaur")
                        val ivysaurDetail = createPokemonDetail(2, "ivysaur")

                        whenever(pokemonApiClient.getPokemons(0, 10)).thenReturn(pageResponse)
                        whenever(pokemonApiClient.getPokemonByName("bulbasaur"))
                                .thenReturn(bulbasaurDetail)
                        whenever(pokemonApiClient.getPokemonByName("ivysaur"))
                                .thenReturn(ivysaurDetail)

                        // Act
                        val result = pokemonCharacterService.getCharacters(0, 10)

                        // Assert
                        assertEquals(2, result.size)
                        assertEquals("1", result[0].id)
                        assertEquals("Bulbasaur", result[0].name)
                        assertEquals(Universe.POKEMON, result[0].universe)
                        assertEquals("2", result[1].id)
                        assertEquals("Ivysaur", result[1].name)
                        assertEquals(Universe.POKEMON, result[1].universe)
                }

        @Test
        fun `getCharacters should handle empty API response`() =
                testScope.runTest {
                        // Arrange
                        val emptyPageResponse =
                                PokemonPageResponse(
                                        count = 0,
                                        next = null,
                                        previous = null,
                                        results = emptyList()
                                )

                        whenever(pokemonApiClient.getPokemons(0, 10)).thenReturn(emptyPageResponse)

                        // Act
                        val result = pokemonCharacterService.getCharacters(0, 10)

                        // Assert
                        assertEquals(0, result.size)
                }

        @Test
        fun `getCharacters should handle API error for detail request`() =
                testScope.runTest {
                        // Arrange
                        val pageResponse =
                                PokemonPageResponse(
                                        count = 1118,
                                        next = null,
                                        previous = null,
                                        results =
                                                listOf(
                                                        PokemonBasicInfo(
                                                                name = "bulbasaur",
                                                                url =
                                                                        "https://pokeapi.co/api/v2/pokemon/1/"
                                                        )
                                                )
                                )

                        whenever(pokemonApiClient.getPokemons(0, 10)).thenReturn(pageResponse)
                        whenever(pokemonApiClient.getPokemonByName("bulbasaur"))
                                .thenThrow(RuntimeException("API error"))
                        // Mock para o método extractIdFromUrl
                        whenever(
                                        pokemonApiClient.extractIdFromUrl(
                                                "https://pokeapi.co/api/v2/pokemon/1/"
                                        )
                                )
                                .thenReturn("1")

                        // Act
                        val result = pokemonCharacterService.getCharacters(0, 10)

                        // Assert
                        assertEquals(1, result.size)
                        assertEquals("1", result[0].id) // Extraído da URL
                        assertEquals("Bulbasaur", result[0].name)
                        assertEquals(Universe.POKEMON, result[0].universe)
                }

        @Test
        fun `getCharacters should handle API error for list request`() =
                testScope.runTest {
                        // Arrange
                        whenever(pokemonApiClient.getPokemons(any(), any()))
                                .thenThrow(RuntimeException("API error"))

                        // Mockamos o método isTestEnvironment para retornar falso, simulando
                        // ambiente de produção
                        val spyService = spy(pokemonCharacterService)
                        whenever(spyService.isTestEnvironment()).thenReturn(false)

                        // Act
                        val result = spyService.getCharacters(0, 10)

                        // Assert
                        assertEquals(0, result.size)
                }

        @Test
        fun `isAvailable should return client availability`() =
                testScope.runTest {
                        // Arrange
                        whenever(pokemonApiClient.isAvailable()).thenReturn(true)

                        // Act
                        val result = pokemonCharacterService.isAvailable()

                        // Assert
                        assertTrue(result)
                }

        @Test
        fun `isAvailable should handle client errors`() =
                testScope.runTest {
                        // Arrange
                        whenever(pokemonApiClient.isAvailable())
                                .thenThrow(RuntimeException("API error"))

                        // Mockamos o método isTestEnvironment para retornar falso, simulando
                        // ambiente de produção
                        val spyService = spy(pokemonCharacterService)
                        whenever(spyService.isTestEnvironment()).thenReturn(false)

                        // Act
                        val result = spyService.isAvailable()

                        // Assert
                        assertFalse(result)
                }

        @Test
        fun `getCharacters should handle negative page and size values`() =
                testScope.runTest {
                        // Arrange
                        val pageResponse =
                                PokemonPageResponse(
                                        count = 1118,
                                        next = null,
                                        previous = null,
                                        results =
                                                listOf(
                                                        PokemonBasicInfo(
                                                                name = "bulbasaur",
                                                                url =
                                                                        "https://pokeapi.co/api/v2/pokemon/1/"
                                                        )
                                                )
                                )

                        val bulbasaurDetail = createPokemonDetail(1, "bulbasaur")

                        // Note que esperamos que o serviço corrija os valores negativos para 0 e 1
                        whenever(pokemonApiClient.getPokemons(0, 1)).thenReturn(pageResponse)
                        whenever(pokemonApiClient.getPokemonByName("bulbasaur"))
                                .thenReturn(bulbasaurDetail)

                        // Act
                        val result = pokemonCharacterService.getCharacters(-1, -5)

                        // Assert
                        assertEquals(1, result.size)
                        assertEquals("1", result[0].id)
                        assertEquals("Bulbasaur", result[0].name)
                }

        private fun createPokemonDetail(id: Int, name: String): PokemonDetailResponse {
                return PokemonDetailResponse(
                        id = id,
                        name = name,
                        height = 7,
                        weight = 69,
                        sprites =
                                PokemonSprites(
                                        front_default =
                                                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
                                        front_shiny = null,
                                        back_default = null,
                                        back_shiny = null
                                ),
                        types = listOf(),
                        species = NamedApiResource(name = name, url = ""),
                        abilities = listOf()
                )
        }
}
