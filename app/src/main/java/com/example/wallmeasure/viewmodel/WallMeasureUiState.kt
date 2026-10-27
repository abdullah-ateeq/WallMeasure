package com.example.wallmeasure.viewmodel

import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.CalculationMode
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallHeightMode
import com.example.wallmeasure.model.WallMeasureHistory
import com.example.wallmeasure.model.WallMeasureSettings
import com.example.wallmeasure.model.WallOrientation
import com.example.wallmeasure.model.WallResult

data class WallMeasureUiState(
    val calculationMode: CalculationMode = CalculationMode.SINGLE_WALL,
    val unit: UnitType = UnitType.CM,
    val outputUnit: AreaUnitType = AreaUnitType.M2,
    val wallName: String = "Wall 1",
    val widthMm: Double = 5000.0,
    val heightMm: Double = 3000.0,
    val orientation: WallOrientation = WallOrientation.HORIZONTAL,
    val openings: List<Opening> = emptyList(),
    val allowancePercent: Double = 0.0,
    val showOpeningsOnDiagram: Boolean = true,
    val result: WallResult? = null,
    val errorMessage: String? = null,
    val userMessage: String? = null,

    // Four-Wall Room Mode
    val roomLengthMm: Double = 5000.0,
    val roomWidthMm: Double = 4000.0,
    val roomHeightMm: Double = 3000.0,
    val roomIncludeCeiling: Boolean = false,
    val roomCeilingAreaMm2: Double = 0.0,
    val roomOpenings: List<Opening> = emptyList(),
    val roomResult: WallResult? = null,

    // Multiple Walls Mode
    val multipleWalls: List<Wall> = emptyList(),
    val heightMode: WallHeightMode = WallHeightMode.INDIVIDUAL_HEIGHT,
    val sharedHeightMm: Double = 3000.0,
    val multipleWallsResult: WallResult? = null,

    // History and Settings
    val history: List<WallMeasureHistory> = emptyList(),
    val settings: WallMeasureSettings = WallMeasureSettings()
)
