package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import com.devcorp.ops_anime_universe_api.domain.port.spi.PokemonApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** Implementação do serviço de personagens do Pokémon */
@Service
class PokemonCharacterService(private val pokemonApiClient: PokemonApiClient) : CharacterService {

  private val logger = LoggerFactory.getLogger(PokemonCharacterService::class.java)

  // Limita o número de chamadas simultâneas para a API Pokemon
  private val concurrentRequestsSemaphore = Semaphore(10)

  override fun getUniverse(): Universe = Universe.POKEMON

  override suspend fun getCharacters(page: Int, size: Int): List<Character> {
    // Verifica parâmetros de paginação - sempre positivos
    val validPage = page.coerceAtLeast(0)
    val validSize = size.coerceAtLeast(1)

    // Passamos diretamente a página e o tamanho, o adapter calculará o offset correto
    logger.info("Buscando pokémons na página $validPage com tamanho $validSize")

    return try {
      val pokemonPage = pokemonApiClient.getPokemons(validPage, validSize)

      if (pokemonPage.results.isEmpty()) {
        logger.info("Nenhum pokémon encontrado na página $validPage")
        return emptyList()
      }

      coroutineScope {
        pokemonPage
                .results
                .map { basicInfo ->
                  async(Dispatchers.IO) {
                    try {
                      // Usa o semáforo para limitar requisições concorrentes
                      concurrentRequestsSemaphore.withPermit {
                        val details = pokemonApiClient.getPokemonByName(basicInfo.name)
                        Character(
                                id = details.id.toString(),
                                name = details.name.replaceFirstChar { it.uppercase() },
                                universe = Universe.POKEMON
                        )
                      }
                    } catch (e: Exception) {
                      logger.warn(
                              "Erro ao buscar detalhes do pokémon ${basicInfo.name}, usando informações básicas",
                              e
                      )
                      // Em caso de erro ao buscar detalhes, retorna informações básicas
                      Character(
                              id = extractIdFromUrl(basicInfo.url),
                              name = basicInfo.name.replaceFirstChar { it.uppercase() },
                              universe = Universe.POKEMON
                      )
                    }
                  }
                }
                .awaitAll()
      }
    } catch (e: Exception) {
      logger.error("Erro ao buscar lista de pokémons, retornando lista vazia", e)
      emptyList()
    }
  }

  override suspend fun isAvailable(): Boolean {
    return withContext(Dispatchers.IO) {
      try {
        pokemonApiClient.isAvailable()
      } catch (e: Exception) {
        logger.error("Erro ao verificar disponibilidade da PokeAPI", e)
        false
      }
    }
  }

  private fun extractIdFromUrl(url: String): String {
    return try {
      val regex = "/pokemon/(\\d+)/".toRegex()
      regex.find(url)?.groupValues?.get(1) ?: "0"
    } catch (e: Exception) {
      logger.warn("Erro ao extrair ID da URL: $url", e)
      "0"
    }
  }
}
