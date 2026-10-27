package com.example.wallmeasure.calculator

import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallResult

object WallMeasureCalculator {

    /**
     * Calculates gross wall area: Width (mm) × Height (mm)
     * Returns area in mm²
     */
    fun calculateGrossArea(
        widthMm: Double,
        heightMm: Double
    ): Double {
        if (widthMm <= 0.0 || heightMm <= 0.0) return 0.0
        return widthMm * heightMm
    }

    /**
     * Calculates opening area: Width (mm) × Height (mm) × Quantity
     * Returns area in mm²
     */
    fun calculateOpeningArea(
        opening: Opening
    ): Double {
        if (opening.widthMm <= 0.0 || opening.heightMm <= 0.0) return 0.0
        val safeQuantity = opening.quantity.coerceAtLeast(1)
        return opening.widthMm * opening.heightMm * safeQuantity
    }

    /**
     * Calculates total opening area for a list of openings
     */
    fun calculateTotalOpeningArea(openings: List<Opening>): Double {
        return openings.sumOf { calculateOpeningArea(it) }
    }

    /**
     * Calculates net usable wall area: Gross Wall Area - Total Openings Area
     * Never returns negative area (returns 0.0 if openings exceed gross area)
     */
    fun calculateNetArea(
        grossAreaMm2: Double,
        openings: List<Opening>
    ): Double {
        val totalOpeningArea = calculateTotalOpeningArea(openings)
        val net = grossAreaMm2 - totalOpeningArea
        return if (net < 0.0) 0.0 else net
    }

    /**
     * Calculates planning area including optional waste/allowance percentage:
     * Planning Area = Net Area + (Net Area * allowancePercent / 100.0)
     */
    fun calculatePlanningArea(
        netAreaMm2: Double,
        allowancePercent: Double
    ): Double {
        if (netAreaMm2 <= 0.0) return 0.0
        val safeAllowance = allowancePercent.coerceAtLeast(0.0)
        return netAreaMm2 * (1.0 + safeAllowance / 100.0)
    }

    /**
     * Calculates gross wall area for a 4-wall rectangular room:
     * Formula: 2 × (Room Length + Room Width) × Wall Height
     */
    fun calculateRoomWallArea(
        roomLengthMm: Double,
        roomWidthMm: Double,
        wallHeightMm: Double
    ): Double {
        if (roomLengthMm <= 0.0 || roomWidthMm <= 0.0 || wallHeightMm <= 0.0) return 0.0
        return 2.0 * (roomLengthMm + roomWidthMm) * wallHeightMm
    }

    /**
     * Calculates ceiling area: Room Length × Room Width
     * Kept completely separate from wall area
     */
    fun calculateCeilingArea(
        roomLengthMm: Double,
        roomWidthMm: Double
    ): Double {
        if (roomLengthMm <= 0.0 || roomWidthMm <= 0.0) return 0.0
        return roomLengthMm * roomWidthMm
    }

    /**
     * Calculates WallResult for a single wall
     */
    fun calculateWallResult(
        wall: Wall,
        allowancePercent: Double
    ): WallResult {
        val gross = calculateGrossArea(wall.widthMm, wall.heightMm)
        val openingsArea = calculateTotalOpeningArea(wall.openings)
        val net = calculateNetArea(gross, wall.openings)
        val planning = calculatePlanningArea(net, allowancePercent)
        val allowanceArea = planning - net
        return WallResult(
            grossAreaMm2 = gross,
            openingAreaMm2 = openingsArea,
            netAreaMm2 = net,
            allowanceAreaMm2 = allowanceArea,
            planningAreaMm2 = planning
        )
    }

    /**
     * Calculates total result for multiple walls
     */
    fun calculateTotalWallResult(
        walls: List<Wall>,
        allowancePercent: Double
    ): WallResult {
        var totalGross = 0.0
        var totalOpenings = 0.0
        for (wall in walls) {
            totalGross += calculateGrossArea(wall.widthMm, wall.heightMm)
            totalOpenings += calculateTotalOpeningArea(wall.openings)
        }
        val net = if (totalGross - totalOpenings < 0.0) 0.0 else totalGross - totalOpenings
        val planning = calculatePlanningArea(net, allowancePercent)
        val allowanceArea = planning - net
        return WallResult(
            grossAreaMm2 = totalGross,
            openingAreaMm2 = totalOpenings,
            netAreaMm2 = net,
            allowanceAreaMm2 = allowanceArea,
            planningAreaMm2 = planning
        )
    }

    /**
     * Validates wall dimensions (Width and Height in mm)
     * Returns null if valid, or an error message if invalid
     */
    fun validateWallDimensions(widthMm: Double?, heightMm: Double?): String? {
        if (widthMm == null || heightMm == null) {
            return "Please enter wall width and height."
        }
        if (widthMm <= 0.0) {
            return "Wall width must be greater than zero."
        }
        if (heightMm <= 0.0) {
            return "Wall height must be greater than zero."
        }
        return null
    }

    /**
     * Validates an opening against wall dimensions
     * Returns null if valid, or an error message if invalid
     */
    fun validateOpening(opening: Opening, wallWidthMm: Double, wallHeightMm: Double): String? {
        val typeName = opening.type.label
        if (opening.widthMm <= 0.0) {
            return "$typeName width must be greater than zero."
        }
        if (opening.heightMm <= 0.0) {
            return "$typeName height must be greater than zero."
        }
        if (opening.quantity < 1) {
            return "$typeName quantity must be at least 1."
        }
        if (opening.widthMm > wallWidthMm) {
            return "$typeName width cannot exceed wall width."
        }
        if (opening.heightMm > wallHeightMm) {
            return "$typeName height cannot exceed wall height."
        }
        // Validate visual position if provided
        if (opening.positionMm != null) {
            if (opening.positionMm < 0.0 || (opening.positionMm + opening.widthMm) > wallWidthMm) {
                return "$typeName horizontal position exceeds wall width."
            }
        }
        if (opening.verticalPositionMm != null) {
            if (opening.verticalPositionMm < 0.0 || (opening.verticalPositionMm + opening.heightMm) > wallHeightMm) {
                return "$typeName vertical position exceeds wall height."
            }
        }
        return null
    }

    /**
     * Checks if total opening area exceeds gross wall area
     */
    fun isOpeningAreaExceedingGross(grossAreaMm2: Double, openings: List<Opening>): Boolean {
        val totalOpeningArea = calculateTotalOpeningArea(openings)
        return totalOpeningArea > grossAreaMm2
    }
}
