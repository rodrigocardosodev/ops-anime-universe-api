package com.devcorp.ops_anime_universe_api.infrastructure.config

import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/** Handler global para exceções da aplicação */
@ControllerAdvice
class GlobalExceptionHandler {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

        /** DTO para representar um erro na resposta */
        data class ErrorResponse(
                val timestamp: LocalDateTime = LocalDateTime.now(),
                val status: Int,
                val error: String,
                val message: String,
                val path: String?
        )

        /** Trata exceções gerais */
        @ExceptionHandler(Exception::class)
        fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
                logger.error("Erro não tratado na aplicação", ex)

                val errorResponse =
                        ErrorResponse(
                                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                error = HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                                message = ex.message ?: "Erro interno no servidor",
                                path = null
                        )

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }

        /** Trata exceções de status específico */
        @ExceptionHandler(ResponseStatusException::class)
        fun handleResponseStatusException(
                ex: ResponseStatusException
        ): ResponseEntity<ErrorResponse> {
                logger.warn("Erro de status HTTP: {}", ex.statusCode)

                val errorResponse =
                        ErrorResponse(
                                status = ex.statusCode.value(),
                                error = ex.reason ?: ex.statusCode.toString(),
                                message = ex.message,
                                path = null
                        )

                return ResponseEntity.status(ex.statusCode).body(errorResponse)
        }

        /** Trata exceções de método HTTP não suportado */
        @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
        fun handleMethodNotSupported(
                ex: HttpRequestMethodNotSupportedException
        ): ResponseEntity<ErrorResponse> {
                logger.warn("Método não suportado: {}", ex.method)

                val errorResponse =
                        ErrorResponse(
                                status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                                error = HttpStatus.METHOD_NOT_ALLOWED.toString(),
                                message =
                                        "Método ${ex.method} não é suportado para este endpoint. " +
                                                "Métodos suportados: ${ex.supportedMethods?.joinToString(", ") ?: "nenhum"}",
                                path = null
                        )

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse)
        }

        /** Trata exceções do WebClient */
        @ExceptionHandler(WebClientException::class)
        fun handleWebClientException(ex: WebClientException): ResponseEntity<ErrorResponse> {
                val isResponseException = ex is WebClientResponseException
                val status =
                        if (isResponseException) {
                                (ex as WebClientResponseException).statusCode
                        } else {
                                HttpStatus.BAD_GATEWAY
                        }

                val message =
                        if (isResponseException) {
                                "Erro ao chamar API externa: ${(ex as WebClientResponseException).statusText}"
                        } else {
                                "Erro ao chamar API externa: ${ex.message}"
                        }

                logger.error("Erro na chamada WebClient: {}", message, ex)

                val errorResponse =
                        ErrorResponse(
                                status = status.value(),
                                error = status.toString(),
                                message = message,
                                path = null
                        )

                return ResponseEntity.status(status).body(errorResponse)
        }
}
