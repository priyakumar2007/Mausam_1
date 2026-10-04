package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// -------------------------------------------------------------
// Entities
// -------------------------------------------------------------

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Friend",
    val email: String = "guest@mausam.app",
    val isGuest: Boolean = true,
    val selectedLifestyles: String = "HEALTH,FITNESS,AGRICULTURE,TRAVEL,BEACH",
    val tempUnit: String = "C",
    val language: String = "ENGLISH",
    val notificationsEnabled: Boolean = true,
    val isDemoMode: Boolean = false
)

@Entity(tableName = "saved_fields")
data class SavedFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fieldName: String,
    val cropType: String,
    val locality: String,
    val district: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val plantingDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "crop_history")
data class CropHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fieldId: Long = 0,
    val fieldName: String,
    val imageUri: String,
    val analysisDate: String,
    val riskLevel: String,
    val reasons: String,
    val recommendations: String,
    val visualObservation: String,
    val weatherSummary: String,
    val locality: String
)

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locality: String,
    val district: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cacheKey: String,
    val jsonData: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// DAOs
// -------------------------------------------------------------

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}

@Dao
interface FieldDao {
    @Query("SELECT * FROM saved_fields ORDER BY id DESC")
    fun getAllFields(): Flow<List<SavedFieldEntity>>

    @Query("SELECT * FROM saved_fields WHERE id = :id LIMIT 1")
    suspend fun getFieldById(id: Long): SavedFieldEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: SavedFieldEntity): Long

    @Delete
    suspend fun deleteField(field: SavedFieldEntity)
}

@Dao
interface CropHistoryDao {
    @Query("SELECT * FROM crop_history ORDER BY id DESC")
    fun getAllCropHistory(): Flow<List<CropHistoryEntity>>

    @Query("SELECT * FROM crop_history WHERE fieldId = :fieldId ORDER BY id DESC")
    fun getHistoryForField(fieldId: Long): Flow<List<CropHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropHistory(history: CropHistoryEntity): Long
}

@Dao
interface SavedLocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY isFavorite DESC, id DESC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity): Long

    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)
}

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCachedWeather(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCachedWeather(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}

// -------------------------------------------------------------
// Database
// -------------------------------------------------------------

@Database(
    entities = [
        UserProfileEntity::class,
        SavedFieldEntity::class,
        CropHistoryEntity::class,
        SavedLocationEntity::class,
        WeatherCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun fieldDao(): FieldDao
    abstract fun cropHistoryDao(): CropHistoryDao
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mausam_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
