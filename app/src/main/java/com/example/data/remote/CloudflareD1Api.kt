package com.example.data.remote

import retrofit2.http.*

interface CloudflareD1Api {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(): GenericSuccessResponse

    @GET("api/auth/me")
    suspend fun getMe(): AuthResponse

    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @GET("api/questions")
    suspend fun getQuestions(): QuestionsResponse

    @POST("api/questions")
    suspend fun addQuestion(@Body request: CreateQuestionRequest): GenericSuccessResponse

    @GET("api/rooms/public")
    suspend fun getPublicRooms(): PublicRoomsResponse

    @POST("api/rooms/create")
    suspend fun createRoom(@Body request: CreateRoomRequest): CreateRoomResponse

    @POST("api/rooms/join")
    suspend fun joinRoom(@Body request: JoinRoomRequest): JoinRoomResponse

    @GET("api/rooms/{code}")
    suspend fun getRoomState(@Path("code") roomCode: String): RoomStateResponse

    @POST("api/rooms/{code}/start")
    suspend fun startMatch(@Path("code") roomCode: String): GenericSuccessResponse

    @POST("api/rooms/{code}/answers")
    suspend fun submitAnswer(
        @Path("code") roomCode: String,
        @Body request: SubmitAnswerRequest
    ): SubmitAnswerResponse

    @GET("api/rooms/{code}/cards")
    suspend fun getCards(@Path("code") roomCode: String): CardsResponse

    @GET("api/rooms/{code}/chat")
    suspend fun getChat(@Path("code") roomCode: String): ChatResponse

    @POST("api/rooms/{code}/chat")
    suspend fun sendChatMessage(
        @Path("code") roomCode: String,
        @Body request: SendMessageRequest
    ): SendMessageResponse

    @POST("api/rooms/{code}/phase")
    suspend fun advancePhase(
        @Path("code") roomCode: String,
        @Body request: AdvancePhaseRequest
    ): GenericSuccessResponse

    @POST("api/rooms/{code}/guess")
    suspend fun submitGuess(
        @Path("code") roomCode: String,
        @Body request: SubmitGuessRequest
    ): GenericSuccessResponse

    @POST("api/rooms/{code}/reveal")
    suspend fun revealAndCalculate(
        @Path("code") roomCode: String
    ): RevealResponse

    @POST("api/rooms/{code}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("code") roomCode: String,
        @Body request: HeartbeatRequest
    ): GenericSuccessResponse

    @POST("api/reports")
    suspend fun submitReport(@Body request: ReportRequest): GenericSuccessResponse
}
