package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DailyQuestEntity
import com.example.data.local.ReportEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.*
import com.example.model.DailyQuest
import com.example.model.GameBadge
import com.example.model.QuestionItem
import com.example.model.RankTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val questDao = db.questDao()
    private val reportDao = db.reportDao()

    val d1Manager = CloudflareD1Manager(context)
    val d1ConnectionState = d1Manager.connectionState

    fun getWorkerUrl(): String = d1Manager.getWorkerUrl()
    fun setWorkerUrl(url: String) = d1Manager.setWorkerUrl(url)
    suspend fun testD1Connection() = d1Manager.testConnection()

    // --- CLOUDFLARE D1 NETWORK CALLS ---

    suspend fun fetchQuestions(): List<QuestionItem> {
        val api = d1Manager.getApi() ?: return QuestionBank.defaultQuestions
        return try {
            val response = api.getQuestions()
            if (response.questions.isNotEmpty()) {
                response.questions.map {
                    QuestionItem(id = it.id, category = it.category, questionText = it.questionText)
                }
            } else {
                QuestionBank.defaultQuestions
            }
        } catch (e: Exception) {
            QuestionBank.defaultQuestions
        }
    }

    suspend fun createD1Room(playerName: String, avatar: String, level: Int, userId: String? = null): CreateRoomResponse? {
        val api = d1Manager.getApi() ?: return null
        return try {
            val res = api.createRoom(CreateRoomRequest(playerName = playerName, avatarEmoji = avatar, level = level, userId = userId))
            if (res.success && res.playerId.isNotBlank()) {
                d1Manager.setActivePlayerId(res.playerId)
            }
            res
        } catch (e: Exception) {
            CreateRoomResponse(success = false, error = e.localizedMessage ?: "خطا در اتصال به D1")
        }
    }

    suspend fun joinD1Room(roomCode: String, playerName: String, avatar: String, level: Int, userId: String? = null): JoinRoomResponse? {
        val api = d1Manager.getApi() ?: return null
        return try {
            val res = api.joinRoom(JoinRoomRequest(roomCode = roomCode, playerName = playerName, avatarEmoji = avatar, level = level, userId = userId))
            if (res.success && res.playerId.isNotBlank()) {
                d1Manager.setActivePlayerId(res.playerId)
            }
            res
        } catch (e: Exception) {
            JoinRoomResponse(success = false, error = e.localizedMessage ?: "خطا در اتصال به D1")
        }
    }

    suspend fun getPublicRooms(): List<D1RoomSummary> {
        val api = d1Manager.getApi() ?: return emptyList()
        return try {
            api.getPublicRooms().rooms
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getD1RoomState(roomCode: String): RoomStateResponse? {
        val api = d1Manager.getApi() ?: return null
        return try {
            api.getRoomState(roomCode)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun startD1Match(roomCode: String): Boolean {
        val api = d1Manager.getApi() ?: return false
        return try {
            api.startMatch(roomCode).success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitD1Answer(roomCode: String, playerId: String, answerText: String): SubmitAnswerResponse? {
        val api = d1Manager.getApi() ?: return null
        return try {
            api.submitAnswer(roomCode, SubmitAnswerRequest(playerId = playerId, answerText = answerText))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getD1Cards(roomCode: String): List<D1CardDto> {
        val api = d1Manager.getApi() ?: return emptyList()
        return try {
            api.getCards(roomCode).cards
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getD1Chat(roomCode: String): List<D1MessageDto> {
        val api = d1Manager.getApi() ?: return emptyList()
        return try {
            api.getChat(roomCode).messages
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendD1ChatMessage(roomCode: String, playerId: String, playerName: String, text: String, isMentalist: Boolean): Boolean {
        val api = d1Manager.getApi() ?: return false
        return try {
            api.sendChatMessage(roomCode, SendMessageRequest(playerId, playerName, text, isMentalist)).success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitD1Guess(roomCode: String, assignments: List<GuessAssignment>): Boolean {
        val api = d1Manager.getApi() ?: return false
        return try {
            api.submitGuess(roomCode, SubmitGuessRequest(assignments)).success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun revealD1Round(roomCode: String): RevealResponse? {
        val api = d1Manager.getApi() ?: return null
        return try {
            api.revealAndCalculate(roomCode)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendD1Heartbeat(roomCode: String, playerId: String) {
        val api = d1Manager.getApi() ?: return
        try {
            api.sendHeartbeat(roomCode, HeartbeatRequest(playerId))
        } catch (_: Exception) {}
    }

    // --- AUTHENTICATION (D1 & LOCAL SESSION) ---

    suspend fun registerUser(
        username: String,
        email: String?,
        password: String,
        avatarEmoji: String
    ): Result<UserProfileEntity> {
        val api = d1Manager.getApi()
        if (api != null) {
            try {
                val res = api.register(RegisterRequest(username, email, password, avatarEmoji))
                if (res.success && res.user != null) {
                    val entity = UserProfileEntity(
                        id = res.user.id,
                        username = res.user.username,
                        email = res.user.email,
                        avatarEmoji = res.user.avatarEmoji,
                        level = res.user.level,
                        xp = res.user.xp,
                        coins = res.user.coins,
                        matchesPlayed = res.user.matchesPlayed,
                        wins = res.user.wins,
                        successfulBluffs = res.user.successfulBluffs,
                        correctGuesses = res.user.correctGuesses,
                        ghostBadges = res.user.ghostBadges,
                        truthseekerBadges = res.user.truthseekerBadges,
                        token = res.token
                    )
                    userDao.insertOrUpdate(entity)
                    d1Manager.setAuthToken(res.token)
                    return Result.success(entity)
                } else if (res.error != null) {
                    return Result.failure(Exception(res.error))
                }
            } catch (e: Exception) {
                // If network failure, provide clear message
                return Result.failure(Exception(e.localizedMessage ?: "خطا در اتصال به سرور Cloudflare D1"))
            }
        }

        // اگر هنوز آدرس D1 تنظیم نشده باشد، ثبت نام محلی با مقادیر کاملاً تمیز (صفر) انجام می‌شود
        val localId = "usr_" + System.currentTimeMillis()
        val localUser = UserProfileEntity(
            id = localId,
            username = username,
            email = email,
            avatarEmoji = avatarEmoji,
            level = 1,
            xp = 0,
            coins = 0,
            matchesPlayed = 0,
            wins = 0,
            successfulBluffs = 0,
            correctGuesses = 0,
            ghostBadges = 0,
            truthseekerBadges = 0
        )
        userDao.insertOrUpdate(localUser)
        return Result.success(localUser)
    }

    suspend fun loginUser(username: String, password: String): Result<UserProfileEntity> {
        val api = d1Manager.getApi()
        if (api != null) {
            try {
                val res = api.login(LoginRequest(username, password))
                if (res.success && res.user != null) {
                    val entity = UserProfileEntity(
                        id = res.user.id,
                        username = res.user.username,
                        email = res.user.email,
                        avatarEmoji = res.user.avatarEmoji,
                        level = res.user.level,
                        xp = res.user.xp,
                        coins = res.user.coins,
                        matchesPlayed = res.user.matchesPlayed,
                        wins = res.user.wins,
                        successfulBluffs = res.user.successfulBluffs,
                        correctGuesses = res.user.correctGuesses,
                        ghostBadges = res.user.ghostBadges,
                        truthseekerBadges = res.user.truthseekerBadges,
                        token = res.token
                    )
                    userDao.insertOrUpdate(entity)
                    d1Manager.setAuthToken(res.token)
                    return Result.success(entity)
                } else {
                    return Result.failure(Exception(res.error ?: "نام کاربری یا رمز عبور اشتباه است"))
                }
            } catch (e: Exception) {
                return Result.failure(Exception(e.localizedMessage ?: "خطا در برقراری ارتباط با D1"))
            }
        }

        // بررسی کاربر ذخیره‌شده محلی
        val local = userDao.getUserProfile()
        if (local != null && (local.username == username || local.email == username)) {
            d1Manager.setAuthToken(local.token)
            return Result.success(local)
        }

        return Result.failure(Exception("کاربری با این مشخصات یافت نشد. لطفاً ابتدا ثبت‌نام کنید."))
    }

    suspend fun logoutUser() {
        try {
            d1Manager.getApi()?.logout()
        } catch (_: Exception) {}
        d1Manager.setAuthToken(null)
        d1Manager.setActivePlayerId(null)
        userDao.clearProfile()
    }

    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfileFlow()

    val dailyQuests: Flow<List<DailyQuest>> = questDao.getAllQuestsFlow().map { entities ->
        if (entities.isEmpty()) {
            val defaults = getDefaultQuests()
            questDao.insertAll(defaults.map { it.toEntity() })
            defaults
        } else {
            entities.map { it.toModel() }
        }
    }

    val reports: Flow<List<ReportEntity>> = reportDao.getAllReportsFlow()

    suspend fun updateUsername(newUsername: String) {
        val current = userDao.getUserProfile() ?: return
        userDao.insertOrUpdate(current.copy(username = newUsername))
    }

    suspend fun recordMatchResult(
        gainedXp: Int,
        isWin: Boolean,
        successfulBluffs: Int,
        correctGuesses: Int,
        gotGhostBadge: Boolean,
        gotTruthseekerBadge: Boolean
    ) {
        val current = userDao.getUserProfile() ?: return
        val newXp = (current.xp + gainedXp).coerceAtLeast(0)
        // Level calculation: 100 XP per level
        val newLevel = (newXp / 100) + 1
        val newCoins = current.coins + (if (isWin) 80 else 30) + (correctGuesses * 15)

        val updated = current.copy(
            xp = newXp,
            level = newLevel,
            coins = newCoins,
            matchesPlayed = current.matchesPlayed + 1,
            wins = current.wins + (if (isWin) 1 else 0),
            successfulBluffs = current.successfulBluffs + successfulBluffs,
            correctGuesses = current.correctGuesses + correctGuesses,
            ghostBadges = current.ghostBadges + (if (gotGhostBadge) 1 else 0),
            truthseekerBadges = current.truthseekerBadges + (if (gotTruthseekerBadge) 1 else 0)
        )
        userDao.insertOrUpdate(updated)

        // Update daily quests
        updateQuestsProgress(
            bluffs = successfulBluffs,
            correctGuesses = correctGuesses,
            matches = 1
        )
    }

    private suspend fun updateQuestsProgress(bluffs: Int, correctGuesses: Int, matches: Int) {
        val entities = getDefaultQuests().map { it.toEntity() }
        entities.forEach { entity ->
            val added = when (entity.id) {
                "q_bluff" -> bluffs
                "q_guess" -> correctGuesses
                "q_match" -> matches
                else -> 0
            }
            if (added > 0) {
                val newProgress = (entity.currentProgress + added).coerceAtMost(entity.targetProgress)
                questDao.updateQuest(entity.copy(currentProgress = newProgress))
            }
        }
    }

    suspend fun claimQuest(questId: String, xpReward: Int) {
        val current = userDao.getUserProfile() ?: return
        userDao.insertOrUpdate(current.copy(xp = current.xp + xpReward, level = ((current.xp + xpReward) / 100) + 1))
    }

    suspend fun reportUser(reportedName: String, messageText: String, reason: String) {
        val current = userDao.getUserProfile()
        reportDao.insertReport(
            ReportEntity(
                reporterId = current?.id ?: "anonymous",
                reportedName = reportedName,
                messageText = messageText,
                reason = reason
            )
        )
    }

    fun getBadges(profile: UserProfileEntity): List<GameBadge> {
        return listOf(
            GameBadge(
                id = "ghost",
                title = "ردپای صفر (Ghost)",
                description = "در یک بازی کامل دستت توسط هیچ بازجویی خوانده نشود",
                iconEmoji = "👻",
                isUnlocked = profile.ghostBadges > 0
            ),
            GameBadge(
                id = "truthseeker",
                title = "دروغ‌سنج (Truthseeker)",
                description = "تمام کارت‌ها در نقش منتالیست با موفقیت حدس زده شوند",
                iconEmoji = "⚖️",
                isUnlocked = profile.truthseekerBadges > 0
            ),
            GameBadge(
                id = "bluff_master",
                title = "شاه‌بلوف",
                description = "ثبت حداقل ۵ بار فریب موفق بازجو در کارنامه",
                iconEmoji = "🎭",
                isUnlocked = profile.successfulBluffs >= 5
            ),
            GameBadge(
                id = "shadow_rank",
                title = "سایه پنهان",
                description = "رسیدن به سطح ۲۶ و کسب لقب سایه",
                iconEmoji = "👤",
                isUnlocked = profile.level >= 26
            ),
            GameBadge(
                id = "mentalist_master",
                title = "منتالیست ارشد",
                description = "رسیدن به سطح ۶۱ و کسب بالاترین عنوان روانشناسی",
                iconEmoji = "👁️",
                isUnlocked = profile.level >= 61
            )
        )
    }

    private fun getDefaultQuests(): List<DailyQuest> {
        return listOf(
            DailyQuest(
                id = "q_bluff",
                title = "استاد فریب",
                description = "امروز ۳ بار منتالیست را فریب بده و کارتت ناشناخته بماند",
                currentProgress = 0,
                targetProgress = 3,
                xpReward = 150
            ),
            DailyQuest(
                id = "q_guess",
                title = "مچ‌گیر هوشیار",
                description = "امروز ۲ حدس درست در نقش بازجو به ثبت برسان",
                currentProgress = 0,
                targetProgress = 2,
                xpReward = 100
            ),
            DailyQuest(
                id = "q_match",
                title = "روانشناس میدانی",
                description = "در ۲ مسابقه سریع سیاه بازی شرکت کن",
                currentProgress = 0,
                targetProgress = 2,
                xpReward = 80
            )
        )
    }

    private fun DailyQuest.toEntity() = DailyQuestEntity(
        id = id,
        title = title,
        description = description,
        currentProgress = currentProgress,
        targetProgress = targetProgress,
        xpReward = xpReward,
        isClaimed = isClaimed
    )

    private fun DailyQuestEntity.toModel() = DailyQuest(
        id = id,
        title = title,
        description = description,
        currentProgress = currentProgress,
        targetProgress = targetProgress,
        xpReward = xpReward,
        isClaimed = isClaimed
    )
}
