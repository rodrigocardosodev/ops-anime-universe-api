package com.devcorp.ops_anime_universe_api.infrastructure.config

import java.util.Collections
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.codec.ServerCodecConfigurer

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
}
