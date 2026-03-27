package com.example.kalorientracker

import com.example.kalorientracker.ui.home.GreetingViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingViewModelTest {
    @Test
    fun `ui state starts with default user name`() {
        val viewModel = GreetingViewModel()

        assertEquals("Kalorientracker", viewModel.uiState.value.userName)
    }
}
