package com.example.wallmeasure.data

import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallMeasureHistory

object HistoryManager {
    const val MAX_HISTORY_ITEMS = 10

    /**
     * Adds an item to the history list according to requirements:
     * - Returns Pair(updatedList, wasSaved)
     * - If duplicate (same name & dimensions), rejects and returns false
     * - Adds newest to the top
     * - If size > 10, removes the oldest entry (from end), newest remains
     */
    fun addHistoryItem(
        currentList: List<WallMeasureHistory>,
        newItem: WallMeasureHistory
    ): Pair<List<WallMeasureHistory>, Boolean> {
        val isDuplicate = currentList.any { existing ->
            existing.name.equals(newItem.name, ignoreCase = true) &&
                    areWallsEqual(existing.walls, newItem.walls)
        }
        if (isDuplicate) {
            return Pair(currentList, false)
        }

        val updated = currentList.toMutableList()
        updated.add(0, newItem)

        while (updated.size > MAX_HISTORY_ITEMS) {
            updated.removeAt(updated.lastIndex)
        }

        return Pair(updated, true)
    }

    fun areWallsEqual(w1: List<Wall>, w2: List<Wall>): Boolean {
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
}
