package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import com.devcorp.ops_anime_universe_api.domain.port.spi.DragonBallApiClient
import org.springframework.stereotype.Service

/** Implementação do serviço de personagens do Dragon Ball */
@Service
class DragonBallCharacterService(private val dragonBallApiClient: DragonBallApiClient) :
        CharacterService {

  override fun getUniverse(): Universe = Universe.DRAGON_BALL

  override suspend fun getCharacters(page: Int, size: Int): List<Character> {
    val response =
            dragonBallApiClient.getCharacters(
                    page + 1,
                    size
            ) // API do Dragon Ball começa com página 1

    return response.items.map { character ->
      Character(id = character.id, name = character.name, universe = Universe.DRAGON_BALL)
    }
  }

  override suspend fun isAvailable(): Boolean {
    return dragonBallApiClient.isAvailable()
  }
}
