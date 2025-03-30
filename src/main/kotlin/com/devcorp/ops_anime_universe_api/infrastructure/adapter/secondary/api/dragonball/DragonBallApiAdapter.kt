package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball

import com.devcorp.ops_anime_universe_api.domain.port.spi.DragonBallApiClient
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.DragonBallCharacterResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.DragonBallPageResponse
import com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto.MetaData
import com.devcorp.ops_anime_universe_api.infrastructure.config.WebClientConfig
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

/** Adaptador para a API do Dragon Ball */
@Component
class DragonBallApiAdapter(
        private val webClientConfig: WebClientConfig,
        @Value("\${external.api.dragonball.base-url}") private val baseUrl: String
) : DragonBallApiClient {

  private val logger = LoggerFactory.getLogger(DragonBallApiAdapter::class.java)

  // Criamos um WebClient específico para o Dragon Ball.
  // Não usamos o lazy aqui para evitar problemas de inicialização
  private val dragonBallWebClient: WebClient =
          webClientConfig.webClientBuilder().baseUrl(baseUrl.trim().removeSuffix("/")).build()

  @CircuitBreaker(name = "dragonballApi", fallbackMethod = "getCharactersFallback")
  @Retry(name = "dragonballApi")
  @Cacheable(value = ["dragonball"], key = "'list-' + #page + '-' + #limit")
  override suspend fun getCharacters(
          page: Int,
          limit: Int
  ): DragonBallPageResponse<DragonBallCharacterResponse> {
    logger.info("Buscando personagens do Dragon Ball: página $page, limite $limit")
    logger.debug("URL Base: {}, Endpoint: /api/characters", baseUrl)

    return withContext(Dispatchers.IO) {
      try {
        dragonBallWebClient
                .get()
                .uri { uriBuilder ->
                  uriBuilder
                          .path("/api/characters")
                          .queryParam("page", page)
                          .queryParam("limit", limit)
                          .build()
                }
                .retrieve()
                .awaitBody<DragonBallPageResponse<DragonBallCharacterResponse>>()
      } catch (e: WebClientResponseException) {
        logger.error(
                "Erro HTTP ${e.statusCode} ao buscar personagens do Dragon Ball: ${e.message}",
                e
        )
        createEmptyResponse(page, limit)
      } catch (e: Exception) {
        logger.error("Erro ao buscar personagens do Dragon Ball: ${e.message}", e)
        createEmptyResponse(page, limit)
      }
    }
  }

  @CircuitBreaker(name = "dragonballApi", fallbackMethod = "isAvailableFallback")
  @Retry(name = "dragonballApi")
  override suspend fun isAvailable(): Boolean {
    logger.info("Verificando disponibilidade da API do Dragon Ball")
    logger.debug("URL Base: {}, Endpoint: /api/characters?limit=1", baseUrl)

    return withContext(Dispatchers.IO) {
      try {
        val response =
                dragonBallWebClient
                        .get()
                        .uri("/api/characters?limit=1")
                        .retrieve()
                        .awaitBodilessEntity()
        logger.info("Status code da Dragon Ball API: {}", response.statusCode)
        response.statusCode == HttpStatus.OK
      } catch (e: Exception) {
        logger.error("Erro ao verificar disponibilidade da API do Dragon Ball: ${e.message}", e)
        false
      }
    }
  }

  // Método de fallback para getCharacters
  suspend fun getCharactersFallback(
          page: Int,
          limit: Int,
          e: Exception
  ): DragonBallPageResponse<DragonBallCharacterResponse> {
    logger.warn(
            "Fallback acionado para getCharacters com página $page e limite $limit: ${e.message}"
    )
    return createEmptyResponse(page, limit)
  }

  // Método de fallback para isAvailable
  suspend fun isAvailableFallback(e: Exception): Boolean {
    logger.warn("Fallback acionado para isAvailable: ${e.message}")
    return false
  }

  // Método auxiliar para criar resposta vazia
  private fun createEmptyResponse(
          page: Int,
          limit: Int
  ): DragonBallPageResponse<DragonBallCharacterResponse> {
    return DragonBallPageResponse(
            items = emptyList(),
            meta =
                    MetaData(
                            totalItems = 0,
                            itemCount = 0,
                            itemsPerPage = limit,
                            totalPages = 0,
                            currentPage = page
                    )
    )
  }
}
