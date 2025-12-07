package org.example.project

expect class LeaderboardStorage() {
    fun saveEntry(entry: LeaderboardEntry)
    fun getEntries(): List<LeaderboardEntry>
    fun clearEntries()
}
