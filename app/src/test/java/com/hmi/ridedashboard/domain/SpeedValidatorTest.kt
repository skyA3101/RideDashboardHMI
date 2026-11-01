package com.hmi.ridedashboard.domain

import com.hmi.ridedashboard.domain.model.SpeedReading
import com.hmi.ridedashboard.domain.usecase.SpeedValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedValidatorTest {

    private val validator = SpeedValidator()

    @Test
    fun `plausible speeds are rounded to whole km per h`() {
        assertEquals(SpeedReading.Valid(0), validator.validate(0f))
        assertEquals(SpeedReading.Valid(25), validator.validate(24.6f))
        assertEquals(SpeedReading.Valid(160), validator.validate(160f))
    }

    @Test
    fun `negative NaN infinite and out of range speeds are invalid`() {
        listOf(-12f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 160.1f, 999f).forEach {
            assertEquals("speed $it", SpeedReading.Invalid, validator.validate(it))
        }
    }

    @Test
    fun `small negative noise at standstill reads as zero`() {
        assertEquals(SpeedReading.Valid(0), validator.validate(-0.4f))
    }

    @Test
    fun `missing speed is unavailable`() {
        assertEquals(SpeedReading.Unavailable, validator.validate(null))
    }
}
