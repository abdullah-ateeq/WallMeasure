package com.example.wallmeasure.calculator

import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.UnitType
import java.util.Locale

object UnitConverter {

    const val MM_PER_CM = 10.0
    const val MM_PER_M = 1000.0
    const val MM_PER_INCH = 25.4
    const val MM_PER_FOOT = 304.8

    // Area conversion constants (in mm²)
    const val MM2_PER_M2 = 1_000_000.0
    const val MM2_PER_FT2 = MM_PER_FOOT * MM_PER_FOOT // 92903.04
    const val MM2_PER_CM2 = 100.0
    const val MM2_PER_IN2 = MM_PER_INCH * MM_PER_INCH // 645.16

    fun toMillimeters(value: Double, unit: UnitType): Double {
        return when (unit) {
            UnitType.MM -> value
            UnitType.CM -> value * MM_PER_CM
            UnitType.M -> value * MM_PER_M
            UnitType.INCH -> value * MM_PER_INCH
            UnitType.FEET -> value * MM_PER_FOOT
        }
    }

    fun fromMillimeters(valueMm: Double, unit: UnitType): Double {
        return when (unit) {
            UnitType.MM -> valueMm
            UnitType.CM -> valueMm / MM_PER_CM
            UnitType.M -> valueMm / MM_PER_M
            UnitType.INCH -> valueMm / MM_PER_INCH
            UnitType.FEET -> valueMm / MM_PER_FOOT
        }
    }

    fun fromMm2ToAreaUnit(areaMm2: Double, unit: AreaUnitType): Double {
        return when (unit) {
            AreaUnitType.M2 -> areaMm2 / MM2_PER_M2
            AreaUnitType.FT2 -> areaMm2 / MM2_PER_FT2
            AreaUnitType.CM2 -> areaMm2 / MM2_PER_CM2
            AreaUnitType.IN2 -> areaMm2 / MM2_PER_IN2
        }
    }

    fun toMm2FromAreaUnit(value: Double, unit: AreaUnitType): Double {
        return when (unit) {
            AreaUnitType.M2 -> value * MM2_PER_M2
            AreaUnitType.FT2 -> value * MM2_PER_FT2
            AreaUnitType.CM2 -> value * MM2_PER_CM2
            AreaUnitType.IN2 -> value * MM2_PER_IN2
        }
    }

    fun formatArea(
        areaMm2: Double,
        unit: AreaUnitType = AreaUnitType.M2,
        decimalPlaces: Int = 2,
        includeUnit: Boolean = true
    ): String {
        val converted = fromMm2ToAreaUnit(areaMm2, unit)
        val formatted = formatNumber(converted, decimalPlaces)
        return if (includeUnit) "$formatted ${unit.symbol}" else formatted
    }

    fun formatDimension(
        dimensionMm: Double,
        unit: UnitType,
        decimalPlaces: Int = 2,
        includeUnit: Boolean = true
    ): String {
        val converted = fromMillimeters(dimensionMm, unit)
        val formatted = formatNumber(converted, decimalPlaces)
        return if (includeUnit) "$formatted ${unit.symbol}" else formatted
    }

    fun formatNumber(value: Double, decimalPlaces: Int): String {
        val safeDecimals = decimalPlaces.coerceIn(0, 4)
        return String.format(Locale.US, "%.${safeDecimals}f", value)
    }

    fun getAllDimensionConversions(dimensionMm: Double, decimalPlaces: Int = 2): List<Pair<UnitType, String>> {
        return UnitType.entries.map { unit ->
            val value = fromMillimeters(dimensionMm, unit)
            unit to "${formatNumber(value, decimalPlaces)} ${unit.symbol}"
        }
    }
}
