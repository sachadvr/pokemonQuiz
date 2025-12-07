package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val name: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)
