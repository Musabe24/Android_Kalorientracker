package com.example.kalorientracker.data.calorie

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType

@Entity(tableName = "calorie_entries")
data class CalorieEntryEntity(
    @PrimaryKey val id: String,
    val amount: Int,
    val type: String,
    val source: String,
    val recordedOnEpochDay: Long
)

fun CalorieEntryEntity.toDomain(): CalorieEntry {
    return CalorieEntry(
        id = id,
        amount = amount,
        type = CalorieEntryType.valueOf(type),
        source = CalorieEntrySource.valueOf(source),
        recordedOnEpochDay = recordedOnEpochDay
    )
}

fun CalorieEntry.toEntity(): CalorieEntryEntity {
    return CalorieEntryEntity(
        id = id,
        amount = amount,
        type = type.name,
        source = source.name,
        recordedOnEpochDay = recordedOnEpochDay
    )
}
