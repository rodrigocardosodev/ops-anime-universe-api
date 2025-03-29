package com.devcorp.ops_anime_universe_api.domain.model

import java.time.LocalDateTime

/** Modelo de domínio que representa o status de saúde da aplicação */
data class HealthStatus(
        val status: Status,
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val services: Map<String, Status>
)

/** Enum que define os possíveis status de saúde */
enum class Status {
  UP,
  DOWN,
  UNKNOWN
}
