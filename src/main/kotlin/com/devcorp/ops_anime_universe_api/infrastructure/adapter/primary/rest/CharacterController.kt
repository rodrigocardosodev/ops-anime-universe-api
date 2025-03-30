package com.devcorp.ops_anime_universe_api.infrastructure.adapter.primary.rest

import com.devcorp.ops_anime_universe_api.domain.model.GroupedPageResponse
import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Controlador para a API REST de personagens */
@RestController
@RequestMapping("/api/v1/characters")
class CharacterController(private val characterUseCase: CharacterUseCase) {

        private val logger = LoggerFactory.getLogger(CharacterController::class.java)

        /**
         * Endpoint para listar personagens de todos os universos
         *
         * @param page Número da página (começando em 0)
         * @param size Tamanho da página (entre 1 e 100, limitado a esse valor)
         * @return Resposta paginada com personagens agrupados por universo
         */
        @GetMapping
        suspend fun getCharacters(
                @RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "10") size: Int
        ): ResponseEntity<GroupedPageResponse> {
                logger.info(
                        "Recebida requisição para buscar personagens: página $page, tamanho $size"
                )

                // Limitar o tamanho da página entre 1 e 100
                val adjustedSize =
                        when {
                                size < 1 -> 1
                                size > 100 -> 100
                                else -> size
                        }

                // Se size > 50, retornaremos exatamente 50 elementos, mas com size = 50 no response
                val finalSize = if (adjustedSize > 50) 50 else adjustedSize

                // Utilizamos o método de busca agrupada diretamente
                val result = characterUseCase.getGroupedCharacters(page, adjustedSize)

                val dragonballSize = result.content["dragonball"]?.size ?: 0
                val pokemonSize = result.content["pokemon"]?.size ?: 0

                // Se o tamanho solicitado for maior que 50, criamos um novo objeto response com
                // size=50
                val finalResult =
                        if (adjustedSize > 50) {
                                GroupedPageResponse(
                                        result.content,
                                        result.page,
                                        finalSize,
                                        result.totalPage,
                                        result.totalElements,
                                        result.totalPages,
                                        result.dragonBallTotalPages,
                                        result.pokemonTotalPages
                                )
                        } else {
                                result
                        }

                logger.info(
                        "Retornando ${dragonballSize + pokemonSize} personagens (${dragonballSize} Dragon Ball, ${pokemonSize} Pokémon)"
                )
                return ResponseEntity.ok(finalResult)
        }

        /**
         * Endpoint para listar personagens agrupados por universo
         *
         * @param page Número da página (começando em 0)
         * @param size Tamanho da página (entre 1 e 50)
         * @return Resposta paginada com personagens agrupados por universo
         */
        @GetMapping("/grouped")
        suspend fun getGroupedCharacters(
                @RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "20") size: Int
        ): ResponseEntity<GroupedPageResponse> {
                logger.info(
                        "Recebida requisição para buscar personagens agrupados: página $page, tamanho $size"
                )

                val result = characterUseCase.getGroupedCharacters(page, size)

                // Obtém o tamanho dos arrays de personagens na estrutura de mapa
                val dragonballSize = result.content["dragonball"]?.size ?: 0
                val pokemonSize = result.content["pokemon"]?.size ?: 0

                logger.info(
                        "Retornando personagens agrupados ($dragonballSize de Dragon Ball, $pokemonSize de Pokémon)"
                )
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
