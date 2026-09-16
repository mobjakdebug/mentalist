package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class D1ConnectionState {
    object Idle : D1ConnectionState()
    object Checking : D1ConnectionState()
    data class Connected(
        val database: String,
        val latencyMs: Long,
        val questionCount: Int,
        val activeRoomsCount: Int
    ) : D1ConnectionState()
    data class Error(val message: String) : D1ConnectionState()
}

class CloudflareD1Manager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cloudflare_d1_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WORKER_URL = "key_worker_url"
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        // Production Cloudflare Worker URL
        const val DEFAULT_WORKER_URL = "https://siahbazi-backend.mid25.workers.dev/"
        private const val LEGACY_PLACEHOLDER_URL = "https://siahbazi-backend.workers.dev/"
    }

    private var currentAuthToken: String? = null
    private var currentActivePlayerId: String? = null

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            currentAuthToken?.let { token ->
                if (token.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                    requestBuilder.header("X-Session-Token", token)
                }
            }

            currentActivePlayerId?.let { pId ->
                if (pId.isNotBlank()) {
                    requestBuilder.header("X-Player-Id", pId)
                }
            }

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private var currentApi: CloudflareD1Api? = null
    private var cachedUrl: String = ""

    private val _connectionState = MutableStateFlow<D1ConnectionState>(D1ConnectionState.Idle)
    val connectionState: StateFlow<D1ConnectionState> = _connectionState.asStateFlow()

    init {
        currentAuthToken = prefs.getString(KEY_AUTH_TOKEN, null)
        val savedUrl = getWorkerUrl()
        rebuildApi(savedUrl)
    }

    fun setAuthToken(token: String?) {
        currentAuthToken = token
        if (token != null) {
            prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        } else {
            prefs.edit().remove(KEY_AUTH_TOKEN).apply()
        }
    }

    fun setActivePlayerId(playerId: String?) {
        currentActivePlayerId = playerId
    }

    fun getWorkerUrl(): String {
        val saved = prefs.getString(KEY_WORKER_URL, null)
        if (saved.isNullOrBlank() || saved == LEGACY_PLACEHOLDER_URL) {
            prefs.edit().putString(KEY_WORKER_URL, DEFAULT_WORKER_URL).apply()
            return DEFAULT_WORKER_URL
        }
        return saved
    }

    fun setWorkerUrl(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        if (!cleanUrl.endsWith("/")) {
            cleanUrl = "$cleanUrl/"
        }
        prefs.edit().putString(KEY_WORKER_URL, cleanUrl).apply()
        rebuildApi(cleanUrl)
    }

    private fun rebuildApi(baseUrl: String) {
        cachedUrl = baseUrl
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            currentApi = retrofit.create(CloudflareD1Api::class.java)
        } catch (e: Exception) {
            currentApi = null
        }
    }

    fun getApi(): CloudflareD1Api? {
        if (currentApi == null) {
            rebuildApi(getWorkerUrl())
        }
        return currentApi
    }

    suspend fun testConnection(): D1ConnectionState = withContext(Dispatchers.IO) {
        _connectionState.value = D1ConnectionState.Checking
        val api = getApi()
        if (api == null) {
            val err = D1ConnectionState.Error("آدرس ورکر نامعتبر است")
            _connectionState.value = err
            return@withContext err
        }
        val startTime = System.currentTimeMillis()
        try {
            val response = api.checkHealth()
            val latency = System.currentTimeMillis() - startTime
            val state = D1ConnectionState.Connected(
                database = response.database.ifEmpty { "Cloudflare D1" },
                latencyMs = latency,
                questionCount = response.questionCount,
                activeRoomsCount = response.activeRoomsCount
            )
            _connectionState.value = state
            state
        } catch (e: Exception) {
            val err = D1ConnectionState.Error("عدم برقراری ارتباط: ${e.localizedMessage ?: "خطای ناشناخته"}")
            _connectionState.value = err
            err
        }
    }
}
