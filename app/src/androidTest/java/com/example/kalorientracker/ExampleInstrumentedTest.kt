package com.example.kalorientracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.kalorientracker.ui.tracker.TrackerScreenTestTags
import org.junit.Rule
import org.junit.Test

class ExampleInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun startScreen_showsCalorieInputField() {
        composeRule.onNodeWithTag(TrackerScreenTestTags.CALORIE_INPUT_FIELD).assertIsDisplayed()
    }
}
