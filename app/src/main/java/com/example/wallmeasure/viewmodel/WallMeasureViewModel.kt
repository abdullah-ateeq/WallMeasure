package com.example.wallmeasure.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.data.WallMeasureRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class WallMeasureViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallMeasureRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(WallMeasureUiState())
    val uiState: StateFlow<WallMeasureUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val settings = repository.getSettings()
        val history = repository.getHistory()

        if (settings.restoreLastCalculation) {
            val mode = repository.getLastMode()
            val unit = repository.getLastUnit()
            val wallName = repository.getLastWallName()
            val widthMm = repository.getLastWidthMm()
            val heightMm = repository.getLastHeightMm()
            val openings = repository.getLastOpenings()
            val allowance = repository.getLastAllowance()
            val orientation = repository.getLastOrientation()
            val showOpeningsOnDiagram = repository.getLastShowOpeningsOnDiagram()

            val roomLengthMm = repository.getLastRoomLengthMm()
            val roomWidthMm = repository.getLastRoomWidthMm()
            val roomHeightMm = repository.getLastRoomHeightMm()
            val roomCeiling = repository.getLastRoomIncludeCeiling()

            val multiWalls = repository.getLastMultiWalls().ifEmpty {
                listOf(
                    Wall(id = UUID.randomUUID().toString(), name = "Living Room Wall A", widthMm = 5000.0, heightMm = 3000.0),
                    Wall(id = UUID.randomUUID().toString(), name = "Living Room Wall B", widthMm = 4000.0, heightMm = 3000.0)
                )
            }
            val heightMode = repository.getLastMultiHeightMode()
            val sharedHeightMm = repository.getLastMultiSharedHeightMm()

            _uiState.update {
                it.copy(
                    settings = settings,
                    history = history,
                    calculationMode = mode,
                    unit = unit,
                    outputUnit = settings.defaultOutputUnit,
                    wallName = wallName,
                    widthMm = widthMm,
                    heightMm = heightMm,
                    openings = openings,
                    allowancePercent = allowance,
                    orientation = orientation,
                    showOpeningsOnDiagram = showOpeningsOnDiagram,
                    roomLengthMm = roomLengthMm,
                    roomWidthMm = roomWidthMm,
                    roomHeightMm = roomHeightMm,
                    roomIncludeCeiling = roomCeiling,
                    multipleWalls = multiWalls,
                    heightMode = heightMode,
                    sharedHeightMm = sharedHeightMm
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    settings = settings,
                    history = history,
                    unit = settings.defaultUnit,
                    outputUnit = settings.defaultOutputUnit,
                    allowancePercent = settings.defaultAllowancePercent,
                    multipleWalls = listOf(
                        Wall(id = UUID.randomUUID().toString(), name = "Living Room Wall A", widthMm = 5000.0, heightMm = 3000.0),
                        Wall(id = UUID.randomUUID().toString(), name = "Living Room Wall B", widthMm = 4000.0, heightMm = 3000.0)
                    )
                )
            }
        }

        recalculateAll()
    }

    private fun persistCurrentState() {
        val s = _uiState.value
        if (s.settings.restoreLastCalculation) {
            repository.saveLastState(
                mode = s.calculationMode,
                unit = s.unit,
                wallName = s.wallName,
                widthMm = s.widthMm,
                heightMm = s.heightMm,
                openings = s.openings,
                allowancePercent = s.allowancePercent,
                orientation = s.orientation,
                showOpeningsOnDiagram = s.showOpeningsOnDiagram,
                roomLengthMm = s.roomLengthMm,
                roomWidthMm = s.roomWidthMm,
                roomHeightMm = s.roomHeightMm,
                roomIncludeCeiling = s.roomIncludeCeiling,
                multiWalls = s.multipleWalls,
                multiHeightMode = s.heightMode,
                multiSharedHeightMm = s.sharedHeightMm
            )
        }
    }

    // =========================================================================
    // CALCULATION LOGIC
    // =========================================================================

    private fun recalculateAll() {
        _uiState.update { current ->
            // 1. Single Wall Result
            val wallError = WallMeasureCalculator.validateWallDimensions(current.widthMm, current.heightMm)
            val currentWall = Wall(
                id = "current_wall",
                name = current.wallName,
                widthMm = current.widthMm,
                heightMm = current.heightMm,
                openings = current.openings
            )
            val singleResult = if (wallError == null) {
                WallMeasureCalculator.calculateWallResult(currentWall, current.allowancePercent)
            } else null

            // 2. Room Mode Result
            val roomCeilingArea = if (current.roomIncludeCeiling) {
                WallMeasureCalculator.calculateCeilingArea(current.roomLengthMm, current.roomWidthMm)
            } else 0.0

            val roomGrossArea = WallMeasureCalculator.calculateRoomWallArea(
                current.roomLengthMm,
                current.roomWidthMm,
                current.roomHeightMm
            )
            val roomOpeningsArea = WallMeasureCalculator.calculateTotalOpeningArea(current.roomOpenings)
            val roomNet = if (roomGrossArea - roomOpeningsArea < 0.0) 0.0 else roomGrossArea - roomOpeningsArea
            val roomPlanning = WallMeasureCalculator.calculatePlanningArea(roomNet, current.allowancePercent)
            val roomResult = WallResult(
                grossAreaMm2 = roomGrossArea,
                openingAreaMm2 = roomOpeningsArea,
                netAreaMm2 = roomNet,
                allowanceAreaMm2 = roomPlanning - roomNet,
                planningAreaMm2 = roomPlanning
            )

            // 3. Multiple Walls Result
            val multiWallsResult = WallMeasureCalculator.calculateTotalWallResult(
                current.multipleWalls,
                current.allowancePercent
            )

            current.copy(
                result = singleResult,
                errorMessage = wallError,
                roomCeilingAreaMm2 = roomCeilingArea,
                roomResult = roomResult,
                multipleWallsResult = multiWallsResult
            )
        }
        persistCurrentState()
    }

    // =========================================================================
    // UI ACTIONS - GENERAL
    // =========================================================================

    fun setCalculationMode(mode: CalculationMode) {
        _uiState.update { it.copy(calculationMode = mode) }
        persistCurrentState()
    }

    fun setUnit(unit: UnitType) {
        _uiState.update { it.copy(unit = unit) }
        persistCurrentState()
    }

    fun setOutputUnit(unit: AreaUnitType) {
        _uiState.update { it.copy(outputUnit = unit) }
    }

    fun setWallName(name: String) {
        _uiState.update { it.copy(wallName = name) }
        persistCurrentState()
    }

    fun setWallWidthMm(widthMm: Double) {
        _uiState.update { it.copy(widthMm = widthMm) }
        recalculateAll()
    }

    fun setWallHeightMm(heightMm: Double) {
        _uiState.update { it.copy(heightMm = heightMm) }
        recalculateAll()
    }

    fun setWallDimensions(widthMm: Double, heightMm: Double) {
        _uiState.update { it.copy(widthMm = widthMm, heightMm = heightMm) }
        recalculateAll()
    }

    fun setOrientation(orientation: WallOrientation) {
        _uiState.update { it.copy(orientation = orientation) }
        persistCurrentState()
    }

    fun setShowOpeningsOnDiagram(show: Boolean) {
        _uiState.update { it.copy(showOpeningsOnDiagram = show) }
        persistCurrentState()
    }

    fun setAllowancePercent(percent: Double) {
        _uiState.update { it.copy(allowancePercent = percent.coerceAtLeast(0.0)) }
        recalculateAll()
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // =========================================================================
    // OPENINGS - SINGLE WALL
    // =========================================================================

    fun addOpening(opening: Opening) {
        _uiState.update { current ->
            current.copy(openings = current.openings + opening)
        }
        recalculateAll()
    }

    fun updateOpening(opening: Opening) {
        _uiState.update { current ->
            current.copy(openings = current.openings.map { if (it.id == opening.id) opening else it })
        }
        recalculateAll()
    }

    fun removeOpening(id: String) {
        _uiState.update { current ->
            current.copy(openings = current.openings.filterNot { it.id == id })
        }
        recalculateAll()
    }

    fun duplicateOpening(id: String) {
        _uiState.update { current ->
            val target = current.openings.find { it.id == id }
            if (target != null) {
                val copy = target.copy(
                    id = UUID.randomUUID().toString(),
                    name = "${target.name} (Copy)"
                )
                current.copy(openings = current.openings + copy)
            } else current
        }
        recalculateAll()
    }

    // =========================================================================
    // FOUR-WALL ROOM
    // =========================================================================

    fun setRoomLengthMm(lengthMm: Double) {
        _uiState.update { it.copy(roomLengthMm = lengthMm) }
        recalculateAll()
    }

    fun setRoomWidthMm(widthMm: Double) {
        _uiState.update { it.copy(roomWidthMm = widthMm) }
        recalculateAll()
    }

    fun setRoomHeightMm(heightMm: Double) {
        _uiState.update { it.copy(roomHeightMm = heightMm) }
        recalculateAll()
    }

    fun setRoomIncludeCeiling(include: Boolean) {
        _uiState.update { it.copy(roomIncludeCeiling = include) }
        recalculateAll()
    }

    fun addRoomOpening(opening: Opening) {
        _uiState.update { current ->
            current.copy(roomOpenings = current.roomOpenings + opening)
        }
        recalculateAll()
    }

    fun updateRoomOpening(opening: Opening) {
        _uiState.update { current ->
            current.copy(roomOpenings = current.roomOpenings.map { if (it.id == opening.id) opening else it })
        }
        recalculateAll()
    }

    fun removeRoomOpening(id: String) {
        _uiState.update { current ->
            current.copy(roomOpenings = current.roomOpenings.filterNot { it.id == id })
        }
        recalculateAll()
    }

    // =========================================================================
    // MULTIPLE WALLS
    // =========================================================================

    fun addWall(wall: Wall) {
        _uiState.update { current ->
            current.copy(multipleWalls = current.multipleWalls + wall)
        }
        recalculateAll()
    }

    fun updateWall(wall: Wall) {
        _uiState.update { current ->
            current.copy(multipleWalls = current.multipleWalls.map { if (it.id == wall.id) wall else it })
        }
        recalculateAll()
    }

    fun removeWall(id: String) {
        _uiState.update { current ->
            current.copy(multipleWalls = current.multipleWalls.filterNot { it.id == id })
        }
        recalculateAll()
    }

    fun duplicateWall(id: String) {
        _uiState.update { current ->
            val target = current.multipleWalls.find { it.id == id }
            if (target != null) {
                val copy = target.copy(
                    id = UUID.randomUUID().toString(),
                    name = "${target.name} (Copy)",
                    openings = target.openings.map { it.copy(id = UUID.randomUUID().toString()) }
                )
                current.copy(multipleWalls = current.multipleWalls + copy)
            } else current
        }
        recalculateAll()
    }

    fun setHeightMode(mode: WallHeightMode) {
        _uiState.update { current ->
            if (mode == WallHeightMode.SAME_HEIGHT) {
                val updatedWalls = current.multipleWalls.map { it.copy(heightMm = current.sharedHeightMm) }
                current.copy(heightMode = mode, multipleWalls = updatedWalls)
            } else {
                current.copy(heightMode = mode)
            }
        }
        recalculateAll()
    }

    fun setSharedHeight(heightMm: Double) {
        _uiState.update { current ->
            val updatedWalls = if (current.heightMode == WallHeightMode.SAME_HEIGHT) {
                current.multipleWalls.map { it.copy(heightMm = heightMm) }
            } else current.multipleWalls
            current.copy(sharedHeightMm = heightMm, multipleWalls = updatedWalls)
        }
        recalculateAll()
    }

    fun generateFourWallsFromRoom(lengthMm: Double, widthMm: Double, heightMm: Double) {
        val walls = listOf(
            Wall(id = UUID.randomUUID().toString(), name = "Wall 1", widthMm = lengthMm, heightMm = heightMm),
            Wall(id = UUID.randomUUID().toString(), name = "Wall 2", widthMm = widthMm, heightMm = heightMm),
            Wall(id = UUID.randomUUID().toString(), name = "Wall 3", widthMm = lengthMm, heightMm = heightMm),
            Wall(id = UUID.randomUUID().toString(), name = "Wall 4", widthMm = widthMm, heightMm = heightMm)
        )
        _uiState.update { it.copy(multipleWalls = walls, calculationMode = CalculationMode.MULTIPLE_WALLS) }
        recalculateAll()
    }

    // =========================================================================
    // HISTORY
    // =========================================================================

    fun saveCurrentCalculation(): Boolean {
        val current = _uiState.value
        val wallsToSave: List<Wall> = when (current.calculationMode) {
            CalculationMode.SINGLE_WALL -> {
                listOf(
                    Wall(
                        id = UUID.randomUUID().toString(),
                        name = current.wallName,
                        widthMm = current.widthMm,
                        heightMm = current.heightMm,
                        openings = current.openings
                    )
                )
            }
            CalculationMode.MULTIPLE_WALLS -> current.multipleWalls
            CalculationMode.FOUR_WALL_ROOM -> {
                listOf(
                    Wall(id = UUID.randomUUID().toString(), name = "Wall 1 (Length)", widthMm = current.roomLengthMm, heightMm = current.roomHeightMm),
                    Wall(id = UUID.randomUUID().toString(), name = "Wall 2 (Width)", widthMm = current.roomWidthMm, heightMm = current.roomHeightMm),
                    Wall(id = UUID.randomUUID().toString(), name = "Wall 3 (Length)", widthMm = current.roomLengthMm, heightMm = current.roomHeightMm),
                    Wall(id = UUID.randomUUID().toString(), name = "Wall 4 (Width)", widthMm = current.roomWidthMm, heightMm = current.roomHeightMm)
                )
            }
        }

        val calculationTitle = when (current.calculationMode) {
            CalculationMode.SINGLE_WALL -> current.wallName
            CalculationMode.MULTIPLE_WALLS -> "${current.multipleWalls.size} Walls Project"
            CalculationMode.FOUR_WALL_ROOM -> "Four-Wall Room"
        }

        val item = WallMeasureHistory(
            id = UUID.randomUUID().toString(),
            name = calculationTitle,
            walls = wallsToSave,
            createdAt = System.currentTimeMillis()
        )

        val saved = repository.saveHistoryItem(item)
        if (saved) {
            _uiState.update {
                it.copy(
                    history = repository.getHistory(),
                    userMessage = "Calculation saved to History"
                )
            }
        } else {
            _uiState.update {
                it.copy(userMessage = "Identical calculation already exists in History")
            }
        }
        return saved
    }

    fun loadHistoryItem(item: WallMeasureHistory) {
        if (item.walls.size == 1) {
            val single = item.walls.first()
            _uiState.update {
                it.copy(
                    calculationMode = CalculationMode.SINGLE_WALL,
                    wallName = single.name,
                    widthMm = single.widthMm,
                    heightMm = single.heightMm,
                    openings = single.openings,
                    userMessage = "Restored '${item.name}'"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    calculationMode = CalculationMode.MULTIPLE_WALLS,
                    multipleWalls = item.walls,
                    userMessage = "Restored '${item.name}' (${item.walls.size} walls)"
                )
            }
        }
        recalculateAll()
    }

    fun duplicateHistoryItem(item: WallMeasureHistory) {
        val copy = item.copy(
            id = UUID.randomUUID().toString(),
            name = "${item.name} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        repository.saveHistoryItem(copy)
        _uiState.update {
            it.copy(
                history = repository.getHistory(),
                userMessage = "Duplicated in History"
            )
        }
    }

    fun deleteHistoryItem(id: String) {
        repository.deleteHistoryItem(id)
        _uiState.update {
            it.copy(
                history = repository.getHistory(),
                userMessage = "History item deleted"
            )
        }
    }

    fun clearAllHistory() {
        repository.clearHistory()
        _uiState.update {
            it.copy(
                history = emptyList(),
                userMessage = "All history cleared"
            )
        }
    }

    // =========================================================================
    // SETTINGS
    // =========================================================================

    fun updateSettings(settings: WallMeasureSettings) {
        repository.saveSettings(settings)
        _uiState.update {
            it.copy(
                settings = settings,
                outputUnit = settings.defaultOutputUnit
            )
        }
    }

    fun resetSettings() {
        repository.resetSettings()
        val defaultSettings = WallMeasureSettings()
        _uiState.update {
            it.copy(
                settings = defaultSettings,
                outputUnit = defaultSettings.defaultOutputUnit,
                unit = defaultSettings.defaultUnit,
                userMessage = "Settings reset to default"
            )
        }
    }
}
