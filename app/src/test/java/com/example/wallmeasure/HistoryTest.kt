package com.example.wallmeasure

import com.example.wallmeasure.data.HistoryManager
import com.example.wallmeasure.model.Wall
import com.example.wallmeasure.model.WallMeasureHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryTest {

    @Test
    fun testHistoryFifoLimitAndDuplicatePrevention() {
        var list = emptyList<WallMeasureHistory>()

        // Save 10 unique calculations
        for (i in 1..10) {
            val item = WallMeasureHistory(
                id = "calc_$i",
                name = "Wall $i",
                walls = listOf(Wall(id = "w_$i", name = "Wall $i", widthMm = i * 1000.0, heightMm = 3000.0)),
                createdAt = 1000L * i
            )
            val (updated, saved) = HistoryManager.addHistoryItem(list, item)
            assertTrue("Item $i should be saved", saved)
            list = updated
        }

        assertEquals(10, list.size)
        assertEquals("Wall 10", list.first().name) // Newest is at index 0
        assertEquals("Wall 1", list.last().name)  // Oldest is at index 9

        // Try to save an exact duplicate of Wall 5
        val duplicateItem = WallMeasureHistory(
            id = "calc_dup",
            name = "Wall 5",
            walls = listOf(Wall(id = "w_5", name = "Wall 5", widthMm = 5000.0, heightMm = 3000.0)),
            createdAt = 50000L
        )
        val (listAfterDup, dupSaved) = HistoryManager.addHistoryItem(list, duplicateItem)
        assertFalse("Duplicate item should be rejected", dupSaved)
        assertEquals("List size should remain 10", 10, listAfterDup.size)

        // Save an 11th unique calculation
        val eleventhItem = WallMeasureHistory(
            id = "calc_11",
            name = "Wall 11",
            walls = listOf(Wall(id = "w_11", name = "Wall 11", widthMm = 11000.0, heightMm = 3000.0)),
            createdAt = 11000L
        )
        val (listAfter11, eleventhSaved) = HistoryManager.addHistoryItem(list, eleventhItem)
        assertTrue("Eleventh item should be saved", eleventhSaved)
        assertEquals("List size should remain capped at 10", 10, listAfter11.size)

        // Verify: Newest remains (at index 0)
        assertEquals("Newest item must be at index 0", "Wall 11", listAfter11.first().name)

        // Verify: Oldest ("Wall 1") is removed, and "Wall 2" is now the oldest at index 9
        assertEquals("Oldest remaining item must be Wall 2", "Wall 2", listAfter11.last().name)
        assertFalse("Wall 1 should have been evicted", listAfter11.any { it.name == "Wall 1" })
    }
}
