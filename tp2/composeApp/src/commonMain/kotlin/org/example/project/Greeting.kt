package org.example.project

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.collections.get

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchPokemon(): String? {
        val random = (1..1025).random()
        val response: Pokemon = client.get("https://tyradex.vercel.app/api/v1/pokemon/$random").body()
        client.close()
        return response.name?.fr ?: "Unknown"
    }

    suspend fun fetchPokemonData(): Pokemon {
        val random = (1..1025).random()
        val baseUrl = getApiBaseUrl()
        val url = "$baseUrl/api/v1/pokemon/$random"
        
        var lastException: Exception? = null
        repeat(3) { attempt ->
            try {
                val response: Pokemon = client.get(url).body()
                return response
            } catch (e: Exception) {
                lastException = e as? Exception ?: Exception(e.message)
                if (attempt < 2) {
                    delay((attempt + 1) * 200L)
                }
            }
        }
        
        throw lastException ?: Exception("Échec de la connexion après 3 tentatives")
    }
}