package com.example.wallmeasure

import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.Wall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WallMeasureCalculatorTest {

    // 1. SINGLE WALL: 500 × 300 cm -> 15.00 m²
    @Test
    fun testSingleWallArea() {
        val widthMm = UnitConverter.toMillimeters(500.0, UnitType.CM)
        val heightMm = UnitConverter.toMillimeters(300.0, UnitType.CM)
        val grossAreaMm2 = WallMeasureCalculator.calculateGrossArea(widthMm, heightMm)
        val areaM2 = UnitConverter.fromMm2ToAreaUnit(grossAreaMm2, AreaUnitType.M2)
        assertEquals(15.00, areaM2, 0.001)
    }

    // 2. SQUARE WALL: 400 × 400 cm -> 16.00 m²
    @Test
    fun testSquareWallArea() {
        val widthMm = UnitConverter.toMillimeters(400.0, UnitType.CM)
        val heightMm = UnitConverter.toMillimeters(400.0, UnitType.CM)
        val grossAreaMm2 = WallMeasureCalculator.calculateGrossArea(widthMm, heightMm)
        val areaM2 = UnitConverter.fromMm2ToAreaUnit(grossAreaMm2, AreaUnitType.M2)
        assertEquals(16.00, areaM2, 0.001)
    }

    // 3. METER INPUT: 5 m × 3 m -> 15.00 m²
    @Test
    fun testMeterInput() {
        val widthMm = UnitConverter.toMillimeters(5.0, UnitType.M)
        val heightMm = UnitConverter.toMillimeters(3.0, UnitType.M)
        val grossAreaMm2 = WallMeasureCalculator.calculateGrossArea(widthMm, heightMm)
        val areaM2 = UnitConverter.fromMm2ToAreaUnit(grossAreaMm2, AreaUnitType.M2)
        assertEquals(15.00, areaM2, 0.001)
    }

    // 4. FEET INPUT: 10 ft × 10 ft -> ~9.2903 m²
    @Test
    fun testFeetInput() {
        val widthMm = UnitConverter.toMillimeters(10.0, UnitType.FEET)
        val heightMm = UnitConverter.toMillimeters(10.0, UnitType.FEET)
        val grossAreaMm2 = WallMeasureCalculator.calculateGrossArea(widthMm, heightMm)
        val areaM2 = UnitConverter.fromMm2ToAreaUnit(grossAreaMm2, AreaUnitType.M2)
        assertEquals(9.2903, areaM2, 0.001)
    }

    // 5. DOOR SUBTRACTION: Wall 500x300 cm, Door 90x210 cm -> Gross 15.00 m², Door 1.89 m², Net 13.11 m²
    @Test
    fun testDoorSubtraction() {
        val wallWidthMm = UnitConverter.toMillimeters(500.0, UnitType.CM)
        val wallHeightMm = UnitConverter.toMillimeters(300.0, UnitType.CM)
        val doorWidthMm = UnitConverter.toMillimeters(90.0, UnitType.CM)
        val doorHeightMm = UnitConverter.toMillimeters(210.0, UnitType.CM)

        val door = Opening(
            id = "door1",
            name = "Main Door",
            type = OpeningType.DOOR,
            widthMm = doorWidthMm,
            heightMm = doorHeightMm,
            quantity = 1
        )

        val wall = Wall(
            id = "wall1",
            name = "Living Room Wall",
            widthMm = wallWidthMm,
            heightMm = wallHeightMm,
            openings = listOf(door)
        )

        val result = WallMeasureCalculator.calculateWallResult(wall, 0.0)
        val grossM2 = UnitConverter.fromMm2ToAreaUnit(result.grossAreaMm2, AreaUnitType.M2)
        val doorM2 = UnitConverter.fromMm2ToAreaUnit(result.openingAreaMm2, AreaUnitType.M2)
        val netM2 = UnitConverter.fromMm2ToAreaUnit(result.netAreaMm2, AreaUnitType.M2)

        assertEquals(15.00, grossM2, 0.001)
        assertEquals(1.89, doorM2, 0.001)
        assertEquals(13.11, netM2, 0.001)
    }

    // 6. MULTIPLE WINDOWS: Wall 500x300 cm, Windows 150x120 cm Qty 2 -> Opening 3.60 m², Net 11.40 m²
    @Test
    fun testMultipleWindowsSubtraction() {
        val wallWidthMm = UnitConverter.toMillimeters(500.0, UnitType.CM)
        val wallHeightMm = UnitConverter.toMillimeters(300.0, UnitType.CM)
        val winWidthMm = UnitConverter.toMillimeters(150.0, UnitType.CM)
        val winHeightMm = UnitConverter.toMillimeters(120.0, UnitType.CM)

        val window = Opening(
            id = "win1",
            name = "Window",
            type = OpeningType.WINDOW,
            widthMm = winWidthMm,
            heightMm = winHeightMm,
            quantity = 2
        )

        val wall = Wall(
            id = "wall1",
            name = "Living Room Wall",
            widthMm = wallWidthMm,
            heightMm = wallHeightMm,
            openings = listOf(window)
        )

        val result = WallMeasureCalculator.calculateWallResult(wall, 0.0)
        val openM2 = UnitConverter.fromMm2ToAreaUnit(result.openingAreaMm2, AreaUnitType.M2)
        val netM2 = UnitConverter.fromMm2ToAreaUnit(result.netAreaMm2, AreaUnitType.M2)

        assertEquals(3.60, openM2, 0.001)
        assertEquals(11.40, netM2, 0.001)
    }

    // 7. FOUR-WALL TEST: Room 500 × 400 cm, Height 300 cm -> 54.00 m²
    @Test
    fun testFourWallRoom() {
        val lengthMm = UnitConverter.toMillimeters(500.0, UnitType.CM)
        val widthMm = UnitConverter.toMillimeters(400.0, UnitType.CM)
        val heightMm = UnitConverter.toMillimeters(300.0, UnitType.CM)

        val grossMm2 = WallMeasureCalculator.calculateRoomWallArea(lengthMm, widthMm, heightMm)
        val grossM2 = UnitConverter.fromMm2ToAreaUnit(grossMm2, AreaUnitType.M2)

        assertEquals(54.00, grossM2, 0.001)
    }

    // 8. OPENING VALIDATION: Wall 500x300 cm, Window 600x100 cm -> INVALID
    @Test
    fun testOpeningValidation() {
        val wallWidthMm = UnitConverter.toMillimeters(500.0, UnitType.CM)
        val wallHeightMm = UnitConverter.toMillimeters(300.0, UnitType.CM)
        val invalidWinWidthMm = UnitConverter.toMillimeters(600.0, UnitType.CM)
        val winHeightMm = UnitConverter.toMillimeters(100.0, UnitType.CM)

        val invalidWindow = Opening(
            id = "invalid_win",
            name = "Oversized Window",
            type = OpeningType.WINDOW,
            widthMm = invalidWinWidthMm,
            heightMm = winHeightMm,
            quantity = 1
        )

        val error = WallMeasureCalculator.validateOpening(invalidWindow, wallWidthMm, wallHeightMm)
        assertNotNull("Expected validation error for width exceeding wall width", error)
        assertTrue(error!!.contains("cannot exceed wall width"))
    }

    // 9. NEGATIVE INPUT: Width = -500 -> Validation error
    @Test
    fun testNegativeInputValidation() {
        val error = WallMeasureCalculator.validateWallDimensions(-500.0, 300.0)
        assertNotNull("Expected validation error for negative width", error)
        assertTrue(error!!.contains("must be greater than zero"))
    }

    // 10. ZERO INPUT: Height = 0 -> Validation error
    @Test
    fun testZeroInputValidation() {
        val error = WallMeasureCalculator.validateWallDimensions(500.0, 0.0)
        assertNotNull("Expected validation error for zero height", error)
        assertTrue(error!!.contains("must be greater than zero"))
    }

    // 11. ALLOWANCE TEST: Net 30.00 m², Allowance 10% -> Allowance 3.00 m², Planning Area 33.00 m²
    @Test
    fun testAllowanceCalculation() {
        val netMm2 = UnitConverter.toMm2FromAreaUnit(30.00, AreaUnitType.M2)
        val planningMm2 = WallMeasureCalculator.calculatePlanningArea(netMm2, 10.0)
        val allowanceMm2 = planningMm2 - netMm2

        val allowanceM2 = UnitConverter.fromMm2ToAreaUnit(allowanceMm2, AreaUnitType.M2)
        val planningM2 = UnitConverter.fromMm2ToAreaUnit(planningMm2, AreaUnitType.M2)

        assertEquals(3.00, allowanceM2, 0.001)
        assertEquals(33.00, planningM2, 0.001)
    }

    // 12. UNIT CONVERSION TEST:
    // 1 m = 1000 mm, 1 cm = 10 mm, 1 inch = 25.4 mm, 1 ft = 304.8 mm
    @Test
    fun testUnitConversions() {
        assertEquals(1000.0, UnitConverter.toMillimeters(1.0, UnitType.M), 0.0001)
        assertEquals(10.0, UnitConverter.toMillimeters(1.0, UnitType.CM), 0.0001)
        assertEquals(25.4, UnitConverter.toMillimeters(1.0, UnitType.INCH), 0.0001)
        assertEquals(304.8, UnitConverter.toMillimeters(1.0, UnitType.FEET), 0.0001)
    }

    // Negative net area protection test
    @Test
    fun testNegativeNetAreaProtection() {
        val wallWidthMm = UnitConverter.toMillimeters(100.0, UnitType.CM)
        val wallHeightMm = UnitConverter.toMillimeters(100.0, UnitType.CM)
        val grossMm2 = WallMeasureCalculator.calculateGrossArea(wallWidthMm, wallHeightMm) // 1 m²

        val largeDoor = Opening(
            id = "large",
            name = "Massive Door",
            type = OpeningType.DOOR,
            widthMm = wallWidthMm,
            heightMm = wallHeightMm,
            quantity = 2 // 2 m² openings
        )

        val netMm2 = WallMeasureCalculator.calculateNetArea(grossMm2, listOf(largeDoor))
        assertEquals(0.0, netMm2, 0.0001) // Never negative!
        assertTrue(WallMeasureCalculator.isOpeningAreaExceedingGross(grossMm2, listOf(largeDoor)))
    }
}
