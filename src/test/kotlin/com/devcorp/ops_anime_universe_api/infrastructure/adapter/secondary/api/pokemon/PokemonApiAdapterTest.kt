package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon

import com.devcorp.ops_anime_universe_api.infrastructure.config.WebClientConfig
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class PokemonApiAdapterTest {

        private lateinit var mockWebServer: MockWebServer
        private lateinit var adapter: PokemonApiAdapter
        private lateinit var mockWebClientConfig: WebClientConfig
        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @BeforeEach
        fun setup() {
                mockWebServer = MockWebServer()
                mockWebServer.start()

                val baseUrl = mockWebServer.url("/").toString()
                val webClientBuilder = WebClient.builder()

                mockWebClientConfig = mock()
                whenever(mockWebClientConfig.webClientBuilder()).thenReturn(webClientBuilder)

                adapter = PokemonApiAdapter(mockWebClientConfig, baseUrl)
        }

        @AfterEach
        fun tearDown() {
                mockWebServer.shutdown()
        }

        @Test
        fun `getPokemons should return pokemons when API returns 200`() =
                testScope.runTest {
                        // Arrange
                        val mockJson =
                                """
                {
                    "count": 1118,
                    "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                    "previous": null,
                    "results": [
                        {
                            "name": "bulbasaur",
                            "url": "https://pokeapi.co/api/v2/pokemon/1/"
                        },
                        {
                            "name": "ivysaur",
                            "url": "https://pokeapi.co/api/v2/pokemon/2/"
                        }
                    ]
                }
            """.trimIndent()

                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody(mockJson)
                        )

                        // Act
                        val result = adapter.getPokemons(0, 2)

                        // Assert
                        assertEquals(1118, result.count)
                        assertEquals(2, result.results.size)
                        assertEquals("bulbasaur", result.results[0].name)
                        assertEquals("https://pokeapi.co/api/v2/pokemon/1/", result.results[0].url)
                        assertEquals("ivysaur", result.results[1].name)
                        assertEquals("https://pokeapi.co/api/v2/pokemon/2/", result.results[1].url)
                }

        @Test
        fun `getPokemons should handle API error and return empty response`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(500)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody("{\"error\": \"Internal Server Error\"}")
                        )

                        // Act
                        val result = adapter.getPokemons(0, 2)

                        // Assert
                        assertEquals(0, result.count)
                        assertEquals(0, result.results.size)
                        assertEquals(null, result.next)
                        assertEquals(null, result.previous)
                }

        @Test
        fun `getPokemonByName should return pokemon details when API returns 200`() =
                testScope.runTest {
                        // Arrange
                        val mockJson =
                                """
                {
                    "id": 25,
                    "name": "pikachu",
                    "height": 4,
                    "weight": 60,
                    "sprites": {
                        "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
                        "front_shiny": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/25.png",
                        "back_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/25.png",
                        "back_shiny": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/shiny/25.png"
                    },
                    "types": [
                        {
                            "slot": 1,
                            "type": {
                                "name": "electric",
                                "url": "https://pokeapi.co/api/v2/type/13/"
                            }
                        }
                    ],
                    "species": {
                        "name": "pikachu",
                        "url": "https://pokeapi.co/api/v2/pokemon-species/25/"
                    },
                    "abilities": [
                        {
                            "is_hidden": false,
                            "slot": 1,
                            "ability": {
                                "name": "static",
                                "url": "https://pokeapi.co/api/v2/ability/9/"
                            }
                        }
                    ]
                }
            """.trimIndent()

                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody(mockJson)
                        )

                        // Act
                        val result = adapter.getPokemonByName("pikachu")

                        // Assert
                        assertEquals(25, result.id)
                        assertEquals("pikachu", result.name)
                        assertEquals(4, result.height)
                        assertEquals(60, result.weight)
                        assertEquals(
                                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
                                result.sprites.front_default
                        )
                        assertEquals(1, result.types.size)
                        assertEquals("electric", result.types[0].type.name)
                        assertEquals("pikachu", result.species.name)
                        assertEquals(1, result.abilities.size)
                        assertEquals("static", result.abilities[0].ability.name)
                }

        @Test
        fun `getPokemonByName should handle API error and return empty response`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(404)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody("{\"error\": \"Not Found\"}")
                        )

                        // Act
                        val result = adapter.getPokemonByName("nonexistent")

                        // Assert
                        assertEquals(0, result.id)
                        assertEquals("nonexistent", result.name)
                        assertEquals(0, result.height)
                        assertEquals(0, result.weight)
                        assertEquals(null, result.sprites.front_default)
                        assertEquals(0, result.types.size)
                        assertEquals("", result.species.name)
                        assertEquals(0, result.abilities.size)
                }

        @Test
        fun `isAvailable should return true when API returns 200`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody("{}")
                        )

                        // Act
                        val result = adapter.isAvailable()

                        // Assert
                        assertTrue(result)
                }

        @Test
        fun `isAvailable should return false when API returns error`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(500)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody("{\"error\": \"Internal Server Error\"}")
                        )

                        // Act
                        val result = adapter.isAvailable()

                        // Assert
                        assertFalse(result)
                }

        @Test
        fun `getPokemonsFallback should return empty response`() =
                testScope.runTest {
                        // Arrange
                        val exception = RuntimeException("Erro simulado")

                        // Act
                        val result = adapter.getPokemonsFallback(0, 10, exception)

                        // Assert
                        assertEquals(0, result.count)
                        assertEquals(0, result.results.size)
                        assertEquals(null, result.next)
                        assertEquals(null, result.previous)
                }

        @Test
        fun `getPokemonByNameFallback should return empty detail response`() =
                testScope.runTest {
                        // Arrange
                        val exception = RuntimeException("Erro simulado")
                        val pokemonName = "pikachu"

                        // Act
                        val result = adapter.getPokemonByNameFallback(pokemonName, exception)

                        // Assert
                        assertEquals(0, result.id)
                        assertEquals(pokemonName, result.name)
                        assertEquals(0, result.height)
                        assertEquals(0, result.weight)
                        assertEquals(null, result.sprites.front_default)
                        assertEquals(0, result.types.size)
                        assertEquals("", result.species.name)
                        assertEquals(0, result.abilities.size)
                }

        @Test
        fun `isAvailableFallback should return false`() =
                testScope.runTest {
                        // Arrange
                        val exception = RuntimeException("Erro simulado")

                        // Act
                        val result = adapter.isAvailableFallback(exception)

                        // Assert
                        assertFalse(result)
                }

        @Test
        fun `getPokemonByName should handle generic exception and return empty response`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                        )

                        // Act
                        val result = adapter.getPokemonByName("pikachu")

                        // Assert
                        assertEquals(0, result.id)
                        assertEquals("pikachu", result.name)
                        assertEquals(0, result.height)
                        assertEquals(0, result.weight)
                        assertEquals(null, result.sprites.front_default)
                }

        @Test
        fun `getPokemons should handle generic exception and return empty response`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                        )

                        // Act
                        val result = adapter.getPokemons(0, 10)

                        // Assert
                        assertEquals(0, result.count)
                        assertEquals(0, result.results.size)
                }

        @Test
        fun `extractIdFromUrl should extract ID correctly from valid URL`() =
                testScope.runTest {
                        // Act
                        val result =
                                adapter.extractIdFromUrl("https://pokeapi.co/api/v2/pokemon/42/")

                        // Assert
                        assertEquals("42", result)
                }

        @Test
        fun `extractIdFromUrl should return 0 for invalid URL`() =
                testScope.runTest {
                        // Act
                        val result = adapter.extractIdFromUrl("invalid-url-without-id")

                        // Assert
                        assertEquals("0", result)
                }

        @Test
        fun `extractIdFromUrl should handle exception and return 0`() =
                testScope.runTest {
                        // Este teste verifica se uma exceção é tratada corretamente
                        try {
                                // Tentar com null causaria erro de compilação, então usamos
                                // uma string que poderia causar problemas no regex
                                val result = adapter.extractIdFromUrl("()*+?[]{}|^$")
                                assertEquals("0", result)
                        } catch (e: Exception) {
                                // Se lançar exceção, o teste falha
                                assertTrue(false, "Não deveria lançar exceção: ${e.message}")
                        }
                }
}
