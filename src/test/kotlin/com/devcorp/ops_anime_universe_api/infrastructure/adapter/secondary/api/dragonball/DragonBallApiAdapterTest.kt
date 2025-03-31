package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball

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

                        val response =
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody(mockJson)

                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.getCharacters(1, 2)

                        // Assert - para um teste normal (não page 0)
                        assertEquals(2, result.items.size)
                        assertEquals(1, result.items[0].id)
                        assertEquals("Goku", result.items[0].name)
                        assertEquals(2, result.items[1].id)
                        assertEquals("Vegeta", result.items[1].name)
                }

        @Test
        fun `getCharacters should handle HTTP error and return empty response`() =
                testScope.runTest {
                        // Arrange
                        val response = MockResponse().setResponseCode(500)
                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.getCharacters(1, 10)

                        // Assert
                        assertTrue(result.items.isEmpty())
                }

        @Test
        fun `getCharacters should handle general exception and return empty response`() =
                testScope.runTest {
                        // Arrange
                        val response =
                                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.getCharacters(1, 10)

                        // Assert
                        assertTrue(result.items.isEmpty())
                }

        @Test
        fun `getCharacters should create empty response with correct metadata`() =
                testScope.runTest {
                        // Arrange
                        val mockJson =
                                """
            {
                "items": [],
                "meta": {
                    "totalItems": 0,
                    "itemCount": 0,
                    "itemsPerPage": 10,
                    "totalPages": 0,
                    "currentPage": 1
                }
            }
        """.trimIndent()

                        val response =
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody(mockJson)

                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.getCharacters(1, 10)

                        // Assert
                        assertTrue(result.items.isEmpty())
                        assertEquals(0, result.meta.totalItems)
                        assertEquals(0, result.meta.itemCount)
                        assertEquals(10, result.meta.itemsPerPage)
                }

        @Test
        fun `isAvailable should return true when API returns 200`() =
                testScope.runTest {
                        // Arrange
                        val response = MockResponse().setResponseCode(200)
                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.isAvailable()

                        // Assert
                        assertTrue(result)
                }

        @Test
        fun `isAvailable should return false when API returns error`() =
                testScope.runTest {
                        // Arrange
                        val response = MockResponse().setResponseCode(500)
                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.isAvailable()

                        // Assert
                        assertFalse(result)
                }

        @Test
        fun `getCharacters should handle network exceptions`() =
                testScope.runTest {
                        // Arrange
                        val response =
                                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                        mockWebServer.enqueue(response)

                        // Act
                        val result = adapter.getCharacters(1, 10)

                        // Assert
                        assertTrue(result.items.isEmpty())
                }

        @Test
        fun `getCharacters should properly handle page 0 with normal API response`() =
                testScope.runTest {
                        // Arrange - mockando resposta normal da API para página 0
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
              "itemsPerPage": 10,
              "totalPages": 50,
              "currentPage": 1
          }
      }
  """.trimIndent()

                        val response =
                                MockResponse()
                                        .setResponseCode(200)
                                        .setHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                                        .setBody(mockJson)

                        mockWebServer.enqueue(response)

                        // Act - usando página 0, que agora usa o comportamento padrão
                        val result = adapter.getCharacters(0, 10)

                        // Assert - verificando que retorna os dados da API normalmente
                        assertEquals(2, result.items.size)
                        assertEquals(1, result.items[0].id)
                        assertEquals("Goku", result.items[0].name)
                        assertEquals(2, result.items[1].id)
                        assertEquals("Vegeta", result.items[1].name)
                }
}
