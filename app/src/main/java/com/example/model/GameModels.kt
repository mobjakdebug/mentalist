package com.example.model

enum class RankTier(
    val titleFa: String,
    val titleEn: String,
    val minLevel: Int,
    val maxLevel: Int,
    val badgeIcon: String
) {
    PROFILER("تحلیل‌گر", "Profiler", 1, 10, "🔍"),
    TACTICIAN("استراتژیست", "Tactician", 11, 25, "♟️"),
    SHADOW("سایه", "Shadow", 26, 40, "👤"),
    ILLUSIONIST("ایلوژنیست", "Illusionist", 41, 60, "🎭"),
    MENTALIST("منتالیست", "Mentalist", 61, 999, "👁️");

    companion object {
        fun fromLevel(level: Int): RankTier = when {
            level >= 61 -> MENTALIST
            level >= 41 -> ILLUSIONIST
            level >= 26 -> SHADOW
            level >= 11 -> TACTICIAN
            else -> PROFILER
        }
    }
}

data class Player(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val level: Int = 1,
    val rankTier: RankTier = RankTier.fromLevel(level),
    val isLocalUser: Boolean = false,
    val isMentalist: Boolean = false,
    val isHost: Boolean = false,
    val isOnline: Boolean = true,
    val matchScore: Int = 0,
    val roundDeltaXp: Int = 0,
    val hasAnswered: Boolean = false,
    val currentAnswer: String = "",
    val isAfk: Boolean = false
)

data class CardAnswer(
    val id: String,
    val label: String, // e.g. "کارت الف", "کارت ب", "کارت ج"
    val authorPlayerId: String,
    val text: String,
    val assignedPlayerId: String? = null,
    val isRevealed: Boolean = false,
    val isCorrectGuess: Boolean? = null
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isFromMentalist: Boolean = false,
    val isSystem: Boolean = false,
    val isReported: Boolean = false
)

enum class GamePhase {
    AUTH,            // Sign in / Sign up screen
    HOME,
    MATCHMAKING,
    LOBBY,
    ANSWERING,       // 45s
    SHUFFLING,       // 4s
    INTERROGATION,   // 150s (2.5m)
    GUESSING,        // 30s
    REVEAL,          // Reveal step-by-step
    ROUND_SUMMARY,   // Round scoreboard
    GAME_OVER        // Podiums, XP gain, level up
}

data class QuestionItem(
    val id: Int,
    val category: String, // "احساسی", "مچ‌گیری", "اخلاقی", "روابط", "عادات روزمره"
    val questionText: String
)

data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val xpReward: Int,
    val isClaimed: Boolean = false
) {
    val isComplete: Boolean get() = currentProgress >= targetProgress
}

data class GameBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false
)

data class RoundRevealResult(
    val card: CardAnswer,
    val authorPlayer: Player,
    val guessedPlayer: Player?,
    val isCorrect: Boolean,
    val mentalistDelta: Int,
    val authorDelta: Int
)
