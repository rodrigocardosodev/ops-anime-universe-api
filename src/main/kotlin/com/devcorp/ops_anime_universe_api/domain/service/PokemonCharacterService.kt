package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import com.devcorp.ops_anime_universe_api.domain.port.spi.PokemonApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** Implementação do serviço de personagens do Pokémon */
@Service
class PokemonCharacterService(private val pokemonApiClient: PokemonApiClient) : CharacterService {

  private val logger = LoggerFactory.getLogger(PokemonCharacterService::class.java)

  override fun getUniverse(): Universe = Universe.POKEMON

  override suspend fun getCharacters(page: Int, size: Int): List<Character> {
    // Verifica parâmetros de paginação
    val validPage = if (page < 0) 0 else page
    val validSize = if (size <= 0) 20 else size
    val offset = validPage * validSize

    logger.info("Buscando pokémons na página $validPage (offset $offset) com tamanho $validSize")

    try {
      val pokemonPage = pokemonApiClient.getPokemons(offset, validSize)

      return coroutineScope {
        pokemonPage
                .results
                .map { basicInfo ->
                  async {
                    try {
                      val details = pokemonApiClient.getPokemonByName(basicInfo.name)
                      Character(
                              id = details.id.toString(),
                              name = details.name.capitalize(),
                              universe = Universe.POKEMON
                      )
                    } catch (e: Exception) {
                      logger.warn(
                              "Erro ao buscar detalhes do pokémon ${basicInfo.name}, usando informações básicas",
                              e
                      )
                      // Em caso de erro ao buscar detalhes, retorna informações básicas
                      Character(
                              id = extractIdFromUrl(basicInfo.url),
                              name = basicInfo.name.capitalize(),
                              universe = Universe.POKEMON
                      )
                    }
                  }
                }
                .awaitAll()
      }
    } catch (e: Exception) {
      logger.error("Erro ao buscar lista de pokémons, retornando lista vazia", e)
      return emptyList()
    }
  }

  override suspend fun isAvailable(): Boolean {
    return try {
      pokemonApiClient.isAvailable()
    } catch (e: Exception) {
      logger.error("Erro ao verificar disponibilidade da PokeAPI", e)
      false
    }
  }

  private fun extractIdFromUrl(url: String): String {
    val regex = "/pokemon/(\\d+)/".toRegex()
    return regex.find(url)?.groupValues?.get(1) ?: "0"
  }

  private fun String.capitalize(): String {
    return if (this.isNotEmpty()) this.substring(0, 1).uppercase() + this.substring(1) else this
  }
}
