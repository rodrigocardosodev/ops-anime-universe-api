package com.devcorp.ops_anime_universe_api.infrastructure.adapter.primary.rest

import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.PageResponse
import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Controlador para a API REST de personagens */
@RestController
@RequestMapping("/api/characters")
class CharacterController(private val characterUseCase: CharacterUseCase) {

  private val logger = LoggerFactory.getLogger(CharacterController::class.java)

  /**
   * Endpoint para listar personagens de todos os universos
   *
   * @param page Número da página (começando em 0)
   * @param size Tamanho da página (entre 1 e 50)
   * @return Resposta paginada com personagens de todos os universos
   */
  @GetMapping
  suspend fun getCharacters(
          @RequestParam(defaultValue = "0") page: Int,
          @RequestParam(defaultValue = "20") size: Int
  ): ResponseEntity<PageResponse<Character>> {
    logger.info("Recebida requisição para buscar personagens: página $page, tamanho $size")

    val result = characterUseCase.getCharacters(page, size)

    logger.info("Retornando ${result.content.size} personagens")
    return ResponseEntity.ok(result)
  }

  /**
   * Endpoint para verificar se o serviço está respondendo (healthcheck simplificado)
   *
   * @return 200 OK se o serviço estiver disponível
   */
  @GetMapping("/ping")
  suspend fun ping(): ResponseEntity<Map<String, String>> {
    return ResponseEntity.ok(
            mapOf("status" to "UP", "message" to "Serviço de personagens disponível")
    )
  }
}
