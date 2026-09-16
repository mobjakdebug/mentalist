package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val username: String,
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
    val truthseekerBadges: Int = 0,
    val token: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_quests")
data class DailyQuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int = 0,
    val targetProgress: Int,
    val xpReward: Int,
    val isClaimed: Boolean = false
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporterId: String,
    val reportedName: String,
    val messageText: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()

    @Query("UPDATE user_profile SET xp = xp + :deltaXp WHERE id = :userId")
    suspend fun addXp(userId: String, deltaXp: Int)
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM daily_quests")
    fun getAllQuestsFlow(): Flow<List<DailyQuestEntity>>

    @Query("SELECT * FROM daily_quests")
    suspend fun getAllQuests(): List<DailyQuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quests: List<DailyQuestEntity>)

    @Update
    suspend fun updateQuest(quest: DailyQuestEntity)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Insert
    suspend fun insertReport(report: ReportEntity)
}

@Database(
    entities = [UserProfileEntity::class, DailyQuestEntity::class, ReportEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "siah_bazi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
