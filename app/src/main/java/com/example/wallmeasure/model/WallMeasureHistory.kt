package com.example.wallmeasure.model

data class WallMeasureHistory(
    val id: String,
    val name: String,
    val walls: List<Wall>,
    val createdAt: Long
)
