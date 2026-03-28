package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun trackerCardColor(): Color = MaterialTheme.colorScheme.surface

@Composable
internal fun trackerPanelColor(alpha: Float = 1f): Color =
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

@Composable
internal fun trackerOutlineColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

@Composable
internal fun trackerPrimaryTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
internal fun trackerSecondaryTextColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)

@Composable
internal fun trackerEyebrowColor(): Color = MaterialTheme.colorScheme.primary

@Composable
internal fun SectionHeader(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = trackerEyebrowColor()
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = trackerPrimaryTextColor()
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = trackerSecondaryTextColor()
        )
    }
}

@Composable
internal fun MetricPill(
    label: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}
