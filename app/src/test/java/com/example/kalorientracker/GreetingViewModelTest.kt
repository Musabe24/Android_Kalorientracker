package com.example.kalorientracker

import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.ui.home.GreetingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GreetingViewModelTest {
    @Test
    fun `add entry updates summary for intake`() {
        val viewModel = GreetingViewModel()

        viewModel.onCalorieInputChanged("500")
        viewModel.onEntryTypeSelected(CalorieEntryType.INTAKE)
        viewModel.onEntrySourceSelected(CalorieEntrySource.MEAL)
        viewModel.addEntry()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals(500, uiState.totalIntake)
        assertEquals(0, uiState.totalBurned)
        assertEquals(500, uiState.netCalories)
    }

    @Test
    fun `add entry updates summary for burned calories`() {
        val viewModel = GreetingViewModel()

        viewModel.onCalorieInputChanged("600")
        viewModel.onEntryTypeSelected(CalorieEntryType.INTAKE)
        viewModel.addEntry()

        viewModel.onCalorieInputChanged("250")
        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        viewModel.onEntrySourceSelected(CalorieEntrySource.WATCH)
        viewModel.addEntry()

        val uiState = viewModel.uiState.value
        assertEquals(600, uiState.totalIntake)
        assertEquals(250, uiState.totalBurned)
        assertEquals(350, uiState.netCalories)
    }

    @Test
    fun `add entry with invalid calories exposes input error`() {
        val viewModel = GreetingViewModel()

        viewModel.onCalorieInputChanged("0")
        viewModel.addEntry()

        assertNotNull(viewModel.uiState.value.inputError)
    }
}
