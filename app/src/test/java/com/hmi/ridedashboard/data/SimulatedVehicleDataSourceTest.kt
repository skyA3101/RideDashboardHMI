package com.hmi.ridedashboard.data

import com.hmi.ridedashboard.data.simulator.ScenarioScript
import com.hmi.ridedashboard.data.simulator.ScriptStep
import com.hmi.ridedashboard.data.simulator.SimulatedVehicleDataSource
import com.hmi.ridedashboard.data.source.VehicleSignalDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedVehicleDataSourceTest {

    private fun TestScope.simulator() =
        SimulatedVehicleDataSource(StandardTestDispatcher(testScheduler), tickMillis = TICK)

    private fun ticksIn(script: List<ScriptStep>) = script.filterIsInstance<ScriptStep.Emit>().sumOf { it.ticks }

    @Test
    fun `nominal loop covers all five scenarios at one frame per tick`() = runTest {
        val frameCount = ticksIn(ScenarioScript.nominal)
        val timestamps = mutableListOf<Long>()

        val frames = simulator().vehicleSignals()
            .map { timestamps += currentTime; it }
            .take(frameCount)
            .toList()

        val expected = listOf(
            VehicleSignalDto(0f, motorOn = false, killSwitchActive = false), // Parked
            VehicleSignalDto(0f, motorOn = true, killSwitchActive = false), // Ready
            VehicleSignalDto(25f, motorOn = true, killSwitchActive = false), // Riding
            VehicleSignalDto(50f, motorOn = true, killSwitchActive = false), // Accelerating
            VehicleSignalDto(0f, motorOn = false, killSwitchActive = true), // Motor disabled
        )
        expected.forEach { assertTrue("missing $it", it in frames) }
        assertEquals(frames.indices.map { it * TICK }, timestamps)
    }

    @Test
    fun `fault injection adds invalid, missing and dropout frames`() = runTest {
        val sim = simulator().apply { setFaultInjectionEnabled(true) }
        val frameCount = ticksIn(ScenarioScript.nominal + ScenarioScript.faults)
        val timestamps = mutableListOf<Long>()

        val frames = sim.vehicleSignals()
            .map { timestamps += currentTime; it }
            .take(frameCount + 1) // first frame of the next loop, after the dropout
            .toList()

        assertTrue(frames.any { it.speedKmh != null && it.speedKmh!! < 0f })
        assertTrue(frames.any { it.speedKmh?.isNaN() == true })
        assertTrue(frames.any { it.speedKmh == null && it.motorOn == null && it.killSwitchActive == null })

        val dropoutTicks = ScenarioScript.faults.filterIsInstance<ScriptStep.Dropout>().sumOf { it.ticks }
        assertEquals((dropoutTicks + 1) * TICK, timestamps.last() - timestamps[timestamps.size - 2])
    }

    private companion object {
        const val TICK = 1_000L
    }
}
