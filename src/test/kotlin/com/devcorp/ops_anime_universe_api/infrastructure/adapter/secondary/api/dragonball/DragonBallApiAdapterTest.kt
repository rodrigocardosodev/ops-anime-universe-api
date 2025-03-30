package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball

import com.devcorp.ops_anime_universe_api.infrastructure.config.WebClientConfig
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class DragonBallApiAdapterTest {

        private lateinit var mockWebServer: MockWebServer
        private lateinit var adapter: DragonBallApiAdapter
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

                adapter = DragonBallApiAdapter(mockWebClientConfig, baseUrl)
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
                        "id": 1,
                        "name": "Goku",
                        "ki": "10000",
                        "race": "Saiyan"
                    },
                    {
                        "id": 2,
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
                        assertEquals(1, result.items[0].id)
                        assertEquals("Goku", result.items[0].name)
                        assertEquals(2, result.items[1].id)
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

        @Test
        fun `getCharacters should handle HTTP error and return empty response`() =
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
                        val result = adapter.getCharacters(1, 10)

                        // Assert
                        assertEquals(0, result.items.size)
                        assertEquals(0, result.meta.totalItems)
                        assertEquals(0, result.meta.itemCount)
                        assertEquals(10, result.meta.itemsPerPage)
                        assertEquals(0, result.meta.totalPages)
                        assertEquals(1, result.meta.currentPage)
                }

        @Test
        fun `getCharacters should handle general exception and return empty response`() =
                testScope.runTest {
                        // Arrange
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setSocketPolicy(
                                                okhttp3.mockwebserver.SocketPolicy
                                                        .DISCONNECT_AFTER_REQUEST
                                        )
                        )

                        // Act
                        val result = adapter.getCharacters(1, 10)

                        // Assert
                        assertEquals(0, result.items.size)
                        assertEquals(0, result.meta.totalItems)
                }

        @Test
        fun `getCharactersFallback should return empty response`() =
                testScope.runTest {
                        // Act
                        val result =
                                adapter.getCharactersFallback(
                                        1,
                                        10,
                                        RuntimeException("Test exception")
                                )

                        // Assert
                        assertEquals(0, result.items.size)
                        assertEquals(0, result.meta.totalItems)
                        assertEquals(0, result.meta.itemCount)
                        assertEquals(10, result.meta.itemsPerPage)
                        assertEquals(0, result.meta.totalPages)
                        assertEquals(1, result.meta.currentPage)
                }

        @Test
        fun `isAvailableFallback should return false`() =
                testScope.runTest {
                        // Act
                        val result = adapter.isAvailableFallback(RuntimeException("Test exception"))

                        // Assert
                        assertFalse(result)
                }

        @Test
        fun `getCharacters should create empty response with correct metadata`() =
                testScope.runTest {
                        // Arrange - preparando uma resposta com conteúdo malformado
                        mockWebServer.enqueue(
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody("{malformed json}")
                        )

                        // Act
                        val result = adapter.getCharacters(3, 15)

                        // Assert
                        assertEquals(0, result.items.size)
                        assertEquals(0, result.meta.totalItems)
                        assertEquals(0, result.meta.itemCount)
                        assertEquals(15, result.meta.itemsPerPage)
                        assertEquals(0, result.meta.totalPages)
                        assertEquals(3, result.meta.currentPage)
                }

        @Test
        fun `isAvailable should handle network exceptions`() =
                testScope.runTest {
                        // Arrange - simular falha de conexão
                        mockWebServer.shutdown()

                        // Act
                        val result = adapter.isAvailable()

                        // Assert
                        assertFalse(result)
                }
}
