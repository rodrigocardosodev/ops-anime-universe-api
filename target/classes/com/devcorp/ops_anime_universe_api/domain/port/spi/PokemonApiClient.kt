package com.devcorp.ops_anime_universe_api.domain.port.spi

import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonDetailResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonPageResponse

/** Porta de SPI que define o contrato para o cliente da PokeAPI */
interface PokemonApiClient {
  /**
   * Busca pokémons de forma paginada
   * @param offset Offset para a paginação
   * @param limit Tamanho da página
   * @return Resposta paginada com os pokémons
   */
  suspend fun getPokemons(offset: Int, limit: Int): PokemonPageResponse

  /**
   * Busca detalhes de um pokémon pelo nome
   * @param name Nome do pokémon
   * @return Detalhes do pokémon
   */
  suspend fun getPokemonByName(name: String): PokemonDetailResponse

  /**
   * Verifica se a PokeAPI está disponível
   * @return true se a API estiver disponível, false caso contrário
   */
  suspend fun isAvailable(): Boolean
}
