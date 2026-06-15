package com.yourcompany.standby.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.yourcompany.standby.data.local.AppStateDao
import com.yourcompany.standby.data.local.JournalEntryDao
import com.yourcompany.standby.data.local.PomodoroSessionDao
import com.yourcompany.standby.data.local.StandByDatabase
import com.yourcompany.standby.data.local.MIGRATION_1_2
import com.yourcompany.standby.data.local.MIGRATION_2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StandByDatabase {
        return Room.databaseBuilder(
            context,
            StandByDatabase::class.java,
            "StandByDatabase"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }

    @Provides
    fun provideAppStateDao(database: StandByDatabase): AppStateDao {
        return database.appStateDao()
    }

    @Provides
    fun providePomodoroSessionDao(database: StandByDatabase): PomodoroSessionDao {
        return database.pomodoroSessionDao()
    }

    @Provides
    fun provideJournalEntryDao(database: StandByDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("standby_settings") }
        )
    }
}
