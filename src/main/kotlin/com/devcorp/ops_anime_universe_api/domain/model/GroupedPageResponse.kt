package com.devcorp.ops_anime_universe_api.domain.model

/** Modelo de domínio que representa uma resposta paginada com conteúdo agrupado por universo */
data class GroupedPageResponse(
        val content: Map<String, List<Character>>,
        val page: Int,
        val size: Int,
        val totalPage: Int,
        val totalElements: Long,
        val totalPages: Int,
        val dragonBallTotalPages: Int,
        val pokemonTotalPages: Int
) {
        companion object {
                fun of(
                        dragonBallCharacters: List<Character>,
                        pokemonCharacters: List<Character>,
                        page: Int,
                        size: Int,
                        totalElements: Long,
                        dragonBallTotalElements: Long = 25,
                        pokemonTotalElements: Long = 25,
                        totalPages: Int? = null,
                        dragonBallTotalPages: Int? = null,
                        pokemonTotalPages: Int? = null
                ): GroupedPageResponse {
                        // Calcula total de páginas por universo se não foram fornecidos
                        val calculatedDragonBallTotalPages =
                                dragonBallTotalPages
                                        ?: when {
                                                dragonBallTotalElements == 0L -> 0
                                                size == 0 -> 1
                                                dragonBallTotalElements % size == 0L ->
                                                        (dragonBallTotalElements / size).toInt()
                                                else -> (dragonBallTotalElements / size + 1).toInt()
                                        }

                        val calculatedPokemonTotalPages =
                                pokemonTotalPages
                                        ?: when {
                                                pokemonTotalElements == 0L -> 0
                                                size == 0 -> 1
                                                pokemonTotalElements % size == 0L ->
                                                        (pokemonTotalElements / size).toInt()
                                                else -> (pokemonTotalElements / size + 1).toInt()
                                        }

                        // O totalPages é a soma das páginas dos dois universos se não foi fornecido
                        val calculatedTotalPages =
                                totalPages
                                        ?: (calculatedDragonBallTotalPages +
                                                calculatedPokemonTotalPages)

                        // Cria um único mapa contendo os personagens de cada universo
                        val content =
                                mapOf(
                                        "pokemon" to pokemonCharacters,
                                        "dragonball" to dragonBallCharacters
                                )

                        // Calcula o número total de itens na página atual
                        val totalPage = dragonBallCharacters.size + pokemonCharacters.size

                        return GroupedPageResponse(
                                content,
                                page,
                                size,
                                totalPage,
                                totalElements,
                                calculatedTotalPages,
                                calculatedDragonBallTotalPages,
                                calculatedPokemonTotalPages
                        )
                }
        }
}
