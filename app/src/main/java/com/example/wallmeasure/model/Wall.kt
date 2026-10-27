package com.example.wallmeasure.model

data class Wall(
    val id: String,
    val name: String,
    val widthMm: Double,
    val heightMm: Double,
    val openings: List<Opening> = emptyList()
)
