package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto

/** DTO para a resposta paginada da PokeAPI */
data class PokemonPageResponse(
        val count: Long,
        val next: String?,
        val previous: String?,
        val results: List<PokemonBasicInfo>
)

/** DTO para as informações básicas de um pokémon */
data class PokemonBasicInfo(val name: String, val url: String)

/** DTO para os detalhes de um pokémon */
data class PokemonDetailResponse(
        val id: Int,
        val name: String,
        val height: Int,
        val weight: Int,
        val sprites: PokemonSprites,
        val types: List<PokemonTypeSlot>,
        val species: NamedApiResource,
        val abilities: List<PokemonAbility>
)

/** DTO para os sprites de um pokémon */
data class PokemonSprites(
        val front_default: String?,
        val front_shiny: String?,
        val back_default: String?,
        val back_shiny: String?
)

/** DTO para o tipo de um pokémon */
data class PokemonTypeSlot(val slot: Int, val type: NamedApiResource)

/** DTO para as habilidades de um pokémon */
data class PokemonAbility(val is_hidden: Boolean, val slot: Int, val ability: NamedApiResource)

/** DTO para um recurso com nome na PokeAPI */
data class NamedApiResource(val name: String, val url: String)
