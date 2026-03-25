package com.example.expensetracker.di

import android.content.Context
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Repository
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideRepository(db: AppDatabase): Repository = Repository(
        taskDao = db.taskDao(),
        expenseDao = db.expenseDao(),
        moodDao = db.moodDao()
    )
}

