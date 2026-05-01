package com.stepcounter.app.di

import android.content.Context
import com.stepcounter.app.data.local.TouristGuidePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    @Provides
    @Singleton
    fun provideTouristGuidePreferences(
        @ApplicationContext context: Context
    ): TouristGuidePreferences {
        return TouristGuidePreferences(context)
    }
}
