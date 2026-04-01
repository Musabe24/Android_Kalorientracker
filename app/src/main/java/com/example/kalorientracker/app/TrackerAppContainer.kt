package com.example.kalorientracker.app

import android.content.Context
import androidx.room.Room
import com.example.kalorientracker.data.calorie.CalorieEntryStorageCodec
import com.example.kalorientracker.data.calorie.CalorieTrackerDatabase
import com.example.kalorientracker.data.calorie.LegacyCalorieEntryStore
import com.example.kalorientracker.data.calorie.RoomCalorieEntryRepository
import com.example.kalorientracker.data.calorie.RoomGoalTargetRepository
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieHistoryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieTimelineTrendUseCase
import com.example.kalorientracker.domain.calorie.LoadGoalTargetUseCase
import com.example.kalorientracker.domain.calorie.LoadWeeklyCalorieTrendUseCase
import com.example.kalorientracker.domain.calorie.PortionCalorieCalculator
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.UpdateGoalTargetUseCase
import java.time.Clock

interface TrackerAppContainer {
    val clock: Clock
    val saveCalorieEntryUseCase: SaveCalorieEntryUseCase
    val deleteCalorieEntryUseCase: DeleteCalorieEntryUseCase
    val loadCalorieHistoryUseCase: LoadCalorieHistoryUseCase
    val loadCalorieTimelineTrendUseCase: LoadCalorieTimelineTrendUseCase
    val loadCalorieOverviewUseCase: LoadCalorieOverviewUseCase
    val loadWeeklyCalorieTrendUseCase: LoadWeeklyCalorieTrendUseCase
    val loadGoalTargetUseCase: LoadGoalTargetUseCase
    val updateGoalTargetUseCase: UpdateGoalTargetUseCase
    val analyzeMealUseCase: com.example.kalorientracker.domain.calorie.AnalyzeMealUseCase
    val loadAiApiKeyUseCase: com.example.kalorientracker.domain.calorie.LoadAiApiKeyUseCase
    val saveAiApiKeyUseCase: com.example.kalorientracker.domain.calorie.SaveAiApiKeyUseCase
    val calculateGoalProgressUseCase: CalculateGoalProgressUseCase
    val portionCalorieCalculator: PortionCalorieCalculator
}

class DefaultTrackerAppContainer(context: Context) : TrackerAppContainer {
    override val clock: Clock = Clock.systemDefaultZone()

    private val database: CalorieTrackerDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            CalorieTrackerDatabase::class.java,
            TRACKER_DATABASE
        ).addMigrations(
            CalorieTrackerDatabase.Migration1To2,
            CalorieTrackerDatabase.Migration2To3
        ).build()
    }

    private val calorieRepository by lazy {
        RoomCalorieEntryRepository(
            calorieEntryDao = database.calorieEntryDao(),
            legacyCalorieEntryStore = LegacyCalorieEntryStore(
                sharedPreferences = context.applicationContext.getSharedPreferences(
                    LEGACY_TRACKER_PREFERENCES,
                    Context.MODE_PRIVATE
                ),
                storageCodec = CalorieEntryStorageCodec()
            )
        )
    }

    private val goalTargetRepository by lazy {
        RoomGoalTargetRepository(goalSettingsDao = database.goalSettingsDao())
    }

    private val settingsRepository by lazy {
        com.example.kalorientracker.data.calorie.SharedPreferencesSettingsRepository(
            sharedPreferences = context.applicationContext.getSharedPreferences(
                SETTINGS_PREFERENCES,
                Context.MODE_PRIVATE
            )
        )
    }

    private val dailyCalorieCalculator = DailyCalorieCalculator()
    private val calorieInputValidator = CalorieInputValidator()

    override val saveCalorieEntryUseCase: SaveCalorieEntryUseCase by lazy {
        SaveCalorieEntryUseCase(
            repository = calorieRepository,
            inputValidator = calorieInputValidator,
            clock = clock
        )
    }

    override val portionCalorieCalculator: PortionCalorieCalculator by lazy {
        PortionCalorieCalculator(inputValidator = calorieInputValidator)
    }

    override val deleteCalorieEntryUseCase: DeleteCalorieEntryUseCase by lazy {
        DeleteCalorieEntryUseCase(repository = calorieRepository)
    }

    override val loadCalorieHistoryUseCase: LoadCalorieHistoryUseCase by lazy {
        LoadCalorieHistoryUseCase(
            repository = calorieRepository,
            dailyCalorieCalculator = dailyCalorieCalculator
        )
    }

    override val loadCalorieTimelineTrendUseCase: LoadCalorieTimelineTrendUseCase by lazy {
        LoadCalorieTimelineTrendUseCase(
            repository = calorieRepository,
            dailyCalorieCalculator = dailyCalorieCalculator,
            clock = clock
        )
    }

    override val loadCalorieOverviewUseCase: LoadCalorieOverviewUseCase by lazy {
        LoadCalorieOverviewUseCase(
            repository = calorieRepository,
            dailyCalorieCalculator = dailyCalorieCalculator,
            clock = clock
        )
    }

    override val loadWeeklyCalorieTrendUseCase: LoadWeeklyCalorieTrendUseCase by lazy {
        LoadWeeklyCalorieTrendUseCase(
            repository = calorieRepository,
            dailyCalorieCalculator = dailyCalorieCalculator,
            clock = clock
        )
    }

    override val loadGoalTargetUseCase: LoadGoalTargetUseCase by lazy {
        LoadGoalTargetUseCase(repository = goalTargetRepository)
    }

    override val updateGoalTargetUseCase: UpdateGoalTargetUseCase by lazy {
        UpdateGoalTargetUseCase(repository = goalTargetRepository)
    }

    override val loadAiApiKeyUseCase: com.example.kalorientracker.domain.calorie.LoadAiApiKeyUseCase by lazy {
        com.example.kalorientracker.domain.calorie.LoadAiApiKeyUseCase(repository = settingsRepository)
    }

    override val saveAiApiKeyUseCase: com.example.kalorientracker.domain.calorie.SaveAiApiKeyUseCase by lazy {
        com.example.kalorientracker.domain.calorie.SaveAiApiKeyUseCase(repository = settingsRepository)
    }

    override val analyzeMealUseCase: com.example.kalorientracker.domain.calorie.AnalyzeMealUseCase by lazy {
        com.example.kalorientracker.domain.calorie.AnalyzeMealUseCase(
            aiMealParser = com.example.kalorientracker.data.calorie.GeminiAiMealParser(
                apiKey = "" // Updated reactively by ViewModel
            )
        )
    }

    override val calculateGoalProgressUseCase: CalculateGoalProgressUseCase = CalculateGoalProgressUseCase()

    private companion object {
        const val TRACKER_DATABASE = "calorie_tracker.db"
        const val LEGACY_TRACKER_PREFERENCES = "calorie_tracker_preferences"
        const val SETTINGS_PREFERENCES = "tracker_settings"
    }
}
