package com.example.memodeck.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PomodoroTimer() {
    var timeLeft by remember { mutableStateOf(25 * 60) } // 25 минут
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) isRunning = false
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .size(70.dp)
            .background(if (isRunning) PeachPrimary else SoftBlue, CircleShape)
            .clickable { isRunning = !isRunning },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeString,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}