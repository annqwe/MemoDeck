package com.example.memodeck.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.memodeck.MemoDeckApp
import com.example.memodeck.data.AnkiRepository
import com.example.memodeck.data.Card
import com.example.memodeck.data.Deck
import com.example.memodeck.data.ReviewQuality
import com.example.memodeck.data.Scheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnkiRepository((application as MemoDeckApp).database)

    val decks = repository.allDecks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _dueCards = MutableStateFlow<List<Card>>(emptyList())
    val dueCards: StateFlow<List<Card>> = _dueCards

    private val _allCardsForQuiz = MutableStateFlow<List<Card>>(emptyList())
    val allCardsForQuiz: StateFlow<List<Card>> = _allCardsForQuiz

    val reviewedCards = repository.getAllReviewedCards().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addDeck(name: String) { if (name.isNotBlank()) viewModelScope.launch { repository.insertDeck(name) } }
    fun deleteDeck(deck: Deck) { viewModelScope.launch { repository.deleteDeck(deck) } }
    fun addCard(deckId: Long, front: String, back: String) {
        if (front.isNotBlank() && back.isNotBlank()) viewModelScope.launch { repository.insertCard(Card(deckId = deckId, front = front, back = back)) }
    }
    fun deleteCard(card: Card) { viewModelScope.launch { repository.deleteCard(card) } }
    fun getDeckById(id: Long): Deck? = decks.value.find { it.id == id }
    fun getCardsForDeck(deckId: Long) = repository.getCardsForDeck(deckId)
    fun getCardCount(deckId: Long) = repository.getCardCount(deckId)
    fun getDueCount(deckId: Long) = repository.getDueCount(deckId)

    // Загрузка для обычного повторения (только те, что на сегодня)
    fun loadDueCards(deckId: Long) {
        viewModelScope.launch {
            _dueCards.value = repository.getDueCards(deckId)
            _allCardsForQuiz.value = repository.getAllCardsForOptions(deckId)
        }
    }

    // НОВОЕ: Загрузка для тренировки (ВСЕ карточки колоды)
    fun loadTrainingCards(deckId: Long) {
        viewModelScope.launch {
            val allCards = repository.getAllCardsForOptions(deckId)
            _dueCards.value = allCards // Кидаем все карточки в список для повторения
            _allCardsForQuiz.value = allCards
        }
    }

    fun reviewCard(card: Card, quality: ReviewQuality) {
        viewModelScope.launch {
            val updatedCard = Scheduler.schedule(card, quality)
            repository.updateCard(updatedCard)
        }
    }
}