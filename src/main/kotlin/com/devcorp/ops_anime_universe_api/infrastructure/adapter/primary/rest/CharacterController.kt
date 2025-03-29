package com.devcorp.ops_anime_universe_api.infrastructure.adapter.primary.rest

import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import com.devcorp.ops_anime_universe_api.domain.model.Character
import com.devcorp.ops_anime_universe_api.domain.model.PageResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Controlador para a API REST de personagens */
@RestController
@RequestMapping("/api/characters")
class CharacterController(private val characterUseCase: CharacterUseCase) {
  /**
   * Endpoint para listar personagens de todos os universos
   * @param page Número da página (começando em 0)
   * @param size Tamanho da página (máximo 50)
   * @return Resposta paginada com personagens de todos os universos
   */
  @GetMapping
  suspend fun getCharacters(
          @RequestParam(defaultValue = "0") page: Int,
          @RequestParam(defaultValue = "20") size: Int
  ): ResponseEntity<PageResponse<Character>> {
    val result = characterUseCase.getCharacters(page, size)
    return ResponseEntity.ok(result)
  }
}
