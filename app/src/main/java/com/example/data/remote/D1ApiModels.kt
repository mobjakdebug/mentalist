package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val email: String? = null,
    val password: String,
    val avatarEmoji: String = "🕵️"
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    val id: String = "",
    val username: String = "",
    val email: String? = null,
    val avatarEmoji: String = "🕵️",
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 0,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val successfulBluffs: Int = 0,
    val correctGuesses: Int = 0,
    val ghostBadges: Int = 0,
    val truthseekerBadges: Int = 0
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val success: Boolean = false,
    val user: UserProfileDto? = null,
    val token: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String = "",
    val service: String = "",
    val database: String = "",
    val questionCount: Int = 0,
    val activeRoomsCount: Int = 0,
    val timestamp: Long = 0L
)

@JsonClass(generateAdapter = true)
data class D1Question(
    val id: Int,
    val category: String,
    val questionText: String,
    val createdBy: String? = null
)

@JsonClass(generateAdapter = true)
data class QuestionsResponse(
    val questions: List<D1Question> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CreateQuestionRequest(
    val category: String,
    val questionText: String,
    val createdBy: String = "USER"
)

@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    val playerName: String,
    val avatarEmoji: String = "🕵️",
    val level: Int = 1,
    val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateRoomResponse(
    val success: Boolean = false,
    val roomCode: String = "",
    val roomId: String = "",
    val playerId: String = "",
    val isHost: Boolean = false,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class JoinRoomRequest(
    val roomCode: String,
    val playerName: String,
    val avatarEmoji: String = "🕵️",
    val level: Int = 1,
    val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class JoinRoomResponse(
    val success: Boolean = false,
    val roomCode: String = "",
    val roomId: String = "",
    val playerId: String = "",
    val isHost: Boolean = false,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class D1RoomSummary(
    val id: String = "",
    val code: String = "",
    val status: String = "LOBBY",
    val currentRound: Int = 1,
    val createdAt: Long = 0L,
    val playerCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class PublicRoomsResponse(
    val rooms: List<D1RoomSummary> = emptyList()
)

@JsonClass(generateAdapter = true)
data class D1RoomInfo(
    val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val status: String = "LOBBY",
    val currentRound: Int = 1,
    val totalRounds: Int = 4,
    val currentMentalistId: String? = null,
    val phaseStartTime: Long? = null,
    val phaseDurationSeconds: Int = 45
)

@JsonClass(generateAdapter = true)
data class D1PlayerDto(
    val id: String,
    val name: String,
    val avatarEmoji: String = "🕵️",
    val level: Int = 1,
    val score: Int = 0,
    val roundDeltaXp: Int = 0,
    val isHost: Int = 0,
    val isMentalist: Int = 0,
    val hasAnswered: Int = 0,
    val lastPing: Long = 0L
)

@JsonClass(generateAdapter = true)
data class RoomStateResponse(
    val room: D1RoomInfo? = null,
    val players: List<D1PlayerDto> = emptyList(),
    val currentQuestion: D1Question? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class GenericSuccessResponse(
    val success: Boolean = false,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SubmitAnswerRequest(
    val playerId: String,
    val answerText: String
)

@JsonClass(generateAdapter = true)
data class SubmitAnswerResponse(
    val success: Boolean = false,
    val allAnswered: Boolean = false,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class D1CardDto(
    val id: String,
    val label: String,
    val text: String,
    val assignedPlayerId: String? = null,
    val isRevealed: Boolean = false,
    val authorPlayerId: String? = null
)

@JsonClass(generateAdapter = true)
data class CardsResponse(
    val cards: List<D1CardDto> = emptyList(),
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val playerId: String,
    val playerName: String,
    val text: String,
    val isMentalist: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SendMessageResponse(
    val success: Boolean = false,
    val messageId: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class D1MessageDto(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isFromMentalist: Int = 0,
    val isSystem: Int = 0
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val messages: List<D1MessageDto> = emptyList(),
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AdvancePhaseRequest(
    val newPhase: String,
    val durationSeconds: Int? = null,
    val playerId: String? = null
)

@JsonClass(generateAdapter = true)
data class GuessAssignment(
    val cardId: String,
    val assignedPlayerId: String
)

@JsonClass(generateAdapter = true)
data class SubmitGuessRequest(
    val assignments: List<GuessAssignment>,
    val playerId: String? = null
)

@JsonClass(generateAdapter = true)
data class D1RevealItemDto(
    val cardId: String,
    val cardLabel: String,
    val text: String,
    val authorPlayerId: String,
    val authorName: String,
    val assignedPlayerId: String?,
    val assignedName: String,
    val isCorrect: Boolean,
    val mentalistDelta: Int,
    val authorDelta: Int
)

@JsonClass(generateAdapter = true)
data class RevealResponse(
    val success: Boolean = false,
    val results: List<D1RevealItemDto> = emptyList(),
    val mentalistTotalDelta: Int = 0,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class HeartbeatRequest(
    val playerId: String
)

@JsonClass(generateAdapter = true)
data class ReportRequest(
    val roomCode: String? = null,
    val reporterName: String,
    val targetName: String,
    val messageText: String,
    val reason: String
)
