package com.example.wallmeasure.model

enum class UnitType(
    val displayName: String,
    val symbol: String
) {
    MM("Millimeters", "mm"),
    CM("Centimeters", "cm"),
    M("Meters", "m"),
    INCH("Inches", "in"),
    FEET("Feet", "ft")
}
