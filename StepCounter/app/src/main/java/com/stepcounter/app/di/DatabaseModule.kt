package com.stepcounter.app.di

import android.content.Context
import androidx.room.Room
import com.stepcounter.app.data.local.DailyStepsDao
import com.stepcounter.app.data.local.StepsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StepsDatabase =
        Room.databaseBuilder(context, StepsDatabase::class.java, "steps.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDailyStepsDao(db: StepsDatabase): DailyStepsDao = db.dailyStepsDao()
}
