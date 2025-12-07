package org.example.project

import platform.Foundation.NSUserDefaults

actual class LeaderboardStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun saveEntry(entry: LeaderboardEntry) {
        val entries = getEntries().toMutableList()
        entries.add(entry)
        val sortedEntries = entries.sortedByDescending { it.score }.take(10)
    }
    
    actual fun getEntries(): List<LeaderboardEntry> {
        return emptyList()
    }
    
    actual fun clearEntries() {
    }
}
