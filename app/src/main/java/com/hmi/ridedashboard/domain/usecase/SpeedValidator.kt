package com.hmi.ridedashboard.domain.usecase

import com.hmi.ridedashboard.domain.model.SpeedReading
import javax.inject.Inject
import kotlin.math.roundToInt

class SpeedValidator @Inject constructor() {

    fun validate(rawKmh: Float?): SpeedReading = when {
        rawKmh == null -> SpeedReading.Unavailable
        rawKmh.isNaN() || rawKmh.isInfinite() -> SpeedReading.Invalid
        // Small negative readings are sensor noise around standstill, not a fault.
        rawKmh < 0f && rawKmh >= -SENSOR_NOISE_TOLERANCE_KMH -> SpeedReading.Valid(0)
        rawKmh < 0f -> SpeedReading.Invalid
        rawKmh > MAX_PLAUSIBLE_SPEED_KMH -> SpeedReading.Invalid
        else -> SpeedReading.Valid(rawKmh.roundToInt())
    }

    companion object {
        const val MAX_PLAUSIBLE_SPEED_KMH = 160f
        const val SENSOR_NOISE_TOLERANCE_KMH = 1f
    }
}
