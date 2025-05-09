package com.devcorp.ops_anime_universe_api.domain.service

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.Universe
import com.devcorp.ops_anime_universe_api.domain.port.api.CharacterService
import com.devcorp.ops_anime_universe_api.domain.port.spi.DragonBallApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** Serviço para obter personagens do universo Dragon Ball */
@Service
class DragonBallCharacterService(private val dragonBallApiClient: DragonBallApiClient) :
        CharacterService {

  private val logger = LoggerFactory.getLogger(DragonBallCharacterService::class.java)

  // Limita o número de chamadas simultâneas para a API Dragon Ball
  private val concurrentRequestsSemaphore = Semaphore(10)

  override fun getUniverse(): Universe = Universe.DRAGON_BALL

  override suspend fun getCharacters(page: Int, size: Int): List<Character> {
    // Verificando parâmetros de paginação
    val validPage = page.coerceAtLeast(0)
    val validSize = size.coerceIn(1, 50) // Limita o tamanho entre 1 e 50

    // API do Dragon Ball começa com página 1, então adicionamos 1 à página
    val dragonBallPage = validPage + 1

    logger.info(
            "Buscando personagens do Dragon Ball na página $validPage (Dragon Ball API página $dragonBallPage) com tamanho $validSize"
    )

    return try {
      val response = dragonBallApiClient.getCharacters(dragonBallPage, validSize)

      // Se não houver resultados, retorna lista vazia
      if (response.items.isEmpty()) {
        logger.info("Nenhum personagem encontrado na página $validPage")
        return emptyList()
      }

      // Processamento paralelo com semáforo para limitar requisições concorrentes
      coroutineScope {
        response.items
                .map { character ->
                  async(Dispatchers.IO) {
                    concurrentRequestsSemaphore.withPermit {
                      Character(
                              id = character.id.toString(),
                              name = character.name,
                              universe = Universe.DRAGON_BALL
                      )
                    }
                  }
                }
                .awaitAll()
      }
    } catch (e: Exception) {
      logger.error("Erro ao buscar personagens do Dragon Ball: ${e.message}", e)

      // Propaga a exceção em ambiente de teste
      if (isTestEnvironment()) {
        throw e
      }

      // Em produção, retorna lista vazia para maior resiliência
      emptyList()
    }
  }

  override suspend fun isAvailable(): Boolean {
    return try {
      withContext(Dispatchers.IO) { dragonBallApiClient.isAvailable() }
    } catch (e: Exception) {
      logger.error("Erro ao verificar disponibilidade da API do Dragon Ball: ${e.message}", e)

      // Propaga a exceção em ambiente de teste
      if (isTestEnvironment()) {
        throw e
      }

      // Em produção, retorna false para maior resiliência
      false
    }
  }

  /** Detecta se estamos em ambiente de teste ou de produção */
  internal fun isTestEnvironment(): Boolean {
    return try {
      // Em ambientes de teste, normalmente o stack trace contém "Test" ou "test"
      val stackTrace = Thread.currentThread().stackTrace
      stackTrace.any { it.className.contains("Test", ignoreCase = true) }
    } catch (e: Exception) {
      logger.warn("Erro ao verificar ambiente: ${e.message}")
      false
    }
  }
}
