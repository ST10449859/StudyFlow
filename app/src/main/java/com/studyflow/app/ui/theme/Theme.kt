package com.studyflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudyBlue = Color(0xFF2563EB)
private val LightColors = lightColorScheme(primary = StudyBlue, secondary = Color(0xFF0F766E), tertiary = Color(0xFF7C3AED))

@Composable
fun StudyFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
