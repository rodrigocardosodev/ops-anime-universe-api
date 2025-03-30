package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.spi.DragonBallApiClient
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.DragonBallCharacterResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.DragonBallPageResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.MetaData
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DragonBallCharacterServiceTest {

        private lateinit var dragonBallApiClient: DragonBallApiClient
        private lateinit var dragonBallCharacterService: DragonBallCharacterService
        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @BeforeEach
        fun setup() {
                dragonBallApiClient = mock()
                dragonBallCharacterService = DragonBallCharacterService(dragonBallApiClient)
        }

        @Test
        fun `getUniverse should return DRAGON_BALL`() {
                // Act
                val result = dragonBallCharacterService.getUniverse()

                // Assert
                assertEquals(Universe.DRAGON_BALL, result)
        }

        @Test
        fun `getCharacters should map API response to domain model`() =
                testScope.runTest {
                        // Arrange
                        val pageResponse =
                                DragonBallPageResponse(
                                        items =
                                                listOf(
                                                        DragonBallCharacterResponse(
                                                                id = 1,
                                                                name = "Goku",
                                                                ki = "10000",
                                                                race = "Saiyan"
                                                        ),
                                                        DragonBallCharacterResponse(
                                                                id = 2,
                                                                name = "Vegeta",
                                                                ki = "9000",
                                                                race = "Saiyan"
                                                        )
                                                ),
                                        meta =
                                                MetaData(
                                                        totalItems = 100,
                                                        itemCount = 2,
                                                        itemsPerPage = 2,
                                                        totalPages = 50,
                                                        currentPage = 1
                                                )
                                )

                        whenever(dragonBallApiClient.getCharacters(1, 10)).thenReturn(pageResponse)

                        // Act
                        val result = dragonBallCharacterService.getCharacters(0, 10)

                        // Assert
                        assertEquals(2, result.size)
                        assertEquals("1", result[0].id)
                        assertEquals("Goku", result[0].name)
                        assertEquals(Universe.DRAGON_BALL, result[0].universe)
                        assertEquals("2", result[1].id)
                        assertEquals("Vegeta", result[1].name)
                        assertEquals(Universe.DRAGON_BALL, result[1].universe)
                }

        @Test
        fun `getCharacters should handle empty response`() =
                testScope.runTest {
                        // Arrange
                        val emptyPageResponse =
                                DragonBallPageResponse(
                                        items = emptyList<DragonBallCharacterResponse>(),
                                        meta =
                                                MetaData(
                                                        totalItems = 0,
                                                        itemCount = 0,
                                                        itemsPerPage = 10,
                                                        totalPages = 0,
                                                        currentPage = 1
                                                )
                                )

                        whenever(dragonBallApiClient.getCharacters(1, 10))
                                .thenReturn(emptyPageResponse)

                        // Act
                        val result = dragonBallCharacterService.getCharacters(0, 10)

                        // Assert
                        assertEquals(0, result.size)
                }

        @Test
        fun `getCharacters should handle client error`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallApiClient.getCharacters(1, 10))
                                .thenThrow(RuntimeException("API error"))

                        // Act & Assert
                        try {
                                val result = dragonBallCharacterService.getCharacters(0, 10)
                                // Se não lançar exceção, o teste falha
                                throw AssertionError("Deveria ter lançado exceção")
                        } catch (e: RuntimeException) {
                                // Exceção esperada
                                assertEquals("API error", e.message)
                        }
                }

        @Test
        fun `isAvailable should return client availability`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallApiClient.isAvailable()).thenReturn(true)

                        // Act
                        val result = dragonBallCharacterService.isAvailable()

                        // Assert
                        assertTrue(result)
                }

        @Test
        fun `isAvailable should handle client errors`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallApiClient.isAvailable()).thenReturn(false)

                        // Act
                        val result = dragonBallCharacterService.isAvailable()

                        // Assert
                        assertFalse(result)
                }

        @Test
        fun `isAvailable should propagate exceptions`() =
                testScope.runTest {
                        // Arrange
                        whenever(dragonBallApiClient.isAvailable())
                                .thenThrow(RuntimeException("Connection error"))

                        // Act & Assert
                        try {
                                val result = dragonBallCharacterService.isAvailable()
                                // Se não lançar exceção, o teste falha
                                throw AssertionError("Deveria ter lançado exceção")
                        } catch (e: RuntimeException) {
                                // Exceção esperada
                                assertEquals("Connection error", e.message)
                        }
                }
}
