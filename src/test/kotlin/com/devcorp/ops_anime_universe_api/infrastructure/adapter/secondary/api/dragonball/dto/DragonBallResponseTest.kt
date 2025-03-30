package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.dragonball.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DragonBallResponseTest {

  @Test
  fun `DragonBallPageResponse should hold correct values`() {
    // Arrange
    val items = listOf(DragonBallCharacterResponse(id = "1", name = "Goku"))
    val meta =
            MetaData(
                    totalItems = 100,
                    itemCount = 1,
                    itemsPerPage = 10,
                    totalPages = 10,
                    currentPage = 1
            )

    // Act
    val response = DragonBallPageResponse(items, meta)

    // Assert
    assertEquals(items, response.items)
    assertEquals(meta, response.meta)
  }

  @Test
  fun `MetaData should hold correct values`() {
    // Arrange
    val totalItems = 100L
    val itemCount = 10
    val itemsPerPage = 10
    val totalPages = 10
    val currentPage = 1

    // Act
    val meta =
            MetaData(
                    totalItems = totalItems,
                    itemCount = itemCount,
                    itemsPerPage = itemsPerPage,
                    totalPages = totalPages,
                    currentPage = currentPage
            )

    // Assert
    assertEquals(totalItems, meta.totalItems)
    assertEquals(itemCount, meta.itemCount)
    assertEquals(itemsPerPage, meta.itemsPerPage)
    assertEquals(totalPages, meta.totalPages)
    assertEquals(currentPage, meta.currentPage)
  }

  @Test
  fun `DragonBallCharacterResponse should hold correct values`() {
    // Arrange
    val id = "1"
    val name = "Goku"
    val ki = "10000"
    val maxKi = "15000"
    val race = "Saiyan"
    val gender = "Male"
    val description = "Main character"
    val image = "https://example.com/goku.jpg"

    // Act
    val character =
            DragonBallCharacterResponse(
                    id = id,
                    name = name,
                    ki = ki,
                    maxKi = maxKi,
                    race = race,
                    gender = gender,
                    description = description,
                    image = image
            )

    // Assert
    assertEquals(id, character.id)
    assertEquals(name, character.name)
    assertEquals(ki, character.ki)
    assertEquals(maxKi, character.maxKi)
    assertEquals(race, character.race)
    assertEquals(gender, character.gender)
    assertEquals(description, character.description)
    assertEquals(image, character.image)
  }

  @Test
  fun `DragonBallCharacterResponse should allow null optional fields`() {
    // Arrange
    val id = "1"
    val name = "Goku"

    // Act
    val character = DragonBallCharacterResponse(id = id, name = name)

    // Assert
    assertEquals(id, character.id)
    assertEquals(name, character.name)
    assertEquals(null, character.ki)
    assertEquals(null, character.maxKi)
    assertEquals(null, character.race)
    assertEquals(null, character.gender)
    assertEquals(null, character.description)
    assertEquals(null, character.image)
  }

  @Test
  fun `DragonBallPageResponse equals and hashCode should work correctly`() {
    // Arrange
    val items1 = listOf(DragonBallCharacterResponse(id = "1", name = "Goku"))
    val meta1 = MetaData(100, 1, 10, 10, 1)
    val response1 = DragonBallPageResponse(items1, meta1)

    val items2 = listOf(DragonBallCharacterResponse(id = "1", name = "Goku"))
    val meta2 = MetaData(100, 1, 10, 10, 1)
    val response2 = DragonBallPageResponse(items2, meta2)

    // Act & Assert
    assertEquals(response1, response2)
    assertEquals(response1.hashCode(), response2.hashCode())
  }

  @Test
  fun `MetaData equals and hashCode should work correctly`() {
    // Arrange
    val meta1 = MetaData(100, 1, 10, 10, 1)
    val meta2 = MetaData(100, 1, 10, 10, 1)

    // Act & Assert
    assertEquals(meta1, meta2)
    assertEquals(meta1.hashCode(), meta2.hashCode())
  }

  @Test
  fun `DragonBallCharacterResponse equals and hashCode should work correctly`() {
    // Arrange
    val char1 = DragonBallCharacterResponse(id = "1", name = "Goku", ki = "10000", race = "Saiyan")

    val char2 = DragonBallCharacterResponse(id = "1", name = "Goku", ki = "10000", race = "Saiyan")

    // Act & Assert
    assertEquals(char1, char2)
    assertEquals(char1.hashCode(), char2.hashCode())
  }

  @Test
  fun `toString should return readable representation`() {
    // Arrange
    val character = DragonBallCharacterResponse(id = "1", name = "Goku", ki = "10000")

    // Act
    val toString = character.toString()

    // Assert
    assertEquals(
            "DragonBallCharacterResponse(id=1, name=Goku, ki=10000, maxKi=null, race=null, " +
                    "gender=null, description=null, image=null)",
            toString
    )
  }
}
