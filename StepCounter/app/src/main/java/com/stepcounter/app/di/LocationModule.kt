package com.stepcounter.app.di

import android.content.Context
import androidx.room.Room
import com.stepcounter.app.data.local.LocationDao
import com.stepcounter.app.data.local.TracksDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideTracksDatabase(@ApplicationContext context: Context): TracksDatabase =
        Room.databaseBuilder(context, TracksDatabase::class.java, "tracks.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLocationDao(db: TracksDatabase): LocationDao = db.locationDao()
}
