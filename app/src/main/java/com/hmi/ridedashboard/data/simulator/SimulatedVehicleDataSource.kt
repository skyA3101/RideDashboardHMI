package com.hmi.ridedashboard.data.simulator

import com.hmi.ridedashboard.data.source.VehicleDataSource
import com.hmi.ridedashboard.data.source.VehicleSignalDto
import com.hmi.ridedashboard.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SimulationTickMillis

/** Plays [ScenarioScript] in a loop, one frame per tick (~1 s). */
@Singleton
class SimulatedVehicleDataSource @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @SimulationTickMillis private val tickMillis: Long,
) : VehicleDataSource {

    private val _faultInjectionEnabled = MutableStateFlow(false)
    val faultInjectionEnabled: StateFlow<Boolean> = _faultInjectionEnabled.asStateFlow()

    fun setFaultInjectionEnabled(enabled: Boolean) {
        _faultInjectionEnabled.value = enabled
    }

    // Toggling fault injection restarts the script from the beginning.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun vehicleSignals(): Flow<VehicleSignalDto> =
        _faultInjectionEnabled
            .flatMapLatest { faults ->
                play(if (faults) ScenarioScript.nominal + ScenarioScript.faults else ScenarioScript.nominal)
            }
            .flowOn(dispatcher)

    private fun play(script: List<ScriptStep>): Flow<VehicleSignalDto> = flow {
        while (true) {
            for (step in script) {
                when (step) {
                    is ScriptStep.Emit -> repeat(step.ticks) {
                        emit(step.frame)
                        delay(tickMillis)
                    }
                    is ScriptStep.Dropout -> delay(tickMillis * step.ticks)
                }
            }
        }
    }
}
