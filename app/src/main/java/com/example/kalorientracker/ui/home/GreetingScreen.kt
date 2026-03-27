package com.example.kalorientracker.ui.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kalorientracker.R
import com.example.kalorientracker.ui.theme.KalorientrackerTheme

object GreetingScreenTestTags {
    const val GREETING_TEXT = "greeting_text"
}

@Composable
fun GreetingScreen(viewModel: GreetingViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GreetingContent(uiState = uiState, modifier = modifier)
}

@Composable
fun GreetingContent(uiState: GreetingUiState, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.greeting_message, uiState.userName),
        modifier = modifier.testTag(GreetingScreenTestTags.GREETING_TEXT)
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingContentPreview() {
    KalorientrackerTheme {
        GreetingContent(GreetingUiState())
    }
}
