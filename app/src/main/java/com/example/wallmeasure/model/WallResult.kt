package com.example.wallmeasure.model

data class WallResult(
    val grossAreaMm2: Double,
    val openingAreaMm2: Double,
    val netAreaMm2: Double,
    val allowanceAreaMm2: Double,
    val planningAreaMm2: Double
)
