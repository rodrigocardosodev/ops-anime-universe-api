package com.devcorp.ops_anime_universe_api.domain.port.spi

import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.DragonBallCharacterResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.DragonBallPageResponse

/** Porta de SPI que define o contrato para o cliente da API do Dragon Ball */
interface DragonBallApiClient {
  /**
   * Busca personagens do Dragon Ball de forma paginada
   * @param page Número da página (começando em 0)
   * @param limit Tamanho da página
   * @return Resposta paginada com os personagens
   */
  suspend fun getCharacters(
          page: Int,
          limit: Int
  ): DragonBallPageResponse<DragonBallCharacterResponse>

  /**
   * Verifica se a API do Dragon Ball está disponível
   * @return true se a API estiver disponível, false caso contrário
   */
  suspend fun isAvailable(): Boolean
}
