package com.example.kalorientracker.data.calorie

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieEntryStorageCodecTest {
    private val codec = CalorieEntryStorageCodec()

    @Test
    fun `encode and decode roundtrip keeps entries`() {
        val entries = listOf(
            CalorieEntry(
                id = "entry-1",
                amount = 300,
                type = CalorieEntryType.INTAKE,
                source = CalorieEntrySource.MEAL,
                recordedOnEpochDay = 19810L
            ),
            CalorieEntry(
                id = "entry-2",
                amount = 180,
                type = CalorieEntryType.BURNED,
                source = CalorieEntrySource.WATCH,
                recordedOnEpochDay = 19810L
            )
        )

        val decodedEntries = codec.decode(codec.encode(entries))

        assertEquals(entries, decodedEntries)
    }

    @Test
    fun `decode accepts legacy three field entries`() {
        val codec = CalorieEntryStorageCodec(
            clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
        )

        val decodedEntries = codec.decode("300|INTAKE|MEAL")

        assertEquals(1, decodedEntries.size)
        assertEquals(300, decodedEntries.single().amount)
        assertEquals(CalorieEntryType.INTAKE, decodedEntries.single().type)
        assertEquals(CalorieEntrySource.MEAL, decodedEntries.single().source)
        assertEquals(20540L, decodedEntries.single().recordedOnEpochDay)
        assertTrue(decodedEntries.single().id.isNotBlank())
    }

    @Test(expected = IllegalStateException::class)
    fun `decode throws when payload is malformed`() {
        codec.decode("entry-1|300|INTAKE")
    }
}
