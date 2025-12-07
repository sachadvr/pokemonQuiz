package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform

enum class GameState {
    PLAYING,
    ENTER_NAME,
    LEADERBOARD
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var pokemon by remember { mutableStateOf<Pokemon?>(null) }
        var guessText by remember { mutableStateOf("") }
        var message by remember { mutableStateOf<String?>(null) }
        var score by remember { mutableStateOf(0) }
        var questionCount by remember { mutableStateOf(0) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var gameState by remember { mutableStateOf(GameState.PLAYING) }
        var playerName by remember { mutableStateOf("") }
        var leaderboardEntries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
        val scope = rememberCoroutineScope()
        
        val greeting = remember { Greeting() }
        val storage = remember { LeaderboardStorage() }
        
        LaunchedEffect(Unit) {
            leaderboardEntries = storage.getEntries()
        }
        
        fun loadPokemon(clearMessage: Boolean = false) {
            scope.launch {
                try {
                    isLoading = true
                    errorMessage = null
                    pokemon = greeting.fetchPokemonData()
                    isLoading = false
                    if (clearMessage) {
                        message = null
                    }
                } catch (e: Exception) {
                    isLoading = false
                    errorMessage = "Erreur de connexion: ${e.message}. Vérifiez que le serveur est démarré."
                }
            }
        }
        
        fun validateAnswer() {
            scope.launch {
                pokemon?.let { pkmn ->
                    val isCorrect = guessText.equals(pkmn.name?.fr, ignoreCase = true)
                    message = if (isCorrect) {
                        score++
                        "Bravo ! C'est bien ${pkmn.name?.fr} !"
                    } else {
                        "Incorrect. Le bon nom est ${pkmn.name?.fr}"
                    }
                    guessText = ""
                    questionCount++
                    
                    if (questionCount >= 10) {
                        delay(2000)
                        gameState = GameState.ENTER_NAME
                    } else {
                        delay(2000)
                        loadPokemon(clearMessage = true)
                    }
                }
            }
        }
        
        fun saveScoreAndShowLeaderboard() {
            if (playerName.isNotBlank()) {
                val entry = LeaderboardEntry(
                    name = playerName.trim(),
                    score = score
                )
                storage.saveEntry(entry)
                leaderboardEntries = storage.getEntries()
            }
            gameState = GameState.LEADERBOARD
        }
        
        fun restartGame() {
            score = 0
            questionCount = 0
            message = null
            guessText = ""
            playerName = ""
            gameState = GameState.PLAYING
            loadPokemon()
        }
        
        LaunchedEffect(gameState) {
            if (gameState == GameState.PLAYING && pokemon == null && !isLoading) {
                loadPokemon()
            }
        }
        
        when (gameState) {
            GameState.PLAYING -> {
                GameScreen(
                    pokemon = pokemon,
                    guessText = guessText,
                    onGuessTextChange = { guessText = it },
                    message = message,
                    score = score,
                    questionCount = questionCount,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onValidate = { validateAnswer() },
                    onRetry = { loadPokemon() }
                )
            }
            GameState.ENTER_NAME -> {
                EnterNameScreen(
                    playerName = playerName,
                    onPlayerNameChange = { playerName = it },
                    score = score,
                    onSave = { saveScoreAndShowLeaderboard() }
                )
            }
            GameState.LEADERBOARD -> {
                LeaderboardScreen(
                    entries = leaderboardEntries,
                    playerScore = score,
                    onRestart = { restartGame() }
                )
            }
        }
    }
}

@Composable
fun GameScreen(
    pokemon: Pokemon?,
    guessText: String,
    onGuessTextChange: (String) -> Unit,
    message: String?,
    score: Int,
    questionCount: Int,
    isLoading: Boolean,
    errorMessage: String?,
    onValidate: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Devinez le Pokémon !",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("title")
        )
        
        Text(
            text = "Question $questionCount/10",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("question_counter")
        )
        
        message?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyLarge,
                color = if (msg.startsWith("Bravo")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.testTag("result_message")
            )
        }
        
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("score_counter")
        )
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loading"))
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Erreur inconnue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("error_message")
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.testTag("retry_button")
            ) {
                Text("Réessayer")
            }
        } else {
            pokemon?.let { pkmn ->
                pkmn.sprites?.regular?.let { imageUrl ->
                    Image(
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "Pokemon sprite",
                        modifier = Modifier
                            .size(200.dp)
                            .testTag("pokemon_image")
                    )
                }
                
                OutlinedTextField(
                    value = guessText,
                    onValueChange = onGuessTextChange,
                    label = { Text("Nom du Pokémon") },
                    singleLine = true,
                    enabled = questionCount < 10,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (questionCount < 10) onValidate() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guess_input")
                )
                
                Button(
                    onClick = onValidate,
                    enabled = questionCount < 10,
                    modifier = Modifier.testTag("validate_button")
                ) {
                    Text("Valider")
                }
            }
        }
    }
}

@Composable
fun EnterNameScreen(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    score: Int,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Partie terminée !",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.testTag("game_over_title")
        )
        
        Text(
            text = "Votre score: $score/10",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("final_score")
        )
        
        Text(
            text = "Entrez votre nom pour le leaderboard",
            style = MaterialTheme.typography.bodyLarge
        )
        
        OutlinedTextField(
            value = playerName,
            onValueChange = onPlayerNameChange,
            label = { Text("Votre nom") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("name_input")
        )
        
        Button(
            onClick = onSave,
            enabled = playerName.isNotBlank(),
            modifier = Modifier.testTag("save_score_button")
        ) {
            Text("Enregistrer")
        }
    }
}

@Composable
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    playerScore: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.testTag("leaderboard_title")
        )
        
        if (entries.isEmpty()) {
            Text(
                text = "Aucun score enregistré",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("leaderboard_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries.sortedByDescending { it.score }) { entry ->
                    LeaderboardItem(
                        entry = entry,
                        isPlayerScore = entry.score == playerScore
                    )
                }
            }
        }
        
        Button(
            onClick = onRestart,
            modifier = Modifier.testTag("restart_button")
        ) {
            Text("Nouvelle partie")
        }
    }
}

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    isPlayerScore: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_item_${entry.name}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayerScore) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isPlayerScore) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "${entry.score}/10",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
