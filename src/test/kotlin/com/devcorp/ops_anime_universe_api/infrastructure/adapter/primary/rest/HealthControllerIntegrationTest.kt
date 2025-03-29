package com.devcorp.ops_anime_universe_api.infrastructure.adapter.primary.rest

import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@Import(HealthControllerIntegrationTest.TestConfig::class)
class HealthControllerIntegrationTest {

        @Configuration
        class TestConfig {
                @Bean fun characterUseCase(): CharacterUseCase = mock()

                @Bean
                fun healthController(characterUseCase: CharacterUseCase): HealthController =
                        HealthController(characterUseCase)
        }

        @Autowired private lateinit var applicationContext: ApplicationContext

        @Autowired private lateinit var webTestClient: WebTestClient

        @Autowired private lateinit var characterUseCase: CharacterUseCase

        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @Test
        fun `should return health status with all services UP`() =
                testScope.runTest {
                        // Arrange
                        val serviceStatuses =
                                mapOf(
                                        Universe.DRAGON_BALL.name to Status.UP,
                                        Universe.POKEMON.name to Status.UP
                                )

                        whenever(characterUseCase.checkServicesAvailability())
                                .thenReturn(serviceStatuses)

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/health")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody()
                                .jsonPath("$.status")
                                .isEqualTo("UP")
                                .jsonPath("$.timestamp")
                                .isNotEmpty
                                .jsonPath("$.services['DRAGON_BALL']")
                                .isEqualTo("UP")
                                .jsonPath("$.services['POKEMON']")
                                .isEqualTo("UP")
                }

        @Test
        fun `should return health status with some services DOWN`() =
                testScope.runTest {
                        // Arrange
                        val serviceStatuses =
                                mapOf(
                                        Universe.DRAGON_BALL.name to Status.UP,
                                        Universe.POKEMON.name to Status.DOWN
                                )

                        whenever(characterUseCase.checkServicesAvailability())
                                .thenReturn(serviceStatuses)

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/health")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody()
                                .jsonPath("$.status")
                                .isEqualTo("UP")
                                .jsonPath("$.timestamp")
                                .isNotEmpty
                                .jsonPath("$.services['DRAGON_BALL']")
                                .isEqualTo("UP")
                                .jsonPath("$.services['POKEMON']")
                                .isEqualTo("DOWN")
                }

        @Test
        fun `should return health status with all services DOWN`() =
                testScope.runTest {
                        // Arrange
                        val serviceStatuses =
                                mapOf(
                                        Universe.DRAGON_BALL.name to Status.DOWN,
                                        Universe.POKEMON.name to Status.DOWN
                                )

                        whenever(characterUseCase.checkServicesAvailability())
                                .thenReturn(serviceStatuses)

                        // Act & Assert
                        webTestClient
                                .get()
                                .uri("/health")
                                .exchange()
                                .expectStatus()
                                .isOk
                                .expectBody()
                                .jsonPath("$.status")
                                .isEqualTo("DOWN")
                                .jsonPath("$.timestamp")
                                .isNotEmpty
                                .jsonPath("$.services['DRAGON_BALL']")
                                .isEqualTo("DOWN")
                                .jsonPath("$.services['POKEMON']")
                                .isEqualTo("DOWN")
                }
}
