/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ExperimentalTime::class)
package test.time

import kotlin.test.*
import kotlin.time.*

class TestTimeSourceTest {

    @Test
    fun overflows() {
        for (enormousDuration in listOf(Duration.INFINITE, Duration.nanoseconds(Double.MAX_VALUE), Duration.nanoseconds(Long.MAX_VALUE) * 2)) {
            assertFailsWith<IllegalStateException>(enormousDuration.toString()) { TestTimeSource() += enormousDuration }
            assertFailsWith<IllegalStateException>((-enormousDuration).toString()) { TestTimeSource() += -enormousDuration }
        }

        val moderatePositiveDuration = Duration.nanoseconds(Long.MAX_VALUE.takeHighestOneBit())
        val borderlinePositiveDuration = Duration.nanoseconds(Long.MAX_VALUE) // rounded to 2.0^63, which is slightly more than Long.MAX_VALUE
        val borderlineNegativeDuration = Duration.nanoseconds(Long.MIN_VALUE)
        run {
            val timeSource = TestTimeSource()
            timeSource += moderatePositiveDuration
            assertFailsWith<IllegalStateException>("Should overflow positive") { timeSource += moderatePositiveDuration }
        }
        run {
            val timeSource = TestTimeSource()
            timeSource += borderlinePositiveDuration
            assertFailsWith<IllegalStateException>("Should overflow positive") { timeSource += Duration.nanoseconds(1) }
        }
        run {
            val timeSource = TestTimeSource()
            timeSource += borderlineNegativeDuration
            assertFailsWith<IllegalStateException>("Should overflow negative") { timeSource += -Duration.nanoseconds(1) }
        }

        run {
            val timeSource = TestTimeSource()
            timeSource += moderatePositiveDuration
            // does not overflow event if duration doesn't fit in long
            timeSource += -moderatePositiveDuration + borderlineNegativeDuration
        }
    }

    @Test
    fun nanosecondRounding() {
        val timeSource = TestTimeSource()
        val mark = timeSource.markNow()

        repeat(10_000) {
            timeSource += Duration.nanoseconds(0.9)

            assertEquals(Duration.ZERO, mark.elapsedNow())
        }

        timeSource += Duration.nanoseconds(1.9)
        assertEquals(Duration.nanoseconds(1), mark.elapsedNow())
    }
}
