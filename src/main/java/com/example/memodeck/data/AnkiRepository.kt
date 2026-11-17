package com.example.memodeck.data

import kotlinx.coroutines.flow.Flow

class AnkiRepository(private val db: AppDatabase) {
    val allDecks: Flow<List<Deck>> = db.deckDao().getAllDecks()
    suspend fun insertDeck(name: String) { db.deckDao().insertDeck(Deck(name = name)) }
    suspend fun deleteDeck(deck: Deck) { db.deckDao().deleteDeck(deck) }
    suspend fun insertCard(card: Card) { db.cardDao().insertCard(card) }
    suspend fun deleteCard(card: Card) { db.cardDao().deleteCard(card) }
    fun getCardsForDeck(deckId: Long): Flow<List<Card>> = db.cardDao().getCardsByDeck(deckId)
    fun getCardCount(deckId: Long): Flow<Int> = db.cardDao().getCardCount(deckId)
    fun getDueCount(deckId: Long): Flow<Int> = db.cardDao().getDueCount(deckId)
    suspend fun getDueCards(deckId: Long): List<Card> = db.cardDao().getDueCards(deckId)
    suspend fun updateCard(card: Card) { db.cardDao().updateCard(card) }
    suspend fun getAllCardsForOptions(deckId: Long): List<Card> = db.cardDao().getAllCardsForOptions(deckId)

    // НОВОЕ
    fun getAllReviewedCards(): Flow<List<Card>> = db.cardDao().getAllReviewedCards()
}