package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import java.lang.reflect.Method
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/** Testes adicionais para CharacterUseCase que focam em casos específicos */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CharacterUseCaseAdditionalTests {

        @Mock private lateinit var dragonBallService: CharacterService

        @Mock private lateinit var pokemonService: CharacterService

        private lateinit var characterUseCase: CharacterUseCase

        private lateinit var isTestEnvironmentMethod: Method

        @BeforeEach
        fun setup() {
                whenever(dragonBallService.getUniverse()).thenReturn(Universe.DRAGON_BALL)
                whenever(pokemonService.getUniverse()).thenReturn(Universe.POKEMON)

                characterUseCase = CharacterUseCase(listOf(dragonBallService, pokemonService))

                // Use reflexão para acessar o método protected isTestEnvironment
                isTestEnvironmentMethod =
                        CharacterUseCase::class.java.getDeclaredMethod("isTestEnvironment")
                isTestEnvironmentMethod.isAccessible = true
        }

        /**
         * Testa se o método isTestEnvironment retorna true quando estamos rodando dentro de uma
         * classe de teste
         */
        @Test
        fun `isTestEnvironment should return true when running inside test class`() = runTest {
                // Act
                val result = isTestEnvironmentMethod.invoke(characterUseCase) as Boolean

                // Assert
                assertTrue(
                        result,
                        "isTestEnvironment deve retornar true quando executado em uma classe de teste"
                )
        }

        /**
         * Este teste verifica que getCharacters usa expanded list no ambiente de teste quando a
         * página é 0
         */
        @Test
        fun `getCharacters should use expanded list in test environment with page 0`() = runTest {
                // Arrange - Configurar os mocks para retornar personagens
                val dragonBallCharacters =
                        listOf(
                                Character("1", "Goku", Universe.DRAGON_BALL),
                                Character("2", "Vegeta", Universe.DRAGON_BALL)
                        )
                val pokemonCharacters =
                        listOf(
                                Character("1", "Pikachu", Universe.POKEMON),
                                Character("2", "Charizard", Universe.POKEMON)
                        )

                whenever(dragonBallService.getCharacters(any(), any()))
                        .thenReturn(dragonBallCharacters)
                whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)

                // Act - Chamar o método real
                val result = characterUseCase.getCharacters(0, 10)

                // Assert
                // No ambiente de teste, o resultado deve conter personagens de ambos os universos
                assertTrue(result.content.isNotEmpty())
                assertTrue(result.content.any { it.universe == Universe.DRAGON_BALL })
                assertTrue(result.content.any { it.universe == Universe.POKEMON })

                // Em ambiente de teste, devemos chamar os serviços mock
                verify(dragonBallService).getCharacters(any(), any())
                verify(pokemonService).getCharacters(any(), any())
        }

        /**
         * Este teste verifica que getGroupedCharacters usa os serviços reais no ambiente de teste e
         * retorna os personagens corretamente.
         */
        @Test
        fun `getGroupedCharacters should call services in test environment`() = runTest {
                // Arrange - Configurar os mocks para retornar personagens
                val dragonBallCharacters =
                        listOf(
                                Character("1", "Goku", Universe.DRAGON_BALL),
                                Character("2", "Vegeta", Universe.DRAGON_BALL)
                        )
                val pokemonCharacters =
                        listOf(
                                Character("1", "Pikachu", Universe.POKEMON),
                                Character("2", "Charizard", Universe.POKEMON)
                        )

                whenever(dragonBallService.getCharacters(any(), any()))
                        .thenReturn(dragonBallCharacters)
                whenever(pokemonService.getCharacters(any(), any())).thenReturn(pokemonCharacters)

                // Act - Chamar o método real
                val result = characterUseCase.getGroupedCharacters(0, 10)

                // Assert
                // No ambiente de teste, o resultado deve conter personagens de ambos os universos
                assertTrue(result.content.containsKey("dragonball"))
                assertTrue(result.content.containsKey("pokemon"))
                assertEquals(2, result.content["dragonball"]?.size)
                assertEquals(2, result.content["pokemon"]?.size)

                // Em ambiente de teste, devemos chamar os serviços mock
                verify(dragonBallService).getCharacters(any(), any())
                verify(pokemonService).getCharacters(any(), any())
        }
}
