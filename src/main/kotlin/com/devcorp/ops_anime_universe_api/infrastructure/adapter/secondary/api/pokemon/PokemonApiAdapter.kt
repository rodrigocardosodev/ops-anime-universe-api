package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon

import com.devcorp.ops_anime_universe_api.domain.port.spi.PokemonApiClient
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonDetailResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonPageResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

/** Adaptador para a PokeAPI */
@Component
class PokemonApiAdapter(
        private val webClientBuilder: WebClient.Builder,
        @Value("\${external.api.pokemon.base-url}") private val baseUrl: String
) : PokemonApiClient {

    private val logger = LoggerFactory.getLogger(PokemonApiAdapter::class.java)

    // Inicialização tardia do WebClient com a URL base correta
    private val webClient: WebClient by lazy {
        logger.info("Inicializando WebClient para PokeAPI com URL base: {}", baseUrl)
        webClientBuilder.baseUrl(baseUrl).build()
    }

    @CircuitBreaker(name = "pokemonApi", fallbackMethod = "getPokemonsFallback")
    @Retry(name = "pokemonApi")
    @Cacheable(value = ["pokemon"], key = "'list-' + #offset + '-' + #limit")
    override suspend fun getPokemons(offset: Int, limit: Int): PokemonPageResponse {
        logger.info("Buscando pokemons: offset $offset, limite $limit")
        return webClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                            .path(
                                    "pokemon"
                            ) // Removi a barra inicial para evitar problemas de caminho duplo
                            .queryParam("offset", offset)
                            .queryParam("limit", limit)
                            .build()
                }
                .retrieve()
                .awaitBody<PokemonPageResponse>()
    }

    @CircuitBreaker(name = "pokemonApi", fallbackMethod = "getPokemonByNameFallback")
    @Retry(name = "pokemonApi")
    @Cacheable(value = ["pokemon"], key = "'detail-' + #name")
    override suspend fun getPokemonByName(name: String): PokemonDetailResponse {
        logger.info("Buscando detalhes do pokemon: $name")
        return webClient
                .get()
                .uri("pokemon/{name}", name) // Removi a barra inicial
                .retrieve()
                .awaitBody<PokemonDetailResponse>()
    }

    @CircuitBreaker(name = "pokemonApi", fallbackMethod = "isAvailableFallback")
    @Retry(name = "pokemonApi")
    override suspend fun isAvailable(): Boolean {
        logger.info("Verificando disponibilidade da PokeAPI")
        return try {
            val response =
                    webClient
                            .get()
                            .uri("pokemon?limit=1") // Removi a barra inicial
                            .retrieve()
                            .awaitBodilessEntity()
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            logger.error("Erro ao verificar disponibilidade da PokeAPI", e)
            false
        }
    }

    // Métodos de fallback
    suspend fun getPokemonsFallback(offset: Int, limit: Int, e: Exception): PokemonPageResponse {
        logger.warn("Fallback acionado para getPokemons com offset $offset e limite $limit", e)
        return PokemonPageResponse(count = 0, next = null, previous = null, results = emptyList())
    }

    suspend fun getPokemonByNameFallback(name: String, e: Exception): PokemonDetailResponse {
        logger.warn("Fallback acionado para getPokemonByName com nome $name", e)
        // Criando uma resposta fallback simplificada
        return PokemonDetailResponse(
                id = 0,
                name = name,
                height = 0,
                weight = 0,
                sprites =
                        com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api
                                .pokemon.dto.PokemonSprites(
                                front_default = null,
                                front_shiny = null,
                                back_default = null,
                                back_shiny = null
                        ),
                types = emptyList(),
                species =
                        com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api
                                .pokemon.dto.NamedApiResource(name = "", url = ""),
                abilities = emptyList()
        )
    }

    suspend fun isAvailableFallback(e: Exception): Boolean {
        logger.warn("Fallback acionado para isAvailable", e)
        return false
    }
}
