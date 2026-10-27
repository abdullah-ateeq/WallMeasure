package com.example.wallmeasure.model

enum class AreaUnitType(
    val displayName: String,
    val symbol: String
) {
    M2("Square Meters", "m²"),
    FT2("Square Feet", "ft²"),
    CM2("Square Centimeters", "cm²"),
    IN2("Square Inches", "in²")
}
