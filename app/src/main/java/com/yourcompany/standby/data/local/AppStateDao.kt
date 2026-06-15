package com.yourcompany.standby.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yourcompany.standby.data.local.entity.AppState
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {
    @Query("SELECT * FROM app_state WHERE id = 1 LIMIT 1")
    suspend fun getAppState(): AppState?

    @Query("SELECT * FROM app_state WHERE id = 1 LIMIT 1")
    fun getAppStateFlow(): Flow<AppState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appState: AppState)

    @Update
    suspend fun update(appState: AppState)
}
