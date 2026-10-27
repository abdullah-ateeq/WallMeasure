package com.example.wallmeasure.data

import android.content.Context
import android.content.SharedPreferences
import com.example.wallmeasure.model.AppThemeSetting
import com.example.wallmeasure.model.AreaUnitType
import com.example.wallmeasure.model.CalculationMode
import com.example.wallmeasure.model.Opening
import com.example.wallmeasure.model.OpeningType
import com.example.wallmeasure.model.UnitType
import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallHeightMode
import com.example.wallmeasure.model.WallMeasureHistory
import com.example.wallmeasure.model.WallMeasureSettings
import com.example.wallmeasure.model.WallOrientation
import org.json.JSONArray
import org.json.JSONObject

class WallMeasureRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "wallmeasure_preferences"
        private const val KEY_HISTORY = "key_history_json"
        private const val KEY_SETTINGS_THEME = "key_settings_theme"
        private const val KEY_SETTINGS_DYNAMIC_COLOR = "key_settings_dynamic_color"
        private const val KEY_SETTINGS_DEFAULT_UNIT = "key_settings_default_unit"
        private const val KEY_SETTINGS_DEFAULT_OUTPUT = "key_settings_default_output"
        private const val KEY_SETTINGS_DECIMAL_PLACES = "key_settings_decimal_places"
        private const val KEY_SETTINGS_DEFAULT_ALLOWANCE = "key_settings_default_allowance"
        private const val KEY_SETTINGS_SHOW_DIAGRAM = "key_settings_show_diagram"
        private const val KEY_SETTINGS_SHOW_GROSS = "key_settings_show_gross"
        private const val KEY_SETTINGS_SHOW_OPENINGS = "key_settings_show_openings"
        private const val KEY_SETTINGS_SHOW_NET = "key_settings_show_net"
        private const val KEY_SETTINGS_SHOW_PLANNING = "key_settings_show_planning"
        private const val KEY_SETTINGS_LIVE_CALC = "key_settings_live_calc"
        private const val KEY_SETTINGS_HAPTICS = "key_settings_haptics"
        private const val KEY_SETTINGS_ANIMATIONS = "key_settings_animations"
        private const val KEY_SETTINGS_CONFIRM_DELETE = "key_settings_confirm_delete"
        private const val KEY_SETTINGS_RESTORE_LAST = "key_settings_restore_last"

        // Last calculation state keys
        private const val KEY_LAST_MODE = "key_last_mode"
        private const val KEY_LAST_UNIT = "key_last_unit"
        private const val KEY_LAST_WALL_NAME = "key_last_wall_name"
        private const val KEY_LAST_WIDTH_MM = "key_last_width_mm"
        private const val KEY_LAST_HEIGHT_MM = "key_last_height_mm"
        private const val KEY_LAST_OPENINGS = "key_last_openings"
        private const val KEY_LAST_ALLOWANCE = "key_last_allowance"
        private const val KEY_LAST_ORIENTATION = "key_last_orientation"
        private const val KEY_LAST_SHOW_OPENINGS_ON_DIAGRAM = "key_last_show_openings_on_diagram"

        // Room mode keys
        private const val KEY_ROOM_LENGTH_MM = "key_room_length_mm"
        private const val KEY_ROOM_WIDTH_MM = "key_room_width_mm"
        private const val KEY_ROOM_HEIGHT_MM = "key_room_height_mm"
        private const val KEY_ROOM_INCLUDE_CEILING = "key_room_include_ceiling"
        private const val KEY_ROOM_DOOR_COUNT = "key_room_door_count"
        private const val KEY_ROOM_WINDOW_COUNT = "key_room_window_count"
        private const val KEY_ROOM_DOOR_WIDTH_MM = "key_room_door_width_mm"
        private const val KEY_ROOM_DOOR_HEIGHT_MM = "key_room_door_height_mm"
        private const val KEY_ROOM_WINDOW_WIDTH_MM = "key_room_window_width_mm"
        private const val KEY_ROOM_WINDOW_HEIGHT_MM = "key_room_window_height_mm"

        // Multiple walls
        private const val KEY_MULTI_WALLS = "key_multi_walls"
        private const val KEY_MULTI_HEIGHT_MODE = "key_multi_height_mode"
        private const val KEY_MULTI_SHARED_HEIGHT_MM = "key_multi_shared_height_mm"

        const val MAX_HISTORY_ITEMS = 10
    }

    // =========================================================================
    // HISTORY
    // =========================================================================

    @Synchronized
    fun getHistory(): List<WallMeasureHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<WallMeasureHistory>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val createdAt = obj.getLong("createdAt")
                val wallsArray = obj.getJSONArray("walls")
                val walls = mutableListOf<Wall>()
                for (j in 0 until wallsArray.length()) {
                    val wObj = wallsArray.getJSONObject(j)
                    walls.add(parseWallJson(wObj))
                }
                list.add(WallMeasureHistory(id = id, name = name, walls = walls, createdAt = createdAt))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @Synchronized
    fun saveHistoryItem(item: WallMeasureHistory): Boolean {
        val currentList = getHistory()
        val (updatedList, saved) = HistoryManager.addHistoryItem(currentList, item)
        if (saved) {
            saveHistoryList(updatedList)
        }
        return saved
    }

    @Synchronized
    fun deleteHistoryItem(id: String) {
        val currentList = getHistory().filterNot { it.id == id }
        saveHistoryList(currentList)
    }

    @Synchronized
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistoryList(list: List<WallMeasureHistory>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("createdAt", item.createdAt)
            val wallsArray = JSONArray()
            for (wall in item.walls) {
                wallsArray.put(serializeWallJson(wall))
            }
            obj.put("walls", wallsArray)
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    private fun areWallsEqual(w1: List<Wall>, w2: List<Wall>): Boolean {
        if (w1.size != w2.size) return false
        for (i in w1.indices) {
            val a = w1[i]
            val b = w2[i]
            if (a.widthMm != b.widthMm || a.heightMm != b.heightMm || a.name != b.name) return false
            if (a.openings.size != b.openings.size) return false
            for (j in a.openings.indices) {
                val oa = a.openings[j]
                val ob = b.openings[j]
                if (oa.widthMm != ob.widthMm || oa.heightMm != ob.heightMm || oa.quantity != ob.quantity || oa.type != ob.type) return false
            }
        }
        return true
    }

    // =========================================================================
    // SETTINGS
    // =========================================================================

    fun getSettings(): WallMeasureSettings {
        val themeStr = prefs.getString(KEY_SETTINGS_THEME, AppThemeSetting.SYSTEM.name) ?: AppThemeSetting.SYSTEM.name
        val theme = runCatching { AppThemeSetting.valueOf(themeStr) }.getOrDefault(AppThemeSetting.SYSTEM)

        val unitStr = prefs.getString(KEY_SETTINGS_DEFAULT_UNIT, UnitType.CM.name) ?: UnitType.CM.name
        val unit = runCatching { UnitType.valueOf(unitStr) }.getOrDefault(UnitType.CM)

        val outStr = prefs.getString(KEY_SETTINGS_DEFAULT_OUTPUT, AreaUnitType.M2.name) ?: AreaUnitType.M2.name
        val outUnit = runCatching { AreaUnitType.valueOf(outStr) }.getOrDefault(AreaUnitType.M2)

        return WallMeasureSettings(
            theme = theme,
            dynamicColor = prefs.getBoolean(KEY_SETTINGS_DYNAMIC_COLOR, true),
            defaultUnit = unit,
            defaultOutputUnit = outUnit,
            decimalPlaces = prefs.getInt(KEY_SETTINGS_DECIMAL_PLACES, 2),
            defaultAllowancePercent = prefs.getFloat(KEY_SETTINGS_DEFAULT_ALLOWANCE, 0f).toDouble(),
            showDimensionDiagram = prefs.getBoolean(KEY_SETTINGS_SHOW_DIAGRAM, true),
            showGrossArea = prefs.getBoolean(KEY_SETTINGS_SHOW_GROSS, true),
            showOpenings = prefs.getBoolean(KEY_SETTINGS_SHOW_OPENINGS, true),
            showNetArea = prefs.getBoolean(KEY_SETTINGS_SHOW_NET, true),
            showPlanningArea = prefs.getBoolean(KEY_SETTINGS_SHOW_PLANNING, true),
            liveCalculation = prefs.getBoolean(KEY_SETTINGS_LIVE_CALC, true),
            haptics = prefs.getBoolean(KEY_SETTINGS_HAPTICS, true),
            animations = prefs.getBoolean(KEY_SETTINGS_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_SETTINGS_CONFIRM_DELETE, true),
            restoreLastCalculation = prefs.getBoolean(KEY_SETTINGS_RESTORE_LAST, true)
        )
    }

    fun saveSettings(settings: WallMeasureSettings) {
        prefs.edit()
            .putString(KEY_SETTINGS_THEME, settings.theme.name)
            .putBoolean(KEY_SETTINGS_DYNAMIC_COLOR, settings.dynamicColor)
            .putString(KEY_SETTINGS_DEFAULT_UNIT, settings.defaultUnit.name)
            .putString(KEY_SETTINGS_DEFAULT_OUTPUT, settings.defaultOutputUnit.name)
            .putInt(KEY_SETTINGS_DECIMAL_PLACES, settings.decimalPlaces)
            .putFloat(KEY_SETTINGS_DEFAULT_ALLOWANCE, settings.defaultAllowancePercent.toFloat())
            .putBoolean(KEY_SETTINGS_SHOW_DIAGRAM, settings.showDimensionDiagram)
            .putBoolean(KEY_SETTINGS_SHOW_GROSS, settings.showGrossArea)
            .putBoolean(KEY_SETTINGS_SHOW_OPENINGS, settings.showOpenings)
            .putBoolean(KEY_SETTINGS_SHOW_NET, settings.showNetArea)
            .putBoolean(KEY_SETTINGS_SHOW_PLANNING, settings.showPlanningArea)
            .putBoolean(KEY_SETTINGS_LIVE_CALC, settings.liveCalculation)
            .putBoolean(KEY_SETTINGS_HAPTICS, settings.haptics)
            .putBoolean(KEY_SETTINGS_ANIMATIONS, settings.animations)
            .putBoolean(KEY_SETTINGS_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_SETTINGS_RESTORE_LAST, settings.restoreLastCalculation)
            .apply()
    }

    fun resetSettings() {
        val historyJson = prefs.getString(KEY_HISTORY, null)
        prefs.edit().clear().apply()
        // Restore history if it existed
        if (historyJson != null) {
            prefs.edit().putString(KEY_HISTORY, historyJson).apply()
        }
    }

    // =========================================================================
    // LAST CALCULATION STATE
    // =========================================================================

    fun saveLastState(
        mode: CalculationMode,
        unit: UnitType,
        wallName: String,
        widthMm: Double,
        heightMm: Double,
        openings: List<Opening>,
        allowancePercent: Double,
        orientation: WallOrientation,
        showOpeningsOnDiagram: Boolean,
        roomLengthMm: Double,
        roomWidthMm: Double,
        roomHeightMm: Double,
        roomIncludeCeiling: Boolean,
        multiWalls: List<Wall>,
        multiHeightMode: WallHeightMode,
        multiSharedHeightMm: Double
    ) {
        val editor = prefs.edit()
            .putString(KEY_LAST_MODE, mode.name)
            .putString(KEY_LAST_UNIT, unit.name)
            .putString(KEY_LAST_WALL_NAME, wallName)
            .putFloat(KEY_LAST_WIDTH_MM, widthMm.toFloat())
            .putFloat(KEY_LAST_HEIGHT_MM, heightMm.toFloat())
            .putString(KEY_LAST_OPENINGS, serializeOpenings(openings))
            .putFloat(KEY_LAST_ALLOWANCE, allowancePercent.toFloat())
            .putString(KEY_LAST_ORIENTATION, orientation.name)
            .putBoolean(KEY_LAST_SHOW_OPENINGS_ON_DIAGRAM, showOpeningsOnDiagram)
            .putFloat(KEY_ROOM_LENGTH_MM, roomLengthMm.toFloat())
            .putFloat(KEY_ROOM_WIDTH_MM, roomWidthMm.toFloat())
            .putFloat(KEY_ROOM_HEIGHT_MM, roomHeightMm.toFloat())
            .putBoolean(KEY_ROOM_INCLUDE_CEILING, roomIncludeCeiling)
            .putString(KEY_MULTI_WALLS, serializeWalls(multiWalls))
            .putString(KEY_MULTI_HEIGHT_MODE, multiHeightMode.name)
            .putFloat(KEY_MULTI_SHARED_HEIGHT_MM, multiSharedHeightMm.toFloat())
        editor.apply()
    }

    fun getLastMode(): CalculationMode {
        val name = prefs.getString(KEY_LAST_MODE, CalculationMode.SINGLE_WALL.name)
        return runCatching { CalculationMode.valueOf(name!!) }.getOrDefault(CalculationMode.SINGLE_WALL)
    }

    fun getLastUnit(): UnitType {
        val name = prefs.getString(KEY_LAST_UNIT, UnitType.CM.name)
        return runCatching { UnitType.valueOf(name!!) }.getOrDefault(UnitType.CM)
    }

    fun getLastWallName(): String = prefs.getString(KEY_LAST_WALL_NAME, "Wall 1") ?: "Wall 1"
    fun getLastWidthMm(): Double = prefs.getFloat(KEY_LAST_WIDTH_MM, 5000f).toDouble()
    fun getLastHeightMm(): Double = prefs.getFloat(KEY_LAST_HEIGHT_MM, 3000f).toDouble()
    fun getLastAllowance(): Double = prefs.getFloat(KEY_LAST_ALLOWANCE, 0f).toDouble()
    fun getLastOrientation(): WallOrientation {
        val name = prefs.getString(KEY_LAST_ORIENTATION, WallOrientation.HORIZONTAL.name)
        return runCatching { WallOrientation.valueOf(name!!) }.getOrDefault(WallOrientation.HORIZONTAL)
    }
    fun getLastShowOpeningsOnDiagram(): Boolean = prefs.getBoolean(KEY_LAST_SHOW_OPENINGS_ON_DIAGRAM, true)

    fun getLastOpenings(): List<Opening> {
        val json = prefs.getString(KEY_LAST_OPENINGS, null) ?: return emptyList()
        return deserializeOpenings(json)
    }

    fun getLastRoomLengthMm(): Double = prefs.getFloat(KEY_ROOM_LENGTH_MM, 5000f).toDouble()
    fun getLastRoomWidthMm(): Double = prefs.getFloat(KEY_ROOM_WIDTH_MM, 4000f).toDouble()
    fun getLastRoomHeightMm(): Double = prefs.getFloat(KEY_ROOM_HEIGHT_MM, 3000f).toDouble()
    fun getLastRoomIncludeCeiling(): Boolean = prefs.getBoolean(KEY_ROOM_INCLUDE_CEILING, false)

    fun getLastMultiWalls(): List<Wall> {
        val json = prefs.getString(KEY_MULTI_WALLS, null) ?: return emptyList()
        return deserializeWalls(json)
    }

    fun getLastMultiHeightMode(): WallHeightMode {
        val name = prefs.getString(KEY_MULTI_HEIGHT_MODE, WallHeightMode.INDIVIDUAL_HEIGHT.name)
        return runCatching { WallHeightMode.valueOf(name!!) }.getOrDefault(WallHeightMode.INDIVIDUAL_HEIGHT)
    }

    fun getLastMultiSharedHeightMm(): Double = prefs.getFloat(KEY_MULTI_SHARED_HEIGHT_MM, 3000f).toDouble()

    // =========================================================================
    // JSON HELPERS
    // =========================================================================

    private fun serializeWallJson(wall: Wall): JSONObject {
        val obj = JSONObject()
        obj.put("id", wall.id)
        obj.put("name", wall.name)
        obj.put("widthMm", wall.widthMm)
        obj.put("heightMm", wall.heightMm)
        val opArray = JSONArray()
        for (op in wall.openings) {
            opArray.put(serializeOpeningJson(op))
        }
        obj.put("openings", opArray)
        return obj
    }

    private fun parseWallJson(obj: JSONObject): Wall {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val widthMm = obj.getDouble("widthMm")
        val heightMm = obj.getDouble("heightMm")
        val opArray = obj.optJSONArray("openings")
        val openings = mutableListOf<Opening>()
        if (opArray != null) {
            for (i in 0 until opArray.length()) {
                openings.add(parseOpeningJson(opArray.getJSONObject(i)))
            }
        }
        return Wall(id = id, name = name, widthMm = widthMm, heightMm = heightMm, openings = openings)
    }

    private fun serializeOpeningJson(op: Opening): JSONObject {
        val obj = JSONObject()
        obj.put("id", op.id)
        obj.put("name", op.name)
        obj.put("type", op.type.name)
        obj.put("widthMm", op.widthMm)
        obj.put("heightMm", op.heightMm)
        obj.put("quantity", op.quantity)
        if (op.positionMm != null) obj.put("positionMm", op.positionMm)
        if (op.verticalPositionMm != null) obj.put("verticalPositionMm", op.verticalPositionMm)
        return obj
    }

    private fun parseOpeningJson(obj: JSONObject): Opening {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val typeStr = obj.getString("type")
        val type = runCatching { OpeningType.valueOf(typeStr) }.getOrDefault(OpeningType.OTHER)
        val widthMm = obj.getDouble("widthMm")
        val heightMm = obj.getDouble("heightMm")
        val quantity = obj.optInt("quantity", 1)
        val positionMm = if (obj.has("positionMm")) obj.getDouble("positionMm") else null
        val verticalPositionMm = if (obj.has("verticalPositionMm")) obj.getDouble("verticalPositionMm") else null
        return Opening(
            id = id,
            name = name,
            type = type,
            widthMm = widthMm,
            heightMm = heightMm,
            quantity = quantity,
            positionMm = positionMm,
            verticalPositionMm = verticalPositionMm
        )
    }

    private fun serializeOpenings(openings: List<Opening>): String {
        val array = JSONArray()
        for (op in openings) {
            array.put(serializeOpeningJson(op))
        }
        return array.toString()
    }

    private fun deserializeOpenings(json: String): List<Opening> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<Opening>()
            for (i in 0 until array.length()) {
                list.add(parseOpeningJson(array.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeWalls(walls: List<Wall>): String {
        val array = JSONArray()
        for (w in walls) {
            array.put(serializeWallJson(w))
        }
        return array.toString()
    }

    private fun deserializeWalls(json: String): List<Wall> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<Wall>()
            for (i in 0 until array.length()) {
                list.add(parseWallJson(array.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
