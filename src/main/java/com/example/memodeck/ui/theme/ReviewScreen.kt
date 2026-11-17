package com.example.memodeck.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memodeck.data.ReviewQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    deckId: Long,
    viewModel: DeckViewModel,
    isTraining: Boolean = false,
    onBack: () -> Unit
) {
    val cards by viewModel.dueCards.collectAsStateWithLifecycle()
    val allCards by viewModel.allCardsForQuiz.collectAsStateWithLifecycle()

    var currentIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    var isQuizMode by remember { mutableStateOf(isTraining) }

    var quizOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val rotation by animateFloatAsState(
        targetValue = if (showAnswer) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "CardFlip"
    )

    LaunchedEffect(deckId, isTraining) {
        if (isTraining) {
            viewModel.loadTrainingCards(deckId)
        } else {
            viewModel.loadDueCards(deckId)
        }
    }

    LaunchedEffect(currentIndex, isQuizMode, cards, allCards) {
        if (isQuizMode && currentIndex < cards.size) {
            val correctCard = cards[currentIndex]
            val otherOptions = allCards.filter { it.id != correctCard.id }.map { it.back }.shuffled().take(3)
            quizOptions = (otherOptions + correctCard.back).shuffled()
            selectedOption = null
            showAnswer = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isTraining) "Тренировка" else if (isQuizMode) "Тест" else "Повторение", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } },
                actions = {
                    // ТАЙМЕР ПОМОДОРО
                    PomodoroTimer()

                    // Кнопка переключения режима
                    if (!isTraining) {
                        IconButton(onClick = { isQuizMode = !isQuizMode; showAnswer = false }) {
                            Icon(if (isQuizMode) Icons.Default.Visibility else Icons.Default.Quiz, contentDescription = "Сменить режим", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (cards.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Нет карточек для повторения!", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack, shape = RoundedCornerShape(16.dp)) { Text("Вернуться назад") }
                }
            }
        } else if (currentIndex < cards.size) {
            val card = cards[currentIndex]

            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        if (rotation <= 90f) {
                            Text(text = card.front, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = card.back,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.graphicsLayer { rotationY = 180f }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isQuizMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        quizOptions.forEach { option ->
                            val isSelected = selectedOption == option
                            val isCorrect = option == card.back
                            val buttonColor = when {
                                selectedOption == null -> MaterialTheme.colorScheme.surface
                                isSelected && isCorrect -> Color(0xFF81C784)
                                isSelected && !isCorrect -> Color(0xFFE57373)
                                !isSelected && isCorrect && selectedOption != null -> Color(0xFF81C784).copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(enabled = selectedOption == null) {
                                    selectedOption = option
                                    viewModel.reviewCard(card, if (isCorrect) ReviewQuality.GOOD else ReviewQuality.AGAIN)
                                },
                                colors = CardDefaults.cardColors(containerColor = buttonColor),
                                elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 2.dp)
                            ) {
                                Text(text = option, modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    if (selectedOption != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAnswer = false; currentIndex++ }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Следующая") }
                    }
                } else {
                    if (!showAnswer) {
                        Button(onClick = { showAnswer = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                            Text("Показать ответ", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.reviewCard(card, ReviewQuality.AGAIN); showAnswer = false; currentIndex++ }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Text("Снова", fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                            Button(onClick = { viewModel.reviewCard(card, ReviewQuality.HARD); showAnswer = false; currentIndex++ }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Text("Трудно", fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                            Button(onClick = { viewModel.reviewCard(card, ReviewQuality.GOOD); showAnswer = false; currentIndex++ }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Text("Хорошо", fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                            Button(onClick = { viewModel.reviewCard(card, ReviewQuality.EASY); showAnswer = false; currentIndex++ }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Text("Легко", fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Все карточки пройдены! 🎉", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { currentIndex = 0; showAnswer = false; selectedOption = null },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Пройти еще раз") }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Вернуться назад") }
                }
            }
        }
    }
}