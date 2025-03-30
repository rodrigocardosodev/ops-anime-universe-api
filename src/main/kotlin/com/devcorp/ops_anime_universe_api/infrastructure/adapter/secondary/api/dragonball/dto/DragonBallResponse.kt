package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto

/** DTO para a resposta paginada da API do Dragon Ball */
data class DragonBallPageResponse<T>(
        val items: List<T>,
        val meta: MetaData,
        val links: Links? = null
)

/** DTO para os metadados da paginação da API do Dragon Ball */
data class MetaData(
        val totalItems: Long,
        val itemCount: Int,
        val itemsPerPage: Int,
        val totalPages: Int,
        val currentPage: Int
)

/** DTO para os links de paginação */
data class Links(val first: String?, val previous: String?, val next: String?, val last: String?)

/** DTO para um personagem da API do Dragon Ball */
data class DragonBallCharacterResponse(
        val id: Int,
        val name: String,
        val ki: String? = null,
        val maxKi: String? = null,
        val race: String? = null,
        val gender: String? = null,
        val description: String? = null,
        val image: String? = null,
        val affiliation: String? = null,
        val deletedAt: String? = null
)
