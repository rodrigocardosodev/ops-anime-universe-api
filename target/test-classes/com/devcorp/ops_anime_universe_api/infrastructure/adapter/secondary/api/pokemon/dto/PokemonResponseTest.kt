package com.devcorp.ops_anime_universe_api.infrastructure.adapter.secondary.api.pokemon.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PokemonResponseTest {

  @Test
  fun `PokemonPageResponse should hold correct values`() {
    // Arrange
    val count = 1118L
    val next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"
    val previous = null
    val results =
            listOf(
                    PokemonBasicInfo("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                    PokemonBasicInfo("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )

    // Act
    val response = PokemonPageResponse(count, next, previous, results)

    // Assert
    assertEquals(count, response.count)
    assertEquals(next, response.next)
    assertEquals(previous, response.previous)
    assertEquals(results, response.results)
  }

  @Test
  fun `PokemonBasicInfo should hold correct values`() {
    // Arrange
    val name = "bulbasaur"
    val url = "https://pokeapi.co/api/v2/pokemon/1/"

    // Act
    val info = PokemonBasicInfo(name, url)

    // Assert
    assertEquals(name, info.name)
    assertEquals(url, info.url)
  }

  @Test
  fun `PokemonDetailResponse should hold correct values`() {
    // Arrange
    val id = 1
    val name = "bulbasaur"
    val height = 7
    val weight = 69
    val sprites =
            PokemonSprites(
                    front_default =
                            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                    front_shiny = null,
                    back_default = null,
                    back_shiny = null
            )
    val types =
            listOf(
                    PokemonTypeSlot(
                            slot = 1,
                            type =
                                    NamedApiResource(
                                            name = "grass",
                                            url = "https://pokeapi.co/api/v2/type/12/"
                                    )
                    )
            )
    val species =
            NamedApiResource(
                    name = "bulbasaur",
                    url = "https://pokeapi.co/api/v2/pokemon-species/1/"
            )
    val abilities =
            listOf(
                    PokemonAbility(
                            is_hidden = false,
                            slot = 1,
                            ability =
                                    NamedApiResource(
                                            name = "overgrow",
                                            url = "https://pokeapi.co/api/v2/ability/65/"
                                    )
                    )
            )

    // Act
    val detail = PokemonDetailResponse(id, name, height, weight, sprites, types, species, abilities)

    // Assert
    assertEquals(id, detail.id)
    assertEquals(name, detail.name)
    assertEquals(height, detail.height)
    assertEquals(weight, detail.weight)
    assertEquals(sprites, detail.sprites)
    assertEquals(types, detail.types)
    assertEquals(species, detail.species)
    assertEquals(abilities, detail.abilities)
  }

  @Test
  fun `PokemonSprites should hold correct values`() {
    // Arrange
    val frontDefault =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
    val frontShiny =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/1.png"
    val backDefault =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/1.png"
    val backShiny =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/shiny/1.png"

    // Act
    val sprites = PokemonSprites(frontDefault, frontShiny, backDefault, backShiny)

    // Assert
    assertEquals(frontDefault, sprites.front_default)
    assertEquals(frontShiny, sprites.front_shiny)
    assertEquals(backDefault, sprites.back_default)
    assertEquals(backShiny, sprites.back_shiny)
  }

  @Test
  fun `PokemonTypeSlot should hold correct values`() {
    // Arrange
    val slot = 1
    val type = NamedApiResource("grass", "https://pokeapi.co/api/v2/type/12/")

    // Act
    val typeSlot = PokemonTypeSlot(slot, type)

    // Assert
    assertEquals(slot, typeSlot.slot)
    assertEquals(type, typeSlot.type)
  }

  @Test
  fun `PokemonAbility should hold correct values`() {
    // Arrange
    val isHidden = false
    val slot = 1
    val ability = NamedApiResource("overgrow", "https://pokeapi.co/api/v2/ability/65/")

    // Act
    val pokemonAbility = PokemonAbility(isHidden, slot, ability)

    // Assert
    assertEquals(isHidden, pokemonAbility.is_hidden)
    assertEquals(slot, pokemonAbility.slot)
    assertEquals(ability, pokemonAbility.ability)
  }

  @Test
  fun `NamedApiResource should hold correct values`() {
    // Arrange
    val name = "grass"
    val url = "https://pokeapi.co/api/v2/type/12/"

    // Act
    val resource = NamedApiResource(name, url)

    // Assert
    assertEquals(name, resource.name)
    assertEquals(url, resource.url)
  }

  @Test
  fun `equals and hashCode should work correctly for all DTOs`() {
    // PokemonBasicInfo
    val basicInfo1 = PokemonBasicInfo("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
    val basicInfo2 = PokemonBasicInfo("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
    assertEquals(basicInfo1, basicInfo2)
    assertEquals(basicInfo1.hashCode(), basicInfo2.hashCode())

    // NamedApiResource
    val resource1 = NamedApiResource("grass", "https://pokeapi.co/api/v2/type/12/")
    val resource2 = NamedApiResource("grass", "https://pokeapi.co/api/v2/type/12/")
    assertEquals(resource1, resource2)
    assertEquals(resource1.hashCode(), resource2.hashCode())

    // PokemonSprites
    val sprites1 = PokemonSprites("front", "shiny", "back", "backShiny")
    val sprites2 = PokemonSprites("front", "shiny", "back", "backShiny")
    assertEquals(sprites1, sprites2)
    assertEquals(sprites1.hashCode(), sprites2.hashCode())
  }

  @Test
  fun `toString should return readable representation`() {
    // PokemonBasicInfo
    val basicInfo = PokemonBasicInfo("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
    val basicInfoString = basicInfo.toString()
    assertEquals(
            "PokemonBasicInfo(name=bulbasaur, url=https://pokeapi.co/api/v2/pokemon/1/)",
            basicInfoString
    )

    // PokemonSprites
    val sprites = PokemonSprites("front", null, null, null)
    val spritesString = sprites.toString()
    assertEquals(
            "PokemonSprites(front_default=front, front_shiny=null, back_default=null, back_shiny=null)",
            spritesString
    )
  }
}
