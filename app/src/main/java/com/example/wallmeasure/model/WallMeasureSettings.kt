package com.example.wallmeasure.model

enum class AppThemeSetting(val title: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class WallMeasureSettings(
    val theme: AppThemeSetting = AppThemeSetting.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUnit: UnitType = UnitType.CM,
    val defaultOutputUnit: AreaUnitType = AreaUnitType.M2,
    val decimalPlaces: Int = 2,
    val defaultAllowancePercent: Double = 0.0,
    val showDimensionDiagram: Boolean = true,
    val showGrossArea: Boolean = true,
    val showOpenings: Boolean = true,
    val showNetArea: Boolean = true,
    val showPlanningArea: Boolean = true,
    val liveCalculation: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastCalculation: Boolean = true
)
