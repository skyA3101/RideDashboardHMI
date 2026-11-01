package com.hmi.ridedashboard.domain.model

import javax.inject.Qualifier

/** Time without a new frame after which the vehicle state is considered stale. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StaleTimeoutMillis
