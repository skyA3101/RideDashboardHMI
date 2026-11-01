package com.hmi.ridedashboard.data.repository

import com.hmi.ridedashboard.data.source.VehicleDataSource
import com.hmi.ridedashboard.data.source.VehicleSignalDto
import com.hmi.ridedashboard.domain.model.VehicleSignal
import com.hmi.ridedashboard.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val dataSource: VehicleDataSource,
) : VehicleRepository {

    override fun observeVehicleSignals(): Flow<VehicleSignal> =
        dataSource.vehicleSignals().map { it.toDomain() }
}

internal fun VehicleSignalDto.toDomain() = VehicleSignal(
    speedKmh = speedKmh,
    motorOn = motorOn,
    killSwitchActive = killSwitchActive,
)
