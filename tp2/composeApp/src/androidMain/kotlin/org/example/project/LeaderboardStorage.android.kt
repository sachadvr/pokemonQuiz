package org.example.project

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

actual class LeaderboardStorage {
    private val prefs: SharedPreferences by lazy {
        androidContext?.getSharedPreferences("leaderboard", Context.MODE_PRIVATE)
            ?: throw IllegalStateException("Android context not initialized")
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    actual fun saveEntry(entry: LeaderboardEntry) {
        val entries = getEntries().toMutableList()
        entries.add(entry)
        val sortedEntries = entries.sortedByDescending { it.score }.take(10)
        val jsonString = json.encodeToString(sortedEntries)
        prefs.edit().putString("entries", jsonString).apply()
    }
    
    actual fun getEntries(): List<LeaderboardEntry> {
        val jsonString = prefs.getString("entries", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<LeaderboardEntry>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    actual fun clearEntries() {
        prefs.edit().remove("entries").apply()
    }
}
