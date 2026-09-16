package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.QuestionBank
import com.example.data.local.UserProfileEntity
import com.example.data.remote.*
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GameUiState(
    val currentPhase: GamePhase = GamePhase.AUTH,
    val userProfile: UserProfileEntity? = null,
    val isLoggedIn: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val dailyQuests: List<DailyQuest> = emptyList(),
    val badges: List<GameBadge> = emptyList(),

    // Cloudflare D1 state
    val d1ConnectionState: D1ConnectionState = D1ConnectionState.Idle,
    val workerUrl: String = "",
    val showD1ConfigDialog: Boolean = false,
    val showD1GuideDialog: Boolean = false,
    val publicRooms: List<D1RoomSummary> = emptyList(),
    val isRefreshingRooms: Boolean = false,

    // Active Room state
    val roomCode: String = "",
    val roomId: String = "",
    val localPlayerId: String = "",
    val isHost: Boolean = false,
    val showCreateRoomDialog: Boolean = false,
    val showJoinRoomDialog: Boolean = false,
    val roomCodeInput: String = "",
    val joinErrorMessage: String? = null,
    val isJoiningOrCreating: Boolean = false,

    // Match state
    val players: List<Player> = emptyList(),
    val currentRound: Int = 1,
    val totalRounds: Int = 4,
    val currentMentalistId: String = "",
    val currentQuestion: QuestionItem = QuestionBank.defaultQuestions[0],
    val timerSecondsRemaining: Int = 45,
    val isTimerRunning: Boolean = false,

    // Answering
    val localUserAnswer: String = "",
    val hasLocalUserSubmitted: Boolean = false,

    // Anonymous Cards
    val anonymousCards: List<CardAnswer> = emptyList(),

    // Interrogation & Chat
    val chatMessages: List<ChatMessage> = emptyList(),
    val userChatInput: String = "",
    val afkWarningActive: Boolean = false,
    val secondsSinceLastUserAction: Int = 0,

    // Guessing
    val selectedCardForAssignment: CardAnswer? = null,

    // Reveal
    val revealResults: List<RoundRevealResult> = emptyList(),
    val revealStepIndex: Int = -1,
    val isRevealingDone: Boolean = false,

    // Report
    val reportingTarget: Pair<String, String>? = null,
    val reportDialogVisible: Boolean = false,
    val reportSuccessSnackbar: String? = null,

    // Dialogs
    val showRulesDialog: Boolean = false,
    val showQuestsDialog: Boolean = false,
    val showQuestionBankDialog: Boolean = false,
    val availableQuestions: List<QuestionItem> = QuestionBank.defaultQuestions
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    private val _uiState = MutableStateFlow(GameUiState(workerUrl = repository.getWorkerUrl()))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var roomSyncJob: Job? = null
    private var timerJob: Job? = null

    init {
        // Collect user profile
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                _uiState.update { current ->
                    if (profile != null) {
                        current.copy(
                            userProfile = profile,
                            badges = repository.getBadges(profile),
                            isLoggedIn = true,
                            currentPhase = if (current.currentPhase == GamePhase.AUTH) GamePhase.HOME else current.currentPhase
                        )
                    } else {
                        current.copy(
                            userProfile = null,
                            badges = emptyList(),
                            isLoggedIn = false,
                            currentPhase = GamePhase.AUTH
                        )
                    }
                }
            }
        }

        // Collect daily quests
        viewModelScope.launch {
            repository.dailyQuests.collect { quests ->
                _uiState.update { it.copy(dailyQuests = quests) }
            }
        }

        // Collect D1 connection state
        viewModelScope.launch {
            repository.d1ConnectionState.collect { state ->
                _uiState.update { it.copy(d1ConnectionState = state) }
            }
        }

        // Test D1 connection and load initial questions & public rooms
        refreshD1Data()
    }

    fun refreshD1Data() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingRooms = true) }
            repository.testD1Connection()
            val questions = repository.fetchQuestions()
            val rooms = repository.getPublicRooms()
            _uiState.update {
                it.copy(
                    availableQuestions = questions,
                    publicRooms = rooms,
                    isRefreshingRooms = false
                )
            }
        }
    }

    // --- CLOUDFLARE D1 SETTINGS ---

    fun openD1ConfigDialog() = _uiState.update { it.copy(showD1ConfigDialog = true) }
    fun closeD1ConfigDialog() = _uiState.update { it.copy(showD1ConfigDialog = false) }

    fun openD1GuideDialog() = _uiState.update { it.copy(showD1GuideDialog = true) }
    fun closeD1GuideDialog() = _uiState.update { it.copy(showD1GuideDialog = false) }

    fun saveWorkerUrl(url: String) {
        repository.setWorkerUrl(url)
        _uiState.update { it.copy(workerUrl = repository.getWorkerUrl()) }
        refreshD1Data()
    }

    // --- AUTHENTICATION FLOW (LOGIN & REGISTRATION) ---

    fun register(username: String, email: String?, password: String, avatar: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val result = repository.registerUser(username, email, password, avatar)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            userProfile = user,
                            badges = repository.getBadges(user),
                            isLoggedIn = true,
                            currentPhase = GamePhase.HOME,
                            authErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = error.localizedMessage ?: "خطا در ثبت‌نام"
                        )
                    }
                }
            )
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val result = repository.loginUser(username, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            userProfile = user,
                            badges = repository.getBadges(user),
                            isLoggedIn = true,
                            currentPhase = GamePhase.HOME,
                            authErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = error.localizedMessage ?: "نام کاربری یا رمز عبور اشتباه است"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    userProfile = null,
                    badges = emptyList(),
                    currentPhase = GamePhase.AUTH,
                    authErrorMessage = null
                )
            }
        }
    }

    // --- NAVIGATION & DIALOGS ---

    fun openRulesDialog() = _uiState.update { it.copy(showRulesDialog = true) }
    fun closeRulesDialog() = _uiState.update { it.copy(showRulesDialog = false) }

    fun openQuestsDialog() = _uiState.update { it.copy(showQuestsDialog = true) }
    fun closeQuestsDialog() = _uiState.update { it.copy(showQuestsDialog = false) }

    fun openQuestionBankDialog() = _uiState.update { it.copy(showQuestionBankDialog = true) }
    fun closeQuestionBankDialog() = _uiState.update { it.copy(showQuestionBankDialog = false) }

    fun dismissSnackbar() = _uiState.update { it.copy(reportSuccessSnackbar = null) }

    fun openCreateRoomDialog() = _uiState.update { it.copy(showCreateRoomDialog = true) }
    fun closeCreateRoomDialog() = _uiState.update { it.copy(showCreateRoomDialog = false) }

    fun openJoinRoomDialog(prefillCode: String = "") {
        _uiState.update {
            it.copy(
                showJoinRoomDialog = true,
                roomCodeInput = prefillCode,
                joinErrorMessage = null
            )
        }
    }
    fun closeJoinRoomDialog() = _uiState.update { it.copy(showJoinRoomDialog = false, joinErrorMessage = null) }

    fun updateRoomCodeInput(input: String) {
        _uiState.update { it.copy(roomCodeInput = input.uppercase().trim()) }
    }

    // --- REAL CLOUDFLARE D1 MULTIPLAYER ROOMS ---

    fun createRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoiningOrCreating = true, showCreateRoomDialog = false) }
            val profile = _uiState.value.userProfile
            val res = repository.createD1Room(
                playerName = profile?.username ?: "کاربر",
                avatar = profile?.avatarEmoji ?: "🕵️",
                level = profile?.level ?: 1,
                userId = profile?.id
            )

            if (res != null && res.success) {
                repository.d1Manager.setActivePlayerId(res.playerId)
                _uiState.update {
                    it.copy(
                        roomCode = res.roomCode,
                        roomId = res.roomId,
                        localPlayerId = res.playerId,
                        isHost = true,
                        currentPhase = GamePhase.LOBBY,
                        isJoiningOrCreating = false
                    )
                }
                startRoomSync(res.roomCode)
            } else {
                _uiState.update {
                    it.copy(
                        isJoiningOrCreating = false,
                        reportSuccessSnackbar = res?.error ?: "خطا در ایجاد اتاق روی Cloudflare D1"
                    )
                }
            }
        }
    }

    fun joinRoom(code: String) {
        val cleanCode = code.uppercase().trim()
        if (cleanCode.isBlank()) {
            _uiState.update { it.copy(joinErrorMessage = "لطفاً کد اتاق را وارد کنید") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isJoiningOrCreating = true, joinErrorMessage = null) }
            val profile = _uiState.value.userProfile
            val res = repository.joinD1Room(
                roomCode = cleanCode,
                playerName = profile?.username ?: "کاربر",
                avatar = profile?.avatarEmoji ?: "🕵️",
                level = profile?.level ?: 1,
                userId = profile?.id
            )

            if (res != null && res.success) {
                repository.d1Manager.setActivePlayerId(res.playerId)
                _uiState.update {
                    it.copy(
                        roomCode = res.roomCode,
                        roomId = res.roomId,
                        localPlayerId = res.playerId,
                        isHost = res.isHost,
                        currentPhase = GamePhase.LOBBY,
                        showJoinRoomDialog = false,
                        isJoiningOrCreating = false
                    )
                }
                startRoomSync(res.roomCode)
            } else {
                _uiState.update {
                    it.copy(
                        isJoiningOrCreating = false,
                        joinErrorMessage = res?.error ?: "اتاقی با این کد در D1 یافت نشد یا شروع شده است"
                    )
                }
            }
        }
    }

    fun leaveRoom() {
        stopRoomSync()
        stopTimer()
        repository.d1Manager.setActivePlayerId(null)
        _uiState.update {
            it.copy(
                currentPhase = GamePhase.HOME,
                roomCode = "",
                roomId = "",
                localPlayerId = "",
                isHost = false,
                players = emptyList(),
                chatMessages = emptyList(),
                anonymousCards = emptyList(),
                revealResults = emptyList()
            )
        }
        refreshD1Data()
    }

    // --- REAL-TIME ROOM POLLING SYNC ---

    private fun startRoomSync(roomCode: String) {
        stopRoomSync()
        roomSyncJob = viewModelScope.launch {
            while (true) {
                try {
                    syncRoomState(roomCode)
                } catch (_: Exception) {}
                delay(1500)
            }
        }
    }

    private fun stopRoomSync() {
        roomSyncJob?.cancel()
        roomSyncJob = null
    }

    private suspend fun syncRoomState(roomCode: String) {
        val localPlayerId = _uiState.value.localPlayerId
        // Heartbeat to keep player marked online
        repository.sendD1Heartbeat(roomCode, localPlayerId)

        val stateResponse = repository.getD1RoomState(roomCode) ?: return
        val room = stateResponse.room ?: return
        val d1Players = stateResponse.players
        val now = System.currentTimeMillis()

        // Map D1 players to app Player model
        val mappedPlayers = d1Players.map { p ->
            Player(
                id = p.id,
                name = p.name,
                avatarEmoji = p.avatarEmoji,
                level = p.level,
                rankTier = RankTier.fromLevel(p.level),
                isLocalUser = (p.id == localPlayerId),
                isMentalist = (p.isMentalist == 1),
                isHost = (p.isHost == 1),
                isOnline = (now - p.lastPing < 15000),
                matchScore = p.score,
                roundDeltaXp = p.roundDeltaXp,
                hasAnswered = (p.hasAnswered == 1)
            )
        }

        val serverPhase = when (room.status) {
            "LOBBY" -> GamePhase.LOBBY
            "ANSWERING" -> GamePhase.ANSWERING
            "SHUFFLING" -> GamePhase.SHUFFLING
            "INTERROGATION" -> GamePhase.INTERROGATION
            "GUESSING" -> GamePhase.GUESSING
            "REVEAL" -> GamePhase.REVEAL
            "SUMMARY" -> GamePhase.ROUND_SUMMARY
            "FINISHED" -> GamePhase.GAME_OVER
            else -> _uiState.value.currentPhase
        }

        val serverQuestion = stateResponse.currentQuestion?.let {
            QuestionItem(id = it.id, category = it.category, questionText = it.questionText)
        } ?: _uiState.value.currentQuestion

        // Calculate timer remaining from server phaseStartTime
        val remainingSeconds = if (room.phaseStartTime != null && room.phaseDurationSeconds > 0) {
            val elapsed = ((now - room.phaseStartTime) / 1000).toInt()
            (room.phaseDurationSeconds - elapsed).coerceAtLeast(0)
        } else {
            _uiState.value.timerSecondsRemaining
        }

        _uiState.update { current ->
            current.copy(
                players = mappedPlayers,
                currentMentalistId = room.currentMentalistId ?: "",
                currentRound = room.currentRound,
                totalRounds = room.totalRounds,
                currentQuestion = serverQuestion,
                currentPhase = serverPhase,
                timerSecondsRemaining = remainingSeconds
            )
        }

        // If in Interrogation, sync real chat messages from D1
        if (serverPhase == GamePhase.INTERROGATION) {
            val d1Msgs = repository.getD1Chat(roomCode)
            val mappedChat = d1Msgs.map { m ->
                ChatMessage(
                    id = m.id,
                    senderId = m.senderId,
                    senderName = m.senderName,
                    text = m.text,
                    timestamp = "چند لحظه پیش",
                    isFromMentalist = (m.isFromMentalist == 1),
                    isSystem = (m.isSystem == 1)
                )
            }
            _uiState.update { it.copy(chatMessages = mappedChat) }
        }

        // If in Interrogation, Guessing, or Reveal, sync cards from D1
        if (serverPhase == GamePhase.INTERROGATION || serverPhase == GamePhase.GUESSING || serverPhase == GamePhase.REVEAL) {
            val d1Cards = repository.getD1Cards(roomCode)
            val mappedCards = d1Cards.map { c ->
                CardAnswer(
                    id = c.id,
                    label = c.label,
                    authorPlayerId = c.authorPlayerId ?: "HIDDEN",
                    text = c.text,
                    assignedPlayerId = c.assignedPlayerId,
                    isRevealed = c.isRevealed
                )
            }
            _uiState.update { it.copy(anonymousCards = mappedCards) }
        }

        // Auto transition if host and timer expired
        if (_uiState.value.isHost && remainingSeconds <= 0) {
            handleHostPhaseTimeout(serverPhase, roomCode)
        }
    }

    private suspend fun handleHostPhaseTimeout(phase: GamePhase, roomCode: String) {
        when (phase) {
            GamePhase.ANSWERING -> {
                repository.d1Manager.getApi()?.advancePhase(roomCode, AdvancePhaseRequest("INTERROGATION", 150))
            }
            GamePhase.INTERROGATION -> {
                repository.d1Manager.getApi()?.advancePhase(roomCode, AdvancePhaseRequest("GUESSING", 30))
            }
            else -> {}
        }
    }

    // --- GAME ACTIONS ---

    fun startMatch() {
        val code = _uiState.value.roomCode
        if (code.isBlank() || !_uiState.value.isHost) return
        viewModelScope.launch {
            val success = repository.startD1Match(code)
            if (success) {
                _uiState.update { it.copy(currentPhase = GamePhase.ANSWERING) }
                syncRoomState(code)
            } else {
                _uiState.update { it.copy(reportSuccessSnackbar = "خطا در شروع مسابقه. حداقل ۲ بازیکن نیاز است.") }
            }
        }
    }

    fun updateUserAnswerText(text: String) {
        _uiState.update { it.copy(localUserAnswer = text) }
    }

    fun submitAnswer() {
        val code = _uiState.value.roomCode
        val playerId = _uiState.value.localPlayerId
        val text = _uiState.value.localUserAnswer.trim().ifEmpty { "مظنون از پاسخ صریح طفره رفت!" }

        if (code.isBlank() || playerId.isBlank()) return

        viewModelScope.launch {
            val res = repository.submitD1Answer(code, playerId, text)
            if (res != null && res.success) {
                _uiState.update { it.copy(hasLocalUserSubmitted = true) }
                syncRoomState(code)
            }
        }
    }

    fun updateUserChatInput(input: String) {
        _uiState.update { it.copy(userChatInput = input) }
    }

    fun sendUserChatMessage() {
        val text = _uiState.value.userChatInput.trim()
        if (text.isBlank()) return

        val code = _uiState.value.roomCode
        val localPlayer = _uiState.value.players.find { it.isLocalUser } ?: return

        _uiState.update { it.copy(userChatInput = "", secondsSinceLastUserAction = 0, afkWarningActive = false) }

        viewModelScope.launch {
            repository.sendD1ChatMessage(
                roomCode = code,
                playerId = localPlayer.id,
                playerName = localPlayer.name,
                text = text,
                isMentalist = localPlayer.isMentalist
            )
            // Immediately refresh chat
            val d1Msgs = repository.getD1Chat(code)
            val mappedChat = d1Msgs.map { m ->
                ChatMessage(
                    id = m.id,
                    senderId = m.senderId,
                    senderName = m.senderName,
                    text = m.text,
                    timestamp = "الان",
                    isFromMentalist = (m.isFromMentalist == 1),
                    isSystem = (m.isSystem == 1)
                )
            }
            _uiState.update { it.copy(chatMessages = mappedChat) }
        }
    }

    fun sendQuickProbe(text: String) {
        _uiState.update { it.copy(userChatInput = text) }
        sendUserChatMessage()
    }

    fun advanceToGuessingPhase() {
        val code = _uiState.value.roomCode
        if (code.isBlank()) return
        viewModelScope.launch {
            repository.d1Manager.getApi()?.advancePhase(code, AdvancePhaseRequest("GUESSING", 30))
            syncRoomState(code)
        }
    }

    // --- GUESSING & REVEAL ---

    fun selectCardForAssignment(card: CardAnswer) {
        _uiState.update {
            it.copy(selectedCardForAssignment = if (it.selectedCardForAssignment?.id == card.id) null else card)
        }
    }

    fun assignSelectedCardToPlayer(suspectPlayerId: String) {
        val card = _uiState.value.selectedCardForAssignment ?: return
        val updatedCards = _uiState.value.anonymousCards.map {
            if (it.id == card.id) it.copy(assignedPlayerId = suspectPlayerId) else it
        }
        _uiState.update {
            it.copy(
                anonymousCards = updatedCards,
                selectedCardForAssignment = null
            )
        }

        // Send to D1
        val code = _uiState.value.roomCode
        if (code.isNotBlank()) {
            viewModelScope.launch {
                val assignments = updatedCards.filter { it.assignedPlayerId != null }.map {
                    GuessAssignment(cardId = it.id, assignedPlayerId = it.assignedPlayerId!!)
                }
                repository.submitD1Guess(code, assignments)
            }
        }
    }

    fun submitMentalistGuessesAndReveal() {
        val code = _uiState.value.roomCode
        if (code.isBlank()) return

        viewModelScope.launch {
            val response = repository.revealD1Round(code)
            if (response != null && response.success) {
                val mappedResults = response.results.map { r ->
                    val authorPlayer = _uiState.value.players.find { it.id == r.authorPlayerId }
                        ?: Player(id = r.authorPlayerId, name = r.authorName, avatarEmoji = "🕵️")
                    val guessedPlayer = _uiState.value.players.find { it.id == r.assignedPlayerId }

                    RoundRevealResult(
                        card = CardAnswer(
                            id = r.cardId,
                            label = r.cardLabel,
                            authorPlayerId = r.authorPlayerId,
                            text = r.text,
                            assignedPlayerId = r.assignedPlayerId,
                            isRevealed = true,
                            isCorrectGuess = r.isCorrect
                        ),
                        authorPlayer = authorPlayer,
                        guessedPlayer = guessedPlayer,
                        isCorrect = r.isCorrect,
                        mentalistDelta = r.mentalistDelta,
                        authorDelta = r.authorDelta
                    )
                }

                _uiState.update {
                    it.copy(
                        currentPhase = GamePhase.REVEAL,
                        revealResults = mappedResults,
                        revealStepIndex = 0,
                        isRevealingDone = false
                    )
                }

                // Update local user stats and quests
                val localPlayer = _uiState.value.players.find { it.isLocalUser }
                if (localPlayer != null) {
                    val isMentalist = localPlayer.isMentalist
                    val correctCount = mappedResults.count { it.isCorrect }
                    val successfulBluffs = mappedResults.count { !it.isCorrect && it.authorPlayer.id == localPlayer.id }
                    val myDelta = if (isMentalist) response.mentalistTotalDelta else {
                        mappedResults.find { it.authorPlayer.id == localPlayer.id }?.authorDelta ?: 0
                    }

                    repository.recordMatchResult(
                        gainedXp = myDelta,
                        isWin = myDelta > 0,
                        successfulBluffs = successfulBluffs,
                        correctGuesses = if (isMentalist) correctCount else 0,
                        gotGhostBadge = (!isMentalist && successfulBluffs >= 1),
                        gotTruthseekerBadge = (isMentalist && correctCount == mappedResults.size && mappedResults.isNotEmpty())
                    )
                }

                syncRoomState(code)
            }
        }
    }

    fun nextRevealStep() {
        val currentStep = _uiState.value.revealStepIndex
        val totalCards = _uiState.value.revealResults.size
        if (currentStep < totalCards - 1) {
            _uiState.update { it.copy(revealStepIndex = currentStep + 1) }
        } else {
            _uiState.update { it.copy(isRevealingDone = true, currentPhase = GamePhase.ROUND_SUMMARY) }
        }
    }

    // --- REPORT SYSTEM ---

    fun openReportDialog(senderName: String, text: String) {
        _uiState.update {
            it.copy(
                reportingTarget = Pair(senderName, text),
                reportDialogVisible = true
            )
        }
    }

    fun closeReportDialog() {
        _uiState.update {
            it.copy(
                reportingTarget = null,
                reportDialogVisible = false
            )
        }
    }

    fun submitReport(reason: String) {
        val target = _uiState.value.reportingTarget ?: return
        val roomCode = _uiState.value.roomCode
        val reporterName = _uiState.value.userProfile?.username ?: "کاربر"

        viewModelScope.launch {
            repository.reportUser(target.first, target.second, reason)
            try {
                repository.d1Manager.getApi()?.submitReport(
                    ReportRequest(
                        roomCode = roomCode.ifBlank { null },
                        reporterName = reporterName,
                        targetName = target.first,
                        messageText = target.second,
                        reason = reason
                    )
                )
            } catch (_: Exception) {}

            _uiState.update {
                it.copy(
                    reportingTarget = null,
                    reportDialogVisible = false,
                    reportSuccessSnackbar = "گزارش تخلف با موفقیت در پایگاه داده D1 ثبت شد."
                )
            }
        }
    }

    fun claimQuest(quest: DailyQuest) {
        if (!quest.isComplete || quest.isClaimed) return
        viewModelScope.launch {
            repository.claimQuest(quest.id, quest.xpReward)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRoomSync()
        stopTimer()
    }
}
