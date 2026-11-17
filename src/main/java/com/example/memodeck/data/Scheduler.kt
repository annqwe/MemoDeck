package com.example.memodeck.data

import kotlin.math.roundToInt

enum class ReviewQuality(val label: String) {
    AGAIN("Снова"),
    HARD("Трудно"),
    GOOD("Хорошо"),
    EASY("Легко")
}

object Scheduler {
    private const val DAY_MS = 24L * 60 * 60 * 1000

    fun schedule(card: Card, quality: ReviewQuality, now: Long = System.currentTimeMillis()): Card {
        val q = when (quality) {
            ReviewQuality.AGAIN -> 0
            ReviewQuality.HARD -> 3
            ReviewQuality.GOOD -> 4
            ReviewQuality.EASY -> 5
        }

        var ef = card.easeFactor
        var reps = card.repetitions
        var interval = card.interval

        if (q < 3) {
            reps = 0
            interval = 0
        } else {
            reps += 1
            interval = when (reps) {
                1 -> 1
                2 -> 6
                else -> (interval * ef).roundToInt()
            }
            ef = ef + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
            if (ef < 1.3) ef = 1.3
        }

        val due = if (q < 3) now + 10 * 60 * 1000L else now + interval * DAY_MS

        return card.copy(
            easeFactor = ef,
            interval = interval,
            repetitions = reps,
            dueDate = due,
            lastReviewed = now
        )
    }
}