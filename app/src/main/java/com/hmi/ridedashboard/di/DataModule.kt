package com.hmi.ridedashboard.di

import com.hmi.ridedashboard.data.repository.SimulationControllerImpl
import com.hmi.ridedashboard.data.repository.VehicleRepositoryImpl
import com.hmi.ridedashboard.data.simulator.SimulatedVehicleDataSource
import com.hmi.ridedashboard.data.source.VehicleDataSource
import com.hmi.ridedashboard.domain.repository.SimulationController
import com.hmi.ridedashboard.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // Swap this binding for a CAN/VHAL-backed source; nothing above the data layer changes.
    @Binds
    abstract fun bindVehicleDataSource(impl: SimulatedVehicleDataSource): VehicleDataSource

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindSimulationController(impl: SimulationControllerImpl): SimulationController
}
