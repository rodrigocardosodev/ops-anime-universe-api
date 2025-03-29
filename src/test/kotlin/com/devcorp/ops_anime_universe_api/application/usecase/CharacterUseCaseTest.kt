package com.devcorp.ops_anime_universe_api.application.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CharacterUseCaseTest {

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
        fun `getCharacters should return characters from all services`() =
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
                                result.content.count { character ->
                                        character.universe == Universe.DRAGON_BALL
                                }
                        )
                        assertEquals(
                                2,
                                result.content.count { character ->
                                        character.universe == Universe.POKEMON
                                }
                        )
                }

        @Test
        fun `getCharacters should handle errors from services`() =
                testScope.runTest {
                        // Arrange
                        val dragonBallCharacters =
                                listOf(
                                        Character("db1", "Goku", Universe.DRAGON_BALL),
                                        Character("db2", "Vegeta", Universe.DRAGON_BALL)
                                )

                        whenever(dragonBallService.getCharacters(any(), any()))
                                .thenReturn(dragonBallCharacters)
                        whenever(pokemonService.getCharacters(any(), any()))
                                .thenThrow(RuntimeException("API error"))

                        // Act
                        val result = characterUseCase.getCharacters(0, 4)

                        // Assert
                        assertEquals(2, result.content.size)
                        assertEquals(
                                2,
                                result.content.count { character ->
                                        character.universe == Universe.DRAGON_BALL
                                }
                        )
                        assertEquals(
                                0,
                                result.content.count { character ->
                                        character.universe == Universe.POKEMON
                                }
                        )
                }

        @Test
        fun `checkServicesAvailability should return status for all services`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallService.isAvailable()).thenReturn(true)
                        whenever(pokemonService.isAvailable()).thenReturn(false)

                        // Act
                        val result = characterUseCase.checkServicesAvailability()

                        // Assert
                        assertEquals(2, result.size)
                        assertEquals(Status.UP, result[Universe.DRAGON_BALL.name])
                        assertEquals(Status.DOWN, result[Universe.POKEMON.name])
                }

        @Test
        fun `checkServicesAvailability should handle errors from services`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallService.isAvailable())
                                .thenThrow(RuntimeException("API error"))
                        whenever(pokemonService.isAvailable()).thenReturn(true)

                        // Act
                        val result = characterUseCase.checkServicesAvailability()

                        // Assert
                        assertEquals(2, result.size)
                        assertEquals(Status.DOWN, result[Universe.DRAGON_BALL.name])
                        assertEquals(Status.UP, result[Universe.POKEMON.name])
                }
}
