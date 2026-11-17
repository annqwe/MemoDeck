package com.example.memodeck.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: DeckViewModel, isDark: Boolean, onToggleTheme: () -> Unit) {
    val reviewedCards by viewModel.reviewedCards.collectAsStateWithLifecycle()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance()
    val activityMap = mutableMapOf<String, Int>()

    for (i in 0..6) {
        val cal = today.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -i)
        activityMap[dateFormat.format(cal.time)] = 0
    }

    reviewedCards.forEach { card ->
        card.lastReviewed?.let { time ->
            val dateStr = dateFormat.format(Date(time))
            if (activityMap.containsKey(dateStr)) {
                activityMap[dateStr] = activityMap[dateStr]!! + 1
            }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Мой профиль", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Brush.linearGradient(listOf(PeachPrimary, SoftBlue))), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = "Аватар", modifier = Modifier.size(70.dp), tint = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Студент", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Всего повторено: ${reviewedCards.size}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // ПЕРЕКЛЮЧАТЕЛЬ ТЕМЫ
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Темная тема", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // КАЛЕНДАРЬ АКТИВНОСТИ
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Активность за неделю", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (i in 6 downTo 0) {
                            val cal = today.clone() as Calendar
                            cal.add(Calendar.DAY_OF_YEAR, -i)
                            val dateStr = dateFormat.format(cal.time)
                            val count = activityMap[dateStr] ?: 0

                            val boxColor = when {
                                count == 0 -> Color.LightGray.copy(alpha = 0.3f)
                                count < 3 -> SoftGreen.copy(alpha = 0.5f)
                                count < 7 -> SoftGreen
                                else -> Color(0xFF4CAF50)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(32.dp).background(boxColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    if (count > 0) Text(count.toString(), fontSize = MaterialTheme.typography.labelSmall.fontSize, color = TextDark, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = when(i) { 0 -> "Сб"; 1 -> "Пт"; 2 -> "Чт"; 3 -> "Ср"; 4 -> "Вт"; 5 -> "Пн"; else -> "Вс" }, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}