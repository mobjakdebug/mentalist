package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameViewModel
import com.example.model.GamePhase
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SiahBaziApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SiahBaziApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Display report or system snackbars
    LaunchedEffect(uiState.reportSuccessSnackbar) {
        uiState.reportSuccessSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    val isLocalUserMentalist = uiState.players.find { it.isLocalUser }?.isMentalist == true
    val isInGame = uiState.currentPhase in listOf(
        GamePhase.ANSWERING,
        GamePhase.SHUFFLING,
        GamePhase.INTERROGATION,
        GamePhase.GUESSING,
        GamePhase.REVEAL
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isInGame) {
                val maxSeconds = when (uiState.currentPhase) {
                    GamePhase.ANSWERING -> 45
                    GamePhase.SHUFFLING -> 4
                    GamePhase.INTERROGATION -> 150
                    GamePhase.GUESSING -> 30
                    else -> 30
                }
                GameHeaderBar(
                    roundText = "راند ${uiState.currentRound} از ${uiState.totalRounds}",
                    secondsRemaining = uiState.timerSecondsRemaining,
                    maxSeconds = maxSeconds,
                    isMentalist = isLocalUserMentalist,
                    onInfoClick = { viewModel.openRulesDialog() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepObsidian)
        ) {
            AnimatedContent(
                targetState = uiState.currentPhase,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "phase_transition"
            ) { phase ->
                when (phase) {
                    GamePhase.AUTH -> {
                        AuthScreen(
                            d1State = uiState.d1ConnectionState,
                            workerUrl = uiState.workerUrl,
                            isLoading = uiState.isAuthLoading,
                            errorMessage = uiState.authErrorMessage,
                            onRegister = { username, email, password, avatar ->
                                viewModel.register(username, email, password, avatar)
                            },
                            onLogin = { username, password ->
                                viewModel.login(username, password)
                            },
                            onOpenD1Settings = { viewModel.openD1ConfigDialog() },
                            onOpenD1Guide = { viewModel.openD1GuideDialog() }
                        )
                    }

                    GamePhase.HOME -> {
                        val profile = uiState.userProfile
                        if (profile != null) {
                            HomeScreen(
                                userProfile = profile,
                                badges = uiState.badges,
                                d1State = uiState.d1ConnectionState,
                                workerUrl = uiState.workerUrl,
                                publicRooms = uiState.publicRooms,
                                isRefreshingRooms = uiState.isRefreshingRooms,
                                onCreateRoom = { viewModel.createRoom() },
                                onOpenJoinDialog = { viewModel.openJoinRoomDialog(it) },
                                onRefreshRooms = { viewModel.refreshD1Data() },
                                onOpenD1Settings = { viewModel.openD1ConfigDialog() },
                                onOpenD1Guide = { viewModel.openD1GuideDialog() },
                                onOpenQuests = { viewModel.openQuestsDialog() },
                                onOpenQuestions = { viewModel.openQuestionBankDialog() },
                                onOpenRules = { viewModel.openRulesDialog() },
                                onLogout = { viewModel.logout() }
                            )
                        } else {
                            AuthScreen(
                                d1State = uiState.d1ConnectionState,
                                workerUrl = uiState.workerUrl,
                                isLoading = uiState.isAuthLoading,
                                errorMessage = uiState.authErrorMessage,
                                onRegister = { username, email, password, avatar ->
                                    viewModel.register(username, email, password, avatar)
                                },
                                onLogin = { username, password ->
                                    viewModel.login(username, password)
                                },
                                onOpenD1Settings = { viewModel.openD1ConfigDialog() },
                                onOpenD1Guide = { viewModel.openD1GuideDialog() }
                            )
                        }
                    }

                    GamePhase.MATCHMAKING, GamePhase.LOBBY -> {
                        LobbyPhaseScreen(
                            roomCode = uiState.roomCode,
                            players = uiState.players,
                            currentRound = uiState.currentRound,
                            totalRounds = uiState.totalRounds,
                            isHost = uiState.isHost,
                            onStartMatch = { viewModel.startMatch() },
                            onLeaveRoom = { viewModel.leaveRoom() }
                        )
                    }

                    GamePhase.ANSWERING -> {
                        AnsweringPhaseScreen(
                            question = uiState.currentQuestion,
                            players = uiState.players,
                            userAnswerText = uiState.localUserAnswer,
                            hasSubmitted = uiState.hasLocalUserSubmitted,
                            onAnswerChange = { viewModel.updateUserAnswerText(it) },
                            onSubmitAnswer = { viewModel.submitAnswer() }
                        )
                    }

                    GamePhase.SHUFFLING -> {
                        ShufflingPhaseScreen()
                    }

                    GamePhase.INTERROGATION -> {
                        InterrogationPhaseScreen(
                            players = uiState.players,
                            cards = uiState.anonymousCards,
                            chatMessages = uiState.chatMessages,
                            userChatInput = uiState.userChatInput,
                            isLocalUserMentalist = isLocalUserMentalist,
                            afkWarningActive = uiState.afkWarningActive,
                            onUserChatInputChange = { viewModel.updateUserChatInput(it) },
                            onSendMessage = { viewModel.sendUserChatMessage() },
                            onQuickProbe = { viewModel.sendQuickProbe(it) },
                            onProceedToGuessing = { viewModel.advanceToGuessingPhase() },
                            onReportMessage = { sender, text -> viewModel.openReportDialog(sender, text) }
                        )
                    }

                    GamePhase.GUESSING -> {
                        GuessingPhaseScreen(
                            players = uiState.players,
                            cards = uiState.anonymousCards,
                            selectedCard = uiState.selectedCardForAssignment,
                            isLocalUserMentalist = isLocalUserMentalist,
                            onSelectCard = { viewModel.selectCardForAssignment(it) },
                            onAssignPlayer = { viewModel.assignSelectedCardToPlayer(it) },
                            onConfirmDeductions = { viewModel.submitMentalistGuessesAndReveal() }
                        )
                    }

                    GamePhase.REVEAL -> {
                        RevealPhaseScreen(
                            results = uiState.revealResults,
                            activeStepIndex = uiState.revealStepIndex,
                            isDone = uiState.isRevealingDone,
                            onProceed = { viewModel.nextRevealStep() }
                        )
                    }

                    GamePhase.ROUND_SUMMARY, GamePhase.GAME_OVER -> {
                        RoundSummaryScreen(
                            players = uiState.players,
                            currentRound = uiState.currentRound,
                            totalRounds = uiState.totalRounds,
                            isFinalGameOver = (phase == GamePhase.GAME_OVER),
                            onNextRound = { viewModel.startMatch() },
                            onReturnHome = { viewModel.leaveRoom() }
                        )
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.showD1ConfigDialog) {
        D1SettingsDialog(
            initialUrl = uiState.workerUrl,
            connectionState = uiState.d1ConnectionState,
            onSaveUrl = { viewModel.saveWorkerUrl(it) },
            onTestConnection = { viewModel.refreshD1Data() },
            onOpenGuide = {
                viewModel.closeD1ConfigDialog()
                viewModel.openD1GuideDialog()
            },
            onDismiss = { viewModel.closeD1ConfigDialog() }
        )
    }

    if (uiState.showD1GuideDialog) {
        D1GuideDialog(onDismiss = { viewModel.closeD1GuideDialog() })
    }

    if (uiState.showJoinRoomDialog) {
        JoinRoomDialog(
            initialCode = uiState.roomCodeInput,
            errorMessage = uiState.joinErrorMessage,
            isLoading = uiState.isJoiningOrCreating,
            onJoin = { viewModel.joinRoom(it) },
            onDismiss = { viewModel.closeJoinRoomDialog() }
        )
    }

    if (uiState.reportDialogVisible && uiState.reportingTarget != null) {
        ReportViolationDialog(
            reportedName = uiState.reportingTarget?.first ?: "",
            messageText = uiState.reportingTarget?.second ?: "",
            onDismiss = { viewModel.closeReportDialog() },
            onSubmitReport = { reason -> viewModel.submitReport(reason) }
        )
    }

    if (uiState.showRulesDialog) {
        GameRulesDialog(onDismiss = { viewModel.closeRulesDialog() })
    }

    if (uiState.showQuestsDialog) {
        QuestsAndBadgesDialog(
            quests = uiState.dailyQuests,
            badges = uiState.badges,
            onClaimQuest = { viewModel.claimQuest(it) },
            onDismiss = { viewModel.closeQuestsDialog() }
        )
    }

    if (uiState.showQuestionBankDialog) {
        QuestionBankDialog(onDismiss = { viewModel.closeQuestionBankDialog() })
    }
}
