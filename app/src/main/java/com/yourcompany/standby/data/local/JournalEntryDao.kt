package com.yourcompany.standby.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yourcompany.standby.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY displayOrder ASC, createdAt DESC")
    fun getAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE isEnabled = 1 ORDER BY displayOrder ASC, createdAt DESC")
    suspend fun getEnabled(): List<JournalEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}
