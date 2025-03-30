package com.devcorp.ops_anime_universe_api.domain.model

/** Modelo de domínio que representa um personagem de qualquer universo */
data class Character(val id: String, val name: String, val universe: Universe)

/** Enum que define os universos disponíveis na aplicação */
enum class Universe {
  DRAGON_BALL,
  POKEMON,
  NARUTO
}
