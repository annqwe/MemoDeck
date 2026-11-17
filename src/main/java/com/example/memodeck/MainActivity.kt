package com.example.memodeck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memodeck.data.Deck
import com.example.memodeck.data.ThemeManager
import com.example.memodeck.ui.theme.*
import kotlinx.coroutines.launch // ВОТ ЭТОТ ИМПОРТ БЫЛ ПРОПУЩЕН

class MainActivity : ComponentActivity() {
    private val viewModel: DeckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager(context) }
            val isDark by themeManager.isDarkTheme.collectAsStateWithLifecycle(initialValue = false)
            val scope = rememberCoroutineScope() // Объявляем scope

            MemoDeckTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAppScreen(viewModel, isDark) {
                        // Теперь launch будет работать
                        scope.launch { themeManager.toggleTheme(!isDark) }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: DeckViewModel, isDark: Boolean, onToggleTheme: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Колоды") },
                    label = { Text("Колоды") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                    label = { Text("Профиль") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) {
                NavHost(navController = navController, startDestination = "main_list") {
                    composable("main_list") {
                        DeckListScreen(viewModel, onDeckClick = { deckId -> navController.navigate("deck/$deckId") })
                    }
                    composable("deck/{deckId}") { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
                        DeckDetailScreen(deckId, viewModel, onBack = { navController.popBackStack() }, onAddCardClick = { navController.navigate("add_card/$deckId") }, onReviewClick = { navController.navigate("review/$deckId") }, onTrainingClick = { navController.navigate("training/$deckId") })
                    }
                    composable("add_card/{deckId}") { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
                        AddCardScreen(deckId, viewModel, onBack = { navController.popBackStack() })
                    }
                    composable("review/{deckId}") { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
                        ReviewScreen(deckId, viewModel, isTraining = false, onBack = { navController.popBackStack() })
                    }
                    composable("training/{deckId}") { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
                        ReviewScreen(deckId, viewModel, isTraining = true, onBack = { navController.popBackStack() })
                    }
                }
            } else {
                ProfileScreen(viewModel, isDark, onToggleTheme)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeckListScreen(viewModel: DeckViewModel, onDeckClick: (Long) -> Unit) {
    val decks by viewModel.decks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var deckName by remember { mutableStateOf("") }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("MemoDeck", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground) }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)) },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White, shape = RoundedCornerShape(20.dp)) { Icon(Icons.Default.Add, contentDescription = "Добавить") } },
        containerColor = Color.Transparent
    ) { padding ->
        if (decks.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("У вас пока нет колод.\nНажмите +, чтобы создать первую!", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(decks) { deck ->
                    val totalCards by viewModel.getCardCount(deck.id).collectAsStateWithLifecycle(initialValue = 0)
                    val dueCards by viewModel.getDueCount(deck.id).collectAsStateWithLifecycle(initialValue = 0)
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = PeachPrimary, spotColor = PeachPrimary).clip(RoundedCornerShape(24.dp)).combinedClickable(onClick = { onDeckClick(deck.id) }, onLongClick = { deckToDelete = deck }),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(52.dp).background(SoftBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(28.dp)) }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = deck.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = "К повторению: $dueCards  •  Всего: $totalCards", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PeachPrimary)
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false }, title = { Text("Новая колода", fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = deckName, onValueChange = { deckName = it }, label = { Text("Название") }, shape = RoundedCornerShape(16.dp)) }, confirmButton = { Button(onClick = { viewModel.addDeck(deckName); deckName = ""; showDialog = false }, shape = RoundedCornerShape(12.dp)) { Text("Создать") } }, dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Отмена") } }, shape = RoundedCornerShape(24.dp), containerColor = MaterialTheme.colorScheme.surface)
    }
    if (deckToDelete != null) {
        AlertDialog(onDismissRequest = { deckToDelete = null }, title = { Text("Удалить колоду?", fontWeight = FontWeight.Bold) }, text = { Text("Колода «${deckToDelete?.name}» и все её карточки будут удалены.") }, confirmButton = { Button(onClick = { viewModel.deleteDeck(deckToDelete!!); deckToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) { Text("Удалить") } }, dismissButton = { TextButton(onClick = { deckToDelete = null }) { Text("Отмена") } }, shape = RoundedCornerShape(24.dp), containerColor = MaterialTheme.colorScheme.surface)
    }
}