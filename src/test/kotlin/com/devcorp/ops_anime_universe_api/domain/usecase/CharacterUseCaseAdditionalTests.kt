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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/** Classe de teste para funcionalidades adicionais do CharacterUseCase */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CharacterUseCaseAdditionalTests {

  private lateinit var dragonBallService: CharacterService
  private lateinit var pokemonService: CharacterService
  private lateinit var characterUseCase: CharacterUseCase

  // Métodos acessados via reflexão
  private lateinit var isTestEnvironmentMethod: Method
  private lateinit var getPagedCharactersMethod: Method

  @BeforeEach
  fun setup() {
    dragonBallService = mock()
    pokemonService = mock()

    whenever(dragonBallService.getUniverse()).thenReturn(Universe.DRAGON_BALL)
    whenever(pokemonService.getUniverse()).thenReturn(Universe.POKEMON)

    characterUseCase = CharacterUseCase(listOf(dragonBallService, pokemonService))

    // Configurar acesso aos métodos privados via reflexão
    isTestEnvironmentMethod = CharacterUseCase::class.java.getDeclaredMethod("isTestEnvironment")
    isTestEnvironmentMethod.isAccessible = true

    getPagedCharactersMethod =
            CharacterUseCase::class.java.getDeclaredMethod(
                    "getPagedCharacters",
                    List::class.java,
                    Int::class.java,
                    Int::class.java
            )
    getPagedCharactersMethod.isAccessible = true
  }

  /**
   * Este teste verifica se o método isTestEnvironment detecta corretamente quando está sendo
   * executado dentro de uma classe de teste.
   */
  @Test
  fun `isTestEnvironment should return true when running inside test class`() = runTest {
    // Act - Chama o método real usando reflexão
    val result = isTestEnvironmentMethod.invoke(characterUseCase) as Boolean

    // Assert - Como estamos em uma classe de teste, o resultado deve ser true
    assertTrue(
            result,
            "isTestEnvironment deveria retornar true quando dentro de uma classe de teste"
    )
  }

  /**
   * Este teste verifica comportamentos especiais do CharacterUseCase em ambientes de teste vs
   * produção
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

    whenever(dragonBallService.getCharacters(any(), any())).thenReturn(dragonBallCharacters)
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

  /** Este teste verifica o método getPagedCharacters com uma sublista válida */
  @Test
  fun `getPagedCharacters should return correct sublist with valid parameters`() = runTest {
    // Arrange
    val characterList = (1..10).map { i -> Character("$i", "Character$i", Universe.DRAGON_BALL) }

    // Act - Usando reflexão para chamar o método privado
    @Suppress("UNCHECKED_CAST")
    val result =
            getPagedCharactersMethod.invoke(
                    characterUseCase,
                    characterList,
                    1, // page 1
                    3 // size 3
            ) as
                    List<Character>

    // Assert
    assertEquals(3, result.size)
    // Verificamos que os IDs são os esperados (4, 5, 6 - segunda página com size=3)
    assertEquals("4", result[0].id)
    assertEquals("5", result[1].id)
    assertEquals("6", result[2].id)
  }

  /**
   * Este teste verifica se getPagedCharacters retorna uma lista vazia quando o startIndex é maior
   * ou igual ao tamanho da lista.
   */
  @Test
  fun `getPagedCharacters should return empty list when startIndex exceeds list size`() = runTest {
    // Arrange
    val characterList =
            listOf(
                    Character("1", "Character1", Universe.DRAGON_BALL),
                    Character("2", "Character2", Universe.DRAGON_BALL),
                    Character("3", "Character3", Universe.DRAGON_BALL)
            )

    // Act - Usando reflexão para chamar o método privado
    @Suppress("UNCHECKED_CAST")
    val result =
            getPagedCharactersMethod.invoke(
                    characterUseCase,
                    characterList,
                    2, // page que excede o tamanho da lista
                    2 // size 2
            ) as
                    List<Character>

    // Assert
    assertTrue(result.isEmpty())
  }

  /**
   * Este teste verifica se getPagedCharacters retorna uma lista vazia quando a lista de personagens
   * de entrada está vazia.
   */
  @Test
  fun `getPagedCharacters should return empty list when input list is empty`() = runTest {
    // Arrange
    val emptyList = emptyList<Character>()

    // Act - Usando reflexão para chamar o método privado
    @Suppress("UNCHECKED_CAST")
    val result =
            getPagedCharactersMethod.invoke(
                    characterUseCase,
                    emptyList,
                    0, // page 0
                    10 // size 10
            ) as
                    List<Character>

    // Assert
    assertTrue(result.isEmpty())
  }

  /**
   * Este teste verifica se getPagedCharacters funciona corretamente quando startIndex + size excede
   * o tamanho da lista.
   */
  @Test
  fun `getPagedCharacters should handle case where size exceeds available elements`() = runTest {
    // Arrange
    val characterList =
            listOf(
                    Character("1", "Character1", Universe.DRAGON_BALL),
                    Character("2", "Character2", Universe.DRAGON_BALL),
                    Character("3", "Character3", Universe.DRAGON_BALL),
                    Character("4", "Character4", Universe.DRAGON_BALL),
                    Character("5", "Character5", Universe.DRAGON_BALL)
            )

    // Act - Solicitando mais elementos do que existem disponíveis
    @Suppress("UNCHECKED_CAST")
    val result =
            getPagedCharactersMethod.invoke(
                    characterUseCase,
                    characterList,
                    0, // page 0
                    10 // size maior que a lista
            ) as
                    List<Character>

    // Assert - deve retornar todos os elementos disponíveis
    assertEquals(5, result.size)
    assertEquals("1", result[0].id)
    assertEquals("5", result[4].id)
  }
}
