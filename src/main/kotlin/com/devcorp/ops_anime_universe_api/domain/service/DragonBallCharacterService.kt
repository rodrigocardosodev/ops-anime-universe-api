package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import com.devcorp.ops_anime_universe_api.domain.port.spi.DragonBallApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** Serviço para obter personagens do universo Dragon Ball */
@Service
class DragonBallCharacterService(private val dragonBallApiClient: DragonBallApiClient) :
        CharacterService {

  private val logger = LoggerFactory.getLogger(DragonBallCharacterService::class.java)

  override fun getUniverse(): Universe {
    return Universe.DRAGON_BALL
  }

  override suspend fun getCharacters(page: Int, size: Int): List<Character> {
    // Verificando parâmetros de paginação
    val validPage = page.coerceAtLeast(0)
    val validSize = size.coerceIn(1, 50) // Limita o tamanho entre 1 e 50

    logger.info("Buscando personagens do Dragon Ball na página $validPage com tamanho $validSize")

    return try {
      val response =
              dragonBallApiClient.getCharacters(
                      validPage + 1,
                      validSize
              ) // API do Dragon Ball começa com página 1

      response.items.map { character ->
        Character(
                id = character.id.toString(),
                name = character.name,
                universe = Universe.DRAGON_BALL
        )
      }
    } catch (e: Exception) {
      logger.error("Erro ao buscar personagens do Dragon Ball: ${e.message}", e)
      throw e // Deixamos a exeção propagar para garantir resiliência no nível superior
    }
  }

  override suspend fun isAvailable(): Boolean {
    return try {
      dragonBallApiClient.isAvailable()
    } catch (e: Exception) {
      logger.error("Erro ao verificar disponibilidade da API do Dragon Ball: ${e.message}", e)
      throw e
    }
  }
}
