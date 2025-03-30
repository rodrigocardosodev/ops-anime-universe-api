package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LinksTest {

  @Test
  fun `Links class should handle all properties correctly`() {
    // Criando uma instância da classe Links com todos os parâmetros
    val links =
            Links(
                    first = "https://api.example.com/first",
                    previous = "https://api.example.com/previous",
                    next = "https://api.example.com/next",
                    last = "https://api.example.com/last"
            )

    // Verificando se os getters retornam os valores corretos
    assertEquals("https://api.example.com/first", links.first)
    assertEquals("https://api.example.com/previous", links.previous)
    assertEquals("https://api.example.com/next", links.next)
    assertEquals("https://api.example.com/last", links.last)

    // Testando com valores nulos
    val nullLinks = Links(first = null, previous = null, next = null, last = null)

    assertNull(nullLinks.first)
    assertNull(nullLinks.previous)
    assertNull(nullLinks.next)
    assertNull(nullLinks.last)
  }
}
