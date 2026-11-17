package com.example.memodeck.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<Deck>>
    @Insert suspend fun insertDeck(deck: Deck): Long
    @Delete suspend fun deleteDeck(deck: Deck)
    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Long): Deck?
}

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY dueDate ASC")
    fun getCardsByDeck(deckId: Long): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun getCardCount(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND dueDate <= :now")
    fun getDueCount(deckId: Long, now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND dueDate <= :now ORDER BY dueDate ASC")
    suspend fun getDueCards(deckId: Long, now: Long = System.currentTimeMillis()): List<Card>

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    suspend fun getAllCardsForOptions(deckId: Long): List<Card>

    // НОВОЕ: Получить все карточки, которые повторяли (для статистики)
    @Query("SELECT * FROM cards WHERE lastReviewed IS NOT NULL")
    fun getAllReviewedCards(): Flow<List<Card>>

    @Insert suspend fun insertCard(card: Card): Long
    @Update suspend fun updateCard(card: Card)
    @Delete suspend fun deleteCard(card: Card)
}