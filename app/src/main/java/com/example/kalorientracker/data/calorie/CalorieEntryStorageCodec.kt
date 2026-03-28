package com.example.kalorientracker.data.calorie

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

/**
 * Encodes persisted calorie entries as a strict line-based payload.
 */
class CalorieEntryStorageCodec(
    private val clock: Clock = Clock.systemDefaultZone()
) {
    fun encode(entries: List<CalorieEntry>): String {
        return entries.joinToString(separator = ENTRY_SEPARATOR) { entry ->
            listOf(
                entry.id,
                entry.amount.toString(),
                entry.type.name,
                entry.source.name,
                entry.recordedOnEpochDay.toString()
            )
                .joinToString(FIELD_SEPARATOR)
        }
    }

    fun decode(serializedEntries: String): List<CalorieEntry> {
        if (serializedEntries.isBlank()) {
            return emptyList()
        }

        return serializedEntries
            .lineSequence()
            .filter { it.isNotBlank() }
            .map(::decodeEntry)
            .toList()
    }

    private fun decodeEntry(encodedEntry: String): CalorieEntry {
        val fields = encodedEntry.split(FIELD_SEPARATOR)
        return when (fields.size) {
            FIELD_COUNT -> decodeCurrentEntry(fields)
            LEGACY_FIELD_COUNT -> decodeLegacyEntry(encodedEntry, fields)
            else -> throw IllegalStateException("Stored calorie entries are malformed.")
        }
    }

    private fun decodeCurrentEntry(fields: List<String>): CalorieEntry {
        check(fields[ID_INDEX].isNotBlank()) {
            "Stored calorie entries are malformed."
        }

        val amount = fields[AMOUNT_INDEX].toIntOrNull()
            ?: throw IllegalStateException("Stored calorie entries are malformed.")

        val type = parseEntryType(fields[TYPE_INDEX])
        val source = parseEntrySource(fields[SOURCE_INDEX])
        val recordedOnEpochDay = fields[RECORDED_ON_INDEX].toLongOrNull()
            ?: throw IllegalStateException("Stored calorie entries are malformed.")

        return CalorieEntry(
            id = fields[ID_INDEX],
            amount = amount,
            type = type,
            source = source,
            recordedOnEpochDay = recordedOnEpochDay
        )
    }

    private fun decodeLegacyEntry(encodedEntry: String, fields: List<String>): CalorieEntry {
        val amount = fields[LEGACY_AMOUNT_INDEX].toIntOrNull()
            ?: throw IllegalStateException("Stored calorie entries are malformed.")

        return CalorieEntry(
            id = UUID.nameUUIDFromBytes(encodedEntry.toByteArray()).toString(),
            amount = amount,
            type = parseEntryType(fields[LEGACY_TYPE_INDEX]),
            source = parseEntrySource(fields[LEGACY_SOURCE_INDEX]),
            recordedOnEpochDay = LocalDate.now(clock).toEpochDay()
        )
    }

    private companion object {
        const val ENTRY_SEPARATOR = "\n"
        const val FIELD_SEPARATOR = "|"
        const val FIELD_COUNT = 5
        const val LEGACY_FIELD_COUNT = 3
        const val ID_INDEX = 0
        const val AMOUNT_INDEX = 1
        const val TYPE_INDEX = 2
        const val SOURCE_INDEX = 3
        const val RECORDED_ON_INDEX = 4
        const val LEGACY_AMOUNT_INDEX = 0
        const val LEGACY_TYPE_INDEX = 1
        const val LEGACY_SOURCE_INDEX = 2
    }

    private fun parseEntryType(rawType: String): CalorieEntryType {
        return try {
            CalorieEntryType.valueOf(rawType)
        } catch (_: IllegalArgumentException) {
            throw IllegalStateException("Stored calorie entries are malformed.")
        }
    }

    private fun parseEntrySource(rawSource: String): CalorieEntrySource {
        return try {
            CalorieEntrySource.valueOf(rawSource)
        } catch (_: IllegalArgumentException) {
            throw IllegalStateException("Stored calorie entries are malformed.")
        }
    }
}
