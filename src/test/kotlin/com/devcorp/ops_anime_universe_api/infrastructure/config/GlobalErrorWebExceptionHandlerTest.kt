package com.devcorp.ops_anime_universe_api.infrastructure.config

import java.util.Collections
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/** Testes unitários para GlobalErrorWebExceptionHandler */
class GlobalErrorWebExceptionHandlerTest {

        /** Testa a criação do GlobalErrorWebExceptionHandler */
        @Test
        fun `deve criar GlobalErrorWebExceptionHandler corretamente`() {
                // Arrange
                val errorAttributes = mock(ErrorAttributes::class.java)
                val webProperties = WebProperties()
                val applicationContext = mock(ApplicationContext::class.java)

                // Configurando o ClassLoader para resolver o problema
                `when`(applicationContext.classLoader).thenReturn(this.javaClass.classLoader)

                val serverCodecConfigurer = mock(ServerCodecConfigurer::class.java)

                // Configurando listas vazias para os readers e writers com tipos corretos
                val readers = Collections.emptyList<HttpMessageReader<*>>()
                val writers = Collections.emptyList<HttpMessageWriter<*>>()

                `when`(serverCodecConfigurer.readers).thenReturn(readers)
                `when`(serverCodecConfigurer.writers).thenReturn(writers)

                // Act
                val handler =
                        GlobalErrorWebExceptionHandler(
                                errorAttributes,
                                webProperties,
                                applicationContext,
                                serverCodecConfigurer
                        )

                // Assert
                assertNotNull(handler)
        }

        /** Verificando as propriedades depois de inicializar o handler */
        @Test
        fun `deve inicializar o handler com as propriedades corretas`() {
                // Arrange
                val errorAttributes = mock(ErrorAttributes::class.java)
                val webProperties = WebProperties()
                val applicationContext = mock(ApplicationContext::class.java)

                // Configurando o ClassLoader para resolver o problema
                `when`(applicationContext.classLoader).thenReturn(this.javaClass.classLoader)

                val serverCodecConfigurer = mock(ServerCodecConfigurer::class.java)

                // Configurando listas vazias para os readers e writers com tipos corretos
                val readers = Collections.emptyList<HttpMessageReader<*>>()
                val writers = Collections.emptyList<HttpMessageWriter<*>>()

                `when`(serverCodecConfigurer.readers).thenReturn(readers)
                `when`(serverCodecConfigurer.writers).thenReturn(writers)

                // Act
                val handler =
                        GlobalErrorWebExceptionHandler(
                                errorAttributes,
                                webProperties,
                                applicationContext,
                                serverCodecConfigurer
                        )

                // Assert
                assertNotNull(handler)

                // Verificando que o handler possui o método getRoutingFunction
                assertTrue(
                        handler.javaClass.declaredMethods.any {
                                it.name.contains("getRoutingFunction")
                        }
                )
        }

        /** Testa o método getRoutingFunction usando reflection */
        @Test
        fun `getRoutingFunction deve retornar uma RouterFunction válida`() {
                // Arrange
                val errorAttributes = mock(ErrorAttributes::class.java)
                val webProperties = WebProperties()
                val applicationContext = mock(ApplicationContext::class.java)
                `when`(applicationContext.classLoader).thenReturn(this.javaClass.classLoader)

                val serverCodecConfigurer = mock(ServerCodecConfigurer::class.java)
                val readers = Collections.emptyList<HttpMessageReader<*>>()
                val writers = Collections.emptyList<HttpMessageWriter<*>>()
                `when`(serverCodecConfigurer.readers).thenReturn(readers)
                `when`(serverCodecConfigurer.writers).thenReturn(writers)

                // Criando mapa de erro para simular o comportamento
                val errorMap =
                        mapOf(
                                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "error" to "Erro Interno",
                                "message" to "Ocorreu um erro"
                        )

                // Mock do ErrorAttributes para retornar nosso mapa de erro
                `when`(errorAttributes.getErrorAttributes(any(), any())).thenReturn(errorMap)

                val handler =
                        GlobalErrorWebExceptionHandler(
                                errorAttributes,
                                webProperties,
                                applicationContext,
                                serverCodecConfigurer
                        )

                // Act - usando reflection para acessar o método protegido
                val getRoutingFunctionMethod =
                        handler.javaClass.getDeclaredMethod(
                                "getRoutingFunction",
                                ErrorAttributes::class.java
                        )
                getRoutingFunctionMethod.isAccessible = true
                val routerFunction = getRoutingFunctionMethod.invoke(handler, errorAttributes)

                // Assert
                assertNotNull(routerFunction)
                assertTrue(routerFunction is RouterFunction<*>)
        }

        /** Testa o renderErrorResponse para status 500 */
        @Test
        fun `renderErrorResponse deve retornar resposta com status 500 quando ocorrer erro interno`() {
                // Arrange
                val errorAttributes = mock(ErrorAttributes::class.java)
                val webProperties = WebProperties()
                val applicationContext = mock(ApplicationContext::class.java)
                `when`(applicationContext.classLoader).thenReturn(this.javaClass.classLoader)

                val serverCodecConfigurer = mock(ServerCodecConfigurer::class.java)
                val readers = Collections.emptyList<HttpMessageReader<*>>()
                val writers = Collections.emptyList<HttpMessageWriter<*>>()
                `when`(serverCodecConfigurer.readers).thenReturn(readers)
                `when`(serverCodecConfigurer.writers).thenReturn(writers)

                // Criando um mapa com erro 500
                val errorMap =
                        mapOf(
                                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "error" to "Erro Interno",
                                "message" to "Ocorreu um erro interno"
                        )

                // Mock do ServerRequest
                val serverRequest = mock(ServerRequest::class.java)

                // Mock do ErrorAttributes para retornar o mapa de erro
                `when`(errorAttributes.getErrorAttributes(eq(serverRequest), any()))
                        .thenReturn(errorMap)

                val handler =
                        GlobalErrorWebExceptionHandler(
                                errorAttributes,
                                webProperties,
                                applicationContext,
                                serverCodecConfigurer
                        )

                // Act - usar reflection para acessar o método privado
                val renderErrorResponseMethod =
                        handler.javaClass.getDeclaredMethod(
                                "renderErrorResponse",
                                ServerRequest::class.java
                        )
                renderErrorResponseMethod.isAccessible = true
                val response =
                        renderErrorResponseMethod.invoke(handler, serverRequest) as
                                Mono<ServerResponse>

                // Assert
                assertNotNull(response)

                // Verificar o status da resposta
                StepVerifier.create(response)
                        .expectNextMatches { serverResponse ->
                                serverResponse.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR
                        }
                        .verifyComplete()
        }

        /** Testa o renderErrorResponse para status 404 */
        @Test
        fun `renderErrorResponse deve retornar resposta com status 404 quando recurso não for encontrado`() {
                // Arrange
                val errorAttributes = mock(ErrorAttributes::class.java)
                val webProperties = WebProperties()
                val applicationContext = mock(ApplicationContext::class.java)
                `when`(applicationContext.classLoader).thenReturn(this.javaClass.classLoader)

                val serverCodecConfigurer = mock(ServerCodecConfigurer::class.java)
                val readers = Collections.emptyList<HttpMessageReader<*>>()
                val writers = Collections.emptyList<HttpMessageWriter<*>>()
                `when`(serverCodecConfigurer.readers).thenReturn(readers)
                `when`(serverCodecConfigurer.writers).thenReturn(writers)

                // Criando um mapa com erro 404
                val errorMap =
                        mapOf(
                                "status" to HttpStatus.NOT_FOUND.value(),
                                "error" to "Não Encontrado",
                                "message" to "Recurso não encontrado"
                        )

                // Mock do ServerRequest
                val serverRequest = mock(ServerRequest::class.java)

                // Mock do ErrorAttributes para retornar o mapa de erro
                `when`(errorAttributes.getErrorAttributes(eq(serverRequest), any()))
                        .thenReturn(errorMap)

                val handler =
                        GlobalErrorWebExceptionHandler(
                                errorAttributes,
                                webProperties,
                                applicationContext,
                                serverCodecConfigurer
                        )

                // Act - usar reflection para acessar o método privado
                val renderErrorResponseMethod =
                        handler.javaClass.getDeclaredMethod(
                                "renderErrorResponse",
                                ServerRequest::class.java
                        )
                renderErrorResponseMethod.isAccessible = true
                val response =
                        renderErrorResponseMethod.invoke(handler, serverRequest) as
                                Mono<ServerResponse>

                // Assert
                assertNotNull(response)

                // Verificar o status da resposta
                StepVerifier.create(response)
                        .expectNextMatches { serverResponse ->
                                serverResponse.statusCode() == HttpStatus.NOT_FOUND
                        }
                        .verifyComplete()
        }
}
