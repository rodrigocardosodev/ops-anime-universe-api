package com.devcorp.ops_anime_universe_api.infrastructure.adapter.primary.rest

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.GroupedPageResponse
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest
@ActiveProfiles("test")
@TestPropertySource(properties = ["spring.main.allow-bean-definition-overriding=true"])
@Import(CharacterControllerIntegrationTest.TestConfig::class)
class CharacterControllerIntegrationTest {

        @Configuration
        class TestConfig {
                @Bean fun characterUseCase(): CharacterUseCase = mock()

                @Bean
                fun characterController(characterUseCase: CharacterUseCase): CharacterController =
                        CharacterController(characterUseCase)
        }

        @Autowired private lateinit var applicationContext: ApplicationContext

        @Autowired private lateinit var webTestClient: WebTestClient

        @Autowired private lateinit var characterUseCase: CharacterUseCase

        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @BeforeEach
        fun setUp() {
                webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build()
        }

        @Test
        fun `getCharacters returns characters from all services`() =
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

                        // Configuração dos mocks
                        whenever(characterUseCase.getGroupedCharacters(any(), any()))
                                .thenReturn(
                                        GroupedPageResponse.of(
                                                dragonBallCharacters,
                                                pokemonCharacters,
                                                0,
                                                4,
                                                (dragonBallCharacters.size + pokemonCharacters.size)
                                                        .toLong()
                                        )
                                )

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/api/v1/characters?page=0&size=4")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody<GroupedPageResponse>()
                                .consumeWith { response ->
                                        val body = response.responseBody
                                        assert(body != null) { "Response body should not be null" }
                                        val dragonballContent =
                                                body!!.content["dragonball"] ?: emptyList()
                                        val pokemonContent = body.content["pokemon"] ?: emptyList()
                                        assert(dragonballContent.size == 2) {
                                                "Expected 2 Dragon Ball characters"
                                        }
                                        assert(pokemonContent.size == 2) {
                                                "Expected 2 Pokemon characters"
                                        }
                                }
                }

        @Test
        fun `getCharacters with pagination parameters applies them correctly`() =
                testScope.runTest {
                        // Arrange
                        val page = 1
                        val size = 2

                        val dragonBallCharacters =
                                listOf(Character("db3", "Gohan", Universe.DRAGON_BALL))

                        val pokemonCharacters =
                                listOf(Character("pk3", "Bulbasaur", Universe.POKEMON))

                        // Configuração dos mocks
                        whenever(characterUseCase.getGroupedCharacters(any(), any()))
                                .thenReturn(
                                        GroupedPageResponse.of(
                                                dragonBallCharacters,
                                                pokemonCharacters,
                                                page,
                                                size,
                                                (dragonBallCharacters.size + pokemonCharacters.size)
                                                        .toLong()
                                        )
                                )

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/api/v1/characters?page=${page}&size=${size}")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody<GroupedPageResponse>()
                                .consumeWith { response ->
                                        val body = response.responseBody
                                        assert(body != null) { "Response body should not be null" }
                                        assert(body!!.page == page) {
                                                "Expected page $page, but got ${body.page}"
                                        }
                                        assert(body.size == size) {
                                                "Expected size $size, but got ${body.size}"
                                        }
                                }
                }

        @Test
        fun `getCharacters with negative size should use minimum size 1`() =
                testScope.runTest {
                        // Arrange
                        val page = 0
                        val size = -5
                        val expectedSize = 1

                        val dragonBallCharacter = Character("db1", "Goku", Universe.DRAGON_BALL)
                        val pokemonCharacter = Character("pk1", "Pikachu", Universe.POKEMON)

                        whenever(characterUseCase.getGroupedCharacters(any(), any()))
                                .thenReturn(
                                        GroupedPageResponse.of(
                                                listOf(dragonBallCharacter),
                                                listOf(pokemonCharacter),
                                                page,
                                                expectedSize,
                                                2
                                        )
                                )

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/api/v1/characters?page=$page&size=$size")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody<GroupedPageResponse>()
                                .consumeWith { response ->
                                        val body = response.responseBody
                                        assert(body != null) { "Response body should not be null" }
                                        assert(body!!.size == expectedSize) {
                                                "Expected size $expectedSize, but got ${body.size}"
                                        }
                                }
                }

        @Test
        fun `getGroupedCharacters returns grouped characters`() =
                testScope.runTest {
                        // Arrange
                        val page = 0
                        val size = 20

                        val dragonBallCharacters =
                                listOf(
                                        Character("db1", "Goku", Universe.DRAGON_BALL),
                                        Character("db2", "Vegeta", Universe.DRAGON_BALL),
                                        Character("db3", "Piccolo", Universe.DRAGON_BALL)
                                )

                        val pokemonCharacters =
                                listOf(
                                        Character("pk1", "Pikachu", Universe.POKEMON),
                                        Character("pk2", "Charizard", Universe.POKEMON)
                                )

                        // Configuração dos mocks
                        whenever(characterUseCase.getGroupedCharacters(any(), any()))
                                .thenReturn(
                                        GroupedPageResponse.of(
                                                dragonBallCharacters,
                                                pokemonCharacters,
                                                page,
                                                size,
                                                (dragonBallCharacters.size + pokemonCharacters.size)
                                                        .toLong()
                                        )
                                )

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/api/v1/characters/grouped?page=${page}&size=${size}")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody<GroupedPageResponse>()
                                .consumeWith { response ->
                                        val body = response.responseBody
                                        assert(body != null) { "Response body should not be null" }
                                        val dragonballContent =
                                                body!!.content["dragonball"] ?: emptyList()
                                        val pokemonContent = body.content["pokemon"] ?: emptyList()
                                        assert(dragonballContent.size == 3) {
                                                "Expected 3 Dragon Ball characters"
                                        }
                                        assert(pokemonContent.size == 2) {
                                                "Expected 2 Pokemon characters"
                                        }
                                }
                }

        @Test
        fun `getGroupedCharacters with custom pagination parameters applies them correctly`() =
                testScope.runTest {
                        // Arrange
                        val page = 2
                        val size = 5

                        val dragonBallCharacters =
                                listOf(Character("db6", "Frieza", Universe.DRAGON_BALL))

                        val pokemonCharacters = listOf(Character("pk6", "Mewtwo", Universe.POKEMON))

                        // Configuração dos mocks
                        whenever(characterUseCase.getGroupedCharacters(any(), any()))
                                .thenReturn(
                                        GroupedPageResponse.of(
                                                dragonBallCharacters,
                                                pokemonCharacters,
                                                page,
                                                size,
                                                12L
                                        )
                                )

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/api/v1/characters/grouped?page=${page}&size=${size}")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody<GroupedPageResponse>()
                                .consumeWith { response ->
                                        val body = response.responseBody
                                        assert(body != null) { "Response body should not be null" }
                                        assert(body!!.page == page) {
                                                "Expected page $page, but got ${body.page}"
                                        }
                                        assert(body.size == size) {
                                                "Expected size $size, but got ${body.size}"
                                        }
                                        assert(body.totalElements == 12L) {
                                                "Expected totalElements 12, but got ${body.totalElements}"
                                        }
                                }
                }

        @Test
        fun `ping endpoint should return status UP`() =
                testScope.runTest {
                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/api/v1/characters/ping")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody<Map<String, String>>()
                                .consumeWith { response ->
                                        val body = response.responseBody
                                        assert(body != null) { "Response body should not be null" }
                                        assert(body!!["status"] == "UP") {
                                                "Expected status UP, but got ${body["status"]}"
                                        }
                                        assert(body.containsKey("message")) {
                                                "Response should contain a message"
                                        }
                                }
                }
}
