package com.hmi.ridedashboard.di

import com.hmi.ridedashboard.data.simulator.SimulationTickMillis
import com.hmi.ridedashboard.domain.model.StaleTimeoutMillis
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @SimulationTickMillis
    fun provideSimulationTickMillis(): Long = 1_000L

    /** 2.5 missed ticks: tolerant of jitter, quick enough for a rider to notice. */
    @Provides
    @StaleTimeoutMillis
    fun provideStaleTimeoutMillis(): Long = 2_500L
}
