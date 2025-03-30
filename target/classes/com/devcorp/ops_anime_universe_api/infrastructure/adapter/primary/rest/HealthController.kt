package com.devcorp.ops_anime_universe_api.infrastructure.adapter.primary.rest

import com.devcorp.ops_anime_universe_api.domain.usecase.CharacterUseCase
import com.devcorp.ops_anime_universe_api.domain.model.HealthStatus
import com.devcorp.ops_anime_universe_api.domain.model.Status
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Controlador para o endpoint de health check */
@RestController
@RequestMapping("/health")
class HealthController(private val characterUseCase: CharacterUseCase) {
  /**
   * Endpoint para verificar o status de saúde da aplicação
   * @return Status de saúde da aplicação e dos serviços externos
   */
  @GetMapping
  suspend fun healthCheck(): ResponseEntity<HealthStatus> {
    val serviceStatuses = characterUseCase.checkServicesAvailability()

    // A aplicação está UP se pelo menos um serviço estiver disponível
    val applicationStatus =
            if (serviceStatuses.values.any { it == Status.UP }) {
              Status.UP
            } else {
              Status.DOWN
            }

    val healthStatus = HealthStatus(status = applicationStatus, services = serviceStatuses)

    return ResponseEntity.ok(healthStatus)
  }
}
