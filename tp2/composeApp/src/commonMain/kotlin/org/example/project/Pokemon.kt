package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val pokedex_id: String? = null,
    val category: String? = null,
    val name: PokemonName? = null,
    val sprites: Sprites? = null,
)

@Serializable
data class Sprites(
    val regular: String? = null,
    val shiny: String? = null,
    val gmax: String? = null
)

@Serializable
data class PokemonName(
    val fr: String? = null,
    val en: String? = null,
    val jp: String? = null
)