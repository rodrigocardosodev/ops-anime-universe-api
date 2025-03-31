package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon

import com.devcorp.ops_anime_universe_api.domain.port.spi.PokemonApiClient
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.NamedApiResource
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonDetailResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonPageResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto.PokemonSprites
import com.devcorp.ops_anime_universe_api.infrastructure.config.WebClientConfig
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

/** Adaptador para a PokeAPI */
@Component
class PokemonApiAdapter(
        private val webClientConfig: WebClientConfig,
        @Value("\${external.api.pokemon.base-url}") private val baseUrl: String
) : PokemonApiClient {

    private val logger = LoggerFactory.getLogger(PokemonApiAdapter::class.java)

    // Criamos um WebClient específico para o Pokemon.
    // Não usamos o lazy aqui para evitar problemas de inicialização
    private val pokemonWebClient: WebClient = run {
        val url = baseUrl.trim().removeSuffix("/")
        logger.debug("Inicializando WebClient para PokeAPI com URL base: {}", url)
        webClientConfig.webClientBuilder().baseUrl(url).build()
    }

    @CircuitBreaker(name = "pokemonApi", fallbackMethod = "getPokemonsFallback")
    @Retry(name = "pokemonApi")
    @Cacheable(value = ["pokemon"], key = "'list-' + #offset + '-' + #limit")
    override suspend fun getPokemons(offset: Int, limit: Int): PokemonPageResponse {
        // Para garantir que estamos buscando os primeiros Pokémon da primeira página
        // O offset da PokeAPI começa com 0 para o primeiro Pokémon (Bulbasaur)
        val adjustedOffset = if (offset == 0) 0 else offset * limit
        val validLimit = limit.coerceAtLeast(1)

        logger.info(
                "Buscando pokemons: offset ajustado $adjustedOffset (página original $offset), limite $validLimit"
        )
        logger.debug(
                "URL Base: {}, Endpoint: /pokemon?offset={}&limit={}",
                baseUrl,
                adjustedOffset,
                validLimit
        )

        return try {
            pokemonWebClient
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                                .path("/pokemon")
                                .queryParam("offset", adjustedOffset)
                                .queryParam("limit", validLimit)
                                .build()
                    }
                    .retrieve()
                    .awaitBody<PokemonPageResponse>()
        } catch (e: WebClientResponseException) {
            logger.error("Erro HTTP ${e.statusCode} ao buscar lista de pokemons: ${e.message}", e)
            createEmptyPageResponse()
        } catch (e: Exception) {
            logger.error("Erro ao buscar lista de pokemons: ${e.message}", e)
            createEmptyPageResponse()
        }
    }

    @CircuitBreaker(name = "pokemonApi", fallbackMethod = "getPokemonByNameFallback")
    @Retry(name = "pokemonApi")
    @Cacheable(value = ["pokemon"], key = "'detail-' + #name")
    override suspend fun getPokemonByName(name: String): PokemonDetailResponse {
        logger.info("Buscando detalhes do pokemon: $name")
        logger.debug("URL Base: {}, Endpoint: /pokemon/{}", baseUrl, name)

        return try {
            pokemonWebClient
                    .get()
                    .uri("/pokemon/{name}", name)
                    .retrieve()
                    .awaitBody<PokemonDetailResponse>()
        } catch (e: WebClientResponseException) {
            logger.error(
                    "Erro HTTP ${e.statusCode} ao buscar detalhes do pokemon $name: ${e.message}",
                    e
            )
            createEmptyDetailResponse(name)
        } catch (e: Exception) {
            logger.error("Erro ao buscar detalhes do pokemon $name: ${e.message}", e)
            createEmptyDetailResponse(name)
        }
    }

    @CircuitBreaker(name = "pokemonApi", fallbackMethod = "isAvailableFallback")
    @Retry(name = "pokemonApi")
    override suspend fun isAvailable(): Boolean {
        logger.info("Verificando disponibilidade da PokeAPI")
        logger.debug("URL Base: {}, Endpoint: /pokemon?limit=1", baseUrl)

        return try {
            val response =
                    pokemonWebClient.get().uri("/pokemon?limit=1").retrieve().awaitBodilessEntity()
            logger.info("Status code da PokeAPI: {}", response.statusCode)
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            logger.error("Erro ao verificar disponibilidade da PokeAPI: {}", e.message)
            false
        }
    }

    // Métodos de fallback
    suspend fun getPokemonsFallback(offset: Int, limit: Int, e: Exception): PokemonPageResponse {
        logger.warn(
                "Fallback acionado para getPokemons com offset $offset e limite $limit: ${e.message}",
                e
        )
        return createEmptyPageResponse()
    }

    suspend fun getPokemonByNameFallback(name: String, e: Exception): PokemonDetailResponse {
        logger.warn("Fallback acionado para getPokemonByName com nome $name: ${e.message}", e)
        return createEmptyDetailResponse(name)
    }

    suspend fun isAvailableFallback(e: Exception): Boolean {
        logger.warn("Fallback acionado para isAvailable: ${e.message}", e)
        return false
    }

    // Métodos auxiliares para criar respostas vazias
    private fun createEmptyPageResponse(): PokemonPageResponse {
        return PokemonPageResponse(count = 0, next = null, previous = null, results = emptyList())
    }

    private fun createEmptyDetailResponse(name: String): PokemonDetailResponse {
        return PokemonDetailResponse(
                id = 0,
                name = name,
                height = 0,
                weight = 0,
                sprites =
                        PokemonSprites(
                                front_default = null,
                                front_shiny = null,
                                back_default = null,
                                back_shiny = null
                        ),
                types = emptyList(),
                species = NamedApiResource(name = "", url = ""),
                abilities = emptyList()
        )
    }

    /**
     * Extrai o ID de um Pokémon a partir da URL
     *
     * @param url URL contendo o ID do Pokémon (ex: "https://pokeapi.co/api/v2/pokemon/25/")
     * @return ID extraído ou "0" caso não encontre
     */
    override fun extractIdFromUrl(url: String): String {
        return try {
            val regex = "/pokemon/(\\d+)/".toRegex()
            regex.find(url)?.groupValues?.get(1) ?: "0"
        } catch (e: Exception) {
            logger.warn("Erro ao extrair ID da URL: $url", e)
            "0"
        }
    }
}
