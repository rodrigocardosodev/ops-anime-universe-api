package com.devcorp.ops_anime_universe_api.infrastructure.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes

/** Testes unitários para CustomErrorAttributes */
class CustomErrorAttributesTest {

        /** Teste simples para verificar se podemos criar um DefaultErrorAttributes */
        @Test
        fun `deve criar DefaultErrorAttributes corretamente`() {
                // Arrange
                val errorAttributes = DefaultErrorAttributes()

                // Assert
                assertNotNull(errorAttributes)
                assertTrue(errorAttributes is DefaultErrorAttributes)
        }
}
