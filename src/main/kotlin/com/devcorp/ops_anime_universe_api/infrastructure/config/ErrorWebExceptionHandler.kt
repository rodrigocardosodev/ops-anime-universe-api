package com.devcorp.ops_anime_universe_api.infrastructure.config

import java.time.LocalDateTime
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

/** Configuração para tratamento de erros no WebFlux */
@Configuration
class ErrorWebFluxConfiguration {

  /** Bean necessário para o handler de erros do WebFlux */
  @Bean
  fun resources(): WebProperties.Resources {
    return WebProperties().resources
  }

  /** Bean para configurar os codecs do servidor */
  @Bean
  fun serverCodecConfigurer(): ServerCodecConfigurer {
    return ServerCodecConfigurer.create()
  }
}

/** Atributos de erro personalizados */
@Component
class CustomErrorAttributes : DefaultErrorAttributes() {
  override fun getErrorAttributes(
          request: ServerRequest,
          options: ErrorAttributeOptions
  ): Map<String, Any> {
    val errorAttributes = super.getErrorAttributes(request, options)
    val path = request.uri().path
    val status = errorAttributes["status"] as Int? ?: 500

    // Se for erro 404, personaliza a mensagem
    if (status == 404) {
      errorAttributes["message"] = "Recurso não encontrado: $path"
      errorAttributes["timestamp"] = LocalDateTime.now().toString()
    }

    return errorAttributes
  }
}

/** Handler de erro global para WebFlux */
@Component
@Order(-2) // Garante maior precedência que o handler padrão
class GlobalErrorWebExceptionHandler(
        errorAttributes: ErrorAttributes,
        webProperties: WebProperties,
        applicationContext: ApplicationContext,
        serverCodecConfigurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {

  init {
    super.setMessageWriters(serverCodecConfigurer.writers)
    super.setMessageReaders(serverCodecConfigurer.readers)
  }

  override fun getRoutingFunction(
          errorAttributes: ErrorAttributes
  ): RouterFunction<ServerResponse> {
    return RouterFunctions.route(RequestPredicates.all()) { request ->
      renderErrorResponse(request)
    }
  }

  private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
    val errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults())
    val status = errorPropertiesMap["status"] as Int? ?: 500

    return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorPropertiesMap))
  }
}
