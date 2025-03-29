package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class DragonBallApiAdapterTest {

        private lateinit var mockWebServer: MockWebServer
        private lateinit var adapter: DragonBallApiAdapter
        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @BeforeEach
        fun setup() {
                mockWebServer = MockWebServer()
                mockWebServer.start()

                val baseUrl = mockWebServer.url("/").toString()

                adapter = DragonBallApiAdapter(WebClient.builder(), baseUrl)
        }

        @AfterEach
        fun tearDown() {
                mockWebServer.shutdown()
        }

        @Test
        fun `getCharacters should return characters when API returns 200`() =
                testScope.runTest {
                        // Arrange
                        val mockJson =
                                """
            {
                "items": [
                    {
                        "id": "1",
                        "name": "Goku",
                        "ki": "10000",
                        "race": "Saiyan"
                    },
                    {
                        "id": "2",
                        "name": "Vegeta",
                        "ki": "9000",
                        "race": "Saiyan"
                    }
                ],
                "meta": {
                    "totalItems": 100,
                    "itemCount": 2,
                    "itemsPerPage": 2,
                    "totalPages": 50,
                    "currentPage": 1
                }
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
                        val result = adapter.getCharacters(1, 2)

                        // Assert
                        assertEquals(2, result.items.size)
                        assertEquals("1", result.items[0].id)
                        assertEquals("Goku", result.items[0].name)
                        assertEquals("2", result.items[1].id)
                        assertEquals("Vegeta", result.items[1].name)
                        assertEquals(100, result.meta.totalItems)
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
                        assertTrue(result as Boolean)
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
                        assertFalse(result as Boolean)
                }
}
