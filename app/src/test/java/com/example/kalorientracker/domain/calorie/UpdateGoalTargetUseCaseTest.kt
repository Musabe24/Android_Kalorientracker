package com.example.kalorientracker.domain.calorie

import com.example.kalorientracker.testutil.InMemoryGoalTargetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateGoalTargetUseCaseTest {
    private val repository = InMemoryGoalTargetRepository()
    private val useCase = UpdateGoalTargetUseCase(repository)

    @Test
    fun `invoke persists valid positive target`() = runTest {
        val result = useCase("2600")

        assertEquals(UpdateGoalTargetResult.Success(2600), result)
        assertEquals(2600, repository.getTargetCalories())
    }

    @Test
    fun `invoke rejects blank target`() = runTest {
        val result = useCase("")

        assertTrue(result is UpdateGoalTargetResult.ValidationError)
        assertEquals(CalculateGoalProgressUseCase.DEFAULT_TARGET_CALORIES, repository.getTargetCalories())
    }

    @Test
    fun `invoke rejects non numeric target`() = runTest {
        val result = useCase("abc")

        assertEquals(
            UpdateGoalTargetResult.ValidationError(GoalTargetValidationError.NotWholeNumber),
            result
        )
        assertEquals(CalculateGoalProgressUseCase.DEFAULT_TARGET_CALORIES, repository.getTargetCalories())
    }
}
