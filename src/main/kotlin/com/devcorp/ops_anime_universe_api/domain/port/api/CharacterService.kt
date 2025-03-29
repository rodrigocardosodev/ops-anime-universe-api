package com.devcorp.ops_anime_universe_api.domain.port.api

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe

/** Porta de API que define o contrato para os serviços de personagem */
interface CharacterService {
  /** Retorna o universo que este serviço representa */
  fun getUniverse(): Universe

  /**
   * Busca personagens de forma paginada
   * @param page Número da página (começando em 0)
   * @param size Tamanho da página
   * @return Lista de personagens
   */
  suspend fun getCharacters(page: Int, size: Int): List<Character>

  /**
   * Verifica se o serviço está disponível
   * @return true se o serviço estiver disponível, false caso contrário
   */
  suspend fun isAvailable(): Boolean
}
