package com.example.wallmeasure.model

data class Opening(
    val id: String,
    val name: String,
    val type: OpeningType,
    val widthMm: Double,
    val heightMm: Double,
    val quantity: Int = 1,
    val positionMm: Double? = null,
    val verticalPositionMm: Double? = null
)
