package com.example.wallmeasure.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.wallmeasure.calculator.UnitConverter
import com.example.wallmeasure.calculator.WallMeasureCalculator
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.Wall
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter

object ExportHelper {

    fun generateTxtExport(
        walls: List<Wall>,
        calculationTitle: String,
        unit: UnitType,
        outputUnit: AreaUnitType,
        allowancePercent: Double,
        decimalPlaces: Int = 2
    ): String {
        val totalResult = WallMeasureCalculator.calculateTotalWallResult(walls, allowancePercent)
        val sb = StringBuilder()
        sb.appendLine("WALLMEASURE")
        sb.appendLine("Calculate wall surface area quickly.")
        sb.appendLine("----------------------------------------")
        sb.appendLine("Calculation: $calculationTitle")
        sb.appendLine("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}")
        sb.appendLine("----------------------------------------")

        walls.forEachIndexed { index, wall ->
            val wallResult = WallMeasureCalculator.calculateWallResult(wall, allowancePercent)
            val widthFormatted = UnitConverter.formatDimension(wall.widthMm, unit, decimalPlaces)
            val heightFormatted = UnitConverter.formatDimension(wall.heightMm, unit, decimalPlaces)

            sb.appendLine("Wall ${index + 1}: ${wall.name}")
            sb.appendLine("Dimensions:\n$widthFormatted × $heightFormatted")
            sb.appendLine("Gross Wall Area:\n${UnitConverter.formatArea(wallResult.grossAreaMm2, outputUnit, decimalPlaces)}")

            if (wall.openings.isNotEmpty()) {
                sb.appendLine("Openings:")
                wall.openings.forEach { op ->
                    val opW = UnitConverter.formatDimension(op.widthMm, unit, decimalPlaces)
                    val opH = UnitConverter.formatDimension(op.heightMm, unit, decimalPlaces)
                    val opArea = UnitConverter.formatArea(WallMeasureCalculator.calculateOpeningArea(op), outputUnit, decimalPlaces)
                    val qtyStr = if (op.quantity > 1) " (Qty: ${op.quantity})" else ""
                    sb.appendLine("  ${op.type.label} (${op.name})$qtyStr — $opW × $opH — $opArea")
                }
            } else {
                sb.appendLine("Openings:\nNone (0.00 ${outputUnit.symbol})")
            }

            sb.appendLine("Net Wall Area:\n${UnitConverter.formatArea(wallResult.netAreaMm2, outputUnit, decimalPlaces)}")
            if (allowancePercent > 0.0) {
                sb.appendLine("${UnitConverter.formatNumber(allowancePercent, 1)}% Allowance:\n${UnitConverter.formatArea(wallResult.allowanceAreaMm2, outputUnit, decimalPlaces)}")
                sb.appendLine("Planning Area:\n${UnitConverter.formatArea(wallResult.planningAreaMm2, outputUnit, decimalPlaces)}")
            }
            sb.appendLine("----------------------------------------")
        }

        if (walls.size > 1) {
            sb.appendLine("TOTAL WALL SUMMARY:")
            sb.appendLine("Walls: ${walls.size}")
            sb.appendLine("Total Gross Area: ${UnitConverter.formatArea(totalResult.grossAreaMm2, outputUnit, decimalPlaces)}")
            sb.appendLine("Total Openings Area: ${UnitConverter.formatArea(totalResult.openingAreaMm2, outputUnit, decimalPlaces)}")
            sb.appendLine("Total Net Wall Area: ${UnitConverter.formatArea(totalResult.netAreaMm2, outputUnit, decimalPlaces)}")
            if (allowancePercent > 0.0) {
                sb.appendLine("Total Planning Area: ${UnitConverter.formatArea(totalResult.planningAreaMm2, outputUnit, decimalPlaces)}")
            }
            sb.appendLine("----------------------------------------")
        }

        sb.appendLine("Generated offline with WallMeasure.")
        return sb.toString()
    }

    fun generateCsvExport(
        walls: List<Wall>,
        unit: UnitType,
        outputUnit: AreaUnitType,
        allowancePercent: Double,
        decimalPlaces: Int = 2
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Wall,Width (${unit.symbol}),Height (${unit.symbol}),Gross Area (${outputUnit.symbol}),Opening Area (${outputUnit.symbol}),Net Area (${outputUnit.symbol}),Planning Area (${outputUnit.symbol})")

        walls.forEach { wall ->
            val res = WallMeasureCalculator.calculateWallResult(wall, allowancePercent)
            val w = UnitConverter.formatNumber(UnitConverter.fromMillimeters(wall.widthMm, unit), decimalPlaces)
            val h = UnitConverter.formatNumber(UnitConverter.fromMillimeters(wall.heightMm, unit), decimalPlaces)
            val gross = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(res.grossAreaMm2, outputUnit), decimalPlaces)
            val openings = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(res.openingAreaMm2, outputUnit), decimalPlaces)
            val net = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(res.netAreaMm2, outputUnit), decimalPlaces)
            val planning = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(res.planningAreaMm2, outputUnit), decimalPlaces)

            sb.appendLine("\"${wall.name}\",$w,$h,$gross,$openings,$net,$planning")
        }

        if (walls.size > 1) {
            val total = WallMeasureCalculator.calculateTotalWallResult(walls, allowancePercent)
            val totalGross = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(total.grossAreaMm2, outputUnit), decimalPlaces)
            val totalOpenings = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(total.openingAreaMm2, outputUnit), decimalPlaces)
            val totalNet = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(total.netAreaMm2, outputUnit), decimalPlaces)
            val totalPlanning = UnitConverter.formatNumber(UnitConverter.fromMm2ToAreaUnit(total.planningAreaMm2, outputUnit), decimalPlaces)
            sb.appendLine("\"TOTAL\",,,$totalGross,$totalOpenings,$totalNet,$totalPlanning")
        }

        return sb.toString()
    }

    fun generateJsonExport(
        walls: List<Wall>,
        calculationTitle: String,
        unit: UnitType,
        outputUnit: AreaUnitType,
        allowancePercent: Double,
        decimalPlaces: Int = 2
    ): String {
        if (walls.size == 1) {
            val wall = walls.first()
            val res = WallMeasureCalculator.calculateWallResult(wall, allowancePercent)
            val root = JSONObject()
            root.put("name", wall.name)
            root.put("unit", unit.name)
            root.put("width", UnitConverter.fromMillimeters(wall.widthMm, unit))
            root.put("height", UnitConverter.fromMillimeters(wall.heightMm, unit))
            root.put("grossAreaM2", UnitConverter.fromMm2ToAreaUnit(res.grossAreaMm2, AreaUnitType.M2))

            val openingsArray = JSONArray()
            wall.openings.forEach { op ->
                val opObj = JSONObject()
                opObj.put("name", op.name)
                opObj.put("type", op.type.name)
                opObj.put("width", UnitConverter.fromMillimeters(op.widthMm, unit))
                opObj.put("height", UnitConverter.fromMillimeters(op.heightMm, unit))
                opObj.put("quantity", op.quantity)
                opObj.put("areaM2", UnitConverter.fromMm2ToAreaUnit(WallMeasureCalculator.calculateOpeningArea(op), AreaUnitType.M2))
                openingsArray.put(opObj)
            }
            root.put("openings", openingsArray)
            root.put("netAreaM2", UnitConverter.fromMm2ToAreaUnit(res.netAreaMm2, AreaUnitType.M2))
            if (allowancePercent > 0.0) {
                root.put("allowancePercent", allowancePercent)
                root.put("planningAreaM2", UnitConverter.fromMm2ToAreaUnit(res.planningAreaMm2, AreaUnitType.M2))
            }
            return root.toString(2)
        } else {
            val root = JSONObject()
            root.put("title", calculationTitle)
            root.put("unit", unit.name)
            val wallsArray = JSONArray()
            walls.forEach { wall ->
                val res = WallMeasureCalculator.calculateWallResult(wall, allowancePercent)
                val wallObj = JSONObject()
                wallObj.put("name", wall.name)
                wallObj.put("width", UnitConverter.fromMillimeters(wall.widthMm, unit))
                wallObj.put("height", UnitConverter.fromMillimeters(wall.heightMm, unit))
                wallObj.put("grossAreaM2", UnitConverter.fromMm2ToAreaUnit(res.grossAreaMm2, AreaUnitType.M2))
                val openingsArray = JSONArray()
                wall.openings.forEach { op ->
                    val opObj = JSONObject()
                    opObj.put("name", op.name)
                    opObj.put("type", op.type.name)
                    opObj.put("width", UnitConverter.fromMillimeters(op.widthMm, unit))
                    opObj.put("height", UnitConverter.fromMillimeters(op.heightMm, unit))
                    opObj.put("quantity", op.quantity)
                    opObj.put("areaM2", UnitConverter.fromMm2ToAreaUnit(WallMeasureCalculator.calculateOpeningArea(op), AreaUnitType.M2))
                    openingsArray.put(opObj)
                }
                wallObj.put("openings", openingsArray)
                wallObj.put("netAreaM2", UnitConverter.fromMm2ToAreaUnit(res.netAreaMm2, AreaUnitType.M2))
                wallsArray.put(wallObj)
            }
            root.put("walls", wallsArray)
            val total = WallMeasureCalculator.calculateTotalWallResult(walls, allowancePercent)
            root.put("totalGrossAreaM2", UnitConverter.fromMm2ToAreaUnit(total.grossAreaMm2, AreaUnitType.M2))
            root.put("totalNetAreaM2", UnitConverter.fromMm2ToAreaUnit(total.netAreaMm2, AreaUnitType.M2))
            if (allowancePercent > 0.0) {
                root.put("allowancePercent", allowancePercent)
                root.put("totalPlanningAreaM2", UnitConverter.fromMm2ToAreaUnit(total.planningAreaMm2, AreaUnitType.M2))
            }
            return root.toString(2)
        }
    }

    fun buildCopyResultText(
        wall: Wall,
        unit: UnitType,
        outputUnit: AreaUnitType,
        allowancePercent: Double,
        decimalPlaces: Int = 2
    ): String {
        val res = WallMeasureCalculator.calculateWallResult(wall, allowancePercent)
        val w = UnitConverter.formatDimension(wall.widthMm, unit, decimalPlaces, includeUnit = false)
        val h = UnitConverter.formatDimension(wall.heightMm, unit, decimalPlaces, includeUnit = false)
        val gross = UnitConverter.formatArea(res.grossAreaMm2, outputUnit, decimalPlaces)
        val openings = UnitConverter.formatArea(res.openingAreaMm2, outputUnit, decimalPlaces)
        val net = UnitConverter.formatArea(res.netAreaMm2, outputUnit, decimalPlaces)

        val sb = StringBuilder()
        sb.appendLine("WallMeasure")
        sb.appendLine()
        sb.appendLine("Wall:")
        sb.appendLine(wall.name)
        sb.appendLine()
        sb.appendLine("Dimensions:")
        sb.appendLine("$w × $h ${unit.symbol}")
        sb.appendLine()
        sb.appendLine("Gross Area:")
        sb.appendLine(gross)
        sb.appendLine()
        sb.appendLine("Openings:")
        sb.appendLine(openings)
        sb.appendLine()
        sb.appendLine("Net Wall Area:")
        sb.append(net)

        if (allowancePercent > 0.0) {
            val planning = UnitConverter.formatArea(res.planningAreaMm2, outputUnit, decimalPlaces)
            sb.appendLine()
            sb.appendLine()
            sb.appendLine("${UnitConverter.formatNumber(allowancePercent, 1)}% Planning Area:")
            sb.append(planning)
        }

        return sb.toString()
    }

    fun buildShareText(
        wall: Wall,
        unit: UnitType,
        outputUnit: AreaUnitType,
        allowancePercent: Double,
        decimalPlaces: Int = 2
    ): String {
        val res = WallMeasureCalculator.calculateWallResult(wall, allowancePercent)
        val w = UnitConverter.formatDimension(wall.widthMm, unit, decimalPlaces, includeUnit = false)
        val h = UnitConverter.formatDimension(wall.heightMm, unit, decimalPlaces, includeUnit = false)
        val gross = UnitConverter.formatArea(res.grossAreaMm2, outputUnit, decimalPlaces)
        val openings = UnitConverter.formatArea(res.openingAreaMm2, outputUnit, decimalPlaces)
        val net = UnitConverter.formatArea(res.netAreaMm2, outputUnit, decimalPlaces)

        val sb = StringBuilder()
        sb.appendLine("WallMeasure")
        sb.appendLine()
        sb.appendLine(wall.name)
        sb.appendLine("$w × $h ${unit.symbol}")
        sb.appendLine()
        sb.appendLine("Gross:")
        sb.appendLine(gross)
        sb.appendLine()
        sb.appendLine("Openings:")
        sb.appendLine(openings)
        sb.appendLine()
        sb.appendLine("Net:")
        sb.append(net)

        if (allowancePercent > 0.0) {
            val planning = UnitConverter.formatArea(res.planningAreaMm2, outputUnit, decimalPlaces)
            sb.appendLine()
            sb.appendLine()
            sb.appendLine("Planning Area (${UnitConverter.formatNumber(allowancePercent, 1)}%):")
            sb.append(planning)
        }

        return sb.toString()
    }

    fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareCalculation(context: Context, text: String, title: String = "Wall Calculation") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share Wall Calculation")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
