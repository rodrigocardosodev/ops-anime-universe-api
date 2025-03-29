package com.devcorp.ops_anime_universe_api.domain.usecase

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.PageResponse
import com.devcorp.ops_anime_universe_api.domain.model.Status
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

/**
 * Caso de uso para orquestrar as chamadas aos serviços de personagens de diferentes universos,
 * aplicando balanceamento de carga e resilência
 */
@Component
class CharacterUseCase(private val characterServices: List<CharacterService>) {
  private val logger = LoggerFactory.getLogger(CharacterUseCase::class.java)

  companion object {
    const val MAX_PAGE_SIZE = 50
    // Estimativa de elementos totais - pode ser ajustada conforme necessário
    private const val ESTIMATED_TOTAL_ELEMENTS = 1000L
  }

  /**
   * Busca personagens de todos os universos de forma paginada e balanceada
   *
   * @param page Número da página (começando em 0)
   * @param size Tamanho da página (máximo 50)
   * @return Resposta paginada com personagens de todos os universos
   */
  @Cacheable(value = ["characters"], key = "#page + '-' + #size")
  suspend fun getCharacters(page: Int, size: Int): PageResponse<Character> {
    val effectiveSize = min(size, MAX_PAGE_SIZE)
    logger.info("Buscando personagens: página $page, tamanho $effectiveSize")

    return withContext(Dispatchers.IO) {
      try {
        // Distribui a carga entre os serviços disponíveis
        val results = fetchCharactersFromAllServices(this, page, effectiveSize)
        PageResponse.of(results, page, effectiveSize, ESTIMATED_TOTAL_ELEMENTS)
      } catch (e: Exception) {
        logger.error("Erro ao buscar personagens", e)
        PageResponse.of(emptyList(), page, effectiveSize, 0)
      }
    }
  }

  /** Busca personagens de todos os serviços com distribuição balanceada */
  private suspend fun fetchCharactersFromAllServices(
          scope: CoroutineScope,
          page: Int,
          size: Int
  ): List<Character> {
    // Calcula o número de personagens a buscar de cada serviço
    val servicesCount = characterServices.size
    val charactersPerService = size / servicesCount
    val remainder = size % servicesCount

    // Cria uma tarefa assíncrona para cada serviço com tamanho calculado
    val deferredResults =
            characterServices.mapIndexed { index, service ->
              scope.async {
                val serviceSize = charactersPerService + (if (index < remainder) 1 else 0)
                val servicePage =
                        calculateServicePage(page, size, serviceSize, index, servicesCount)

                try {
                  service.getCharacters(servicePage, serviceSize)
                } catch (e: Exception) {
                  logger.error("Erro ao buscar personagens do universo ${service.getUniverse()}", e)
                  emptyList()
                }
              }
            }

    // Aguarda os resultados e os combina
    return deferredResults.awaitAll().flatten()
  }

  /**
   * Verifica a disponibilidade de todos os serviços
   *
   * @return Mapa com os nomes dos serviços e seus status
   */
  suspend fun checkServicesAvailability(): Map<String, Status> {
    logger.info("Verificando disponibilidade dos serviços")

    return withContext(Dispatchers.IO) {
      try {
        val deferredResults =
                characterServices.map { service ->
                  this.async { checkServiceAvailability(service) }
                }

        deferredResults.awaitAll().toMap()
      } catch (e: Exception) {
        logger.error("Erro ao verificar disponibilidade dos serviços", e)
        characterServices.associate { it.getUniverse().name to Status.UNKNOWN }
      }
    }
  }

  /** Verifica a disponibilidade de um serviço específico */
  private suspend fun checkServiceAvailability(service: CharacterService): Pair<String, Status> {
    val universe = service.getUniverse()
    val available =
            try {
              service.isAvailable()
            } catch (e: Exception) {
              logger.error("Erro ao verificar disponibilidade do serviço ${universe.name}", e)
              false
            }
    return universe.name to if (available) Status.UP else Status.DOWN
  }

  /**
   * Calcula a página a ser solicitada para um serviço específico considerando a distribuição
   * balanceada entre serviços
   */
  private fun calculateServicePage(
          page: Int,
          pageSize: Int,
          serviceSize: Int,
          serviceIndex: Int,
          servicesCount: Int
  ): Int {
    val globalOffset = page * pageSize
    val serviceOffset = (globalOffset + serviceIndex * serviceSize) / serviceSize
    return serviceOffset
  }
}
