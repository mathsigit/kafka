/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.kstream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class JoinWindowsTest {

    private static final long ANY_SIZE = 123L;
    private static final long ANY_OTHER_SIZE = 456L; // should be larger than anySize

    @Test
    public void validWindows() {
        JoinWindows.of(ANY_OTHER_SIZE)   // [ -anyOtherSize ; anyOtherSize ]
                   .before(ANY_SIZE)                    // [ -anySize ; anyOtherSize ]
                   .before(0)                          // [ 0 ; anyOtherSize ]
                   .before(-ANY_SIZE)                   // [ anySize ; anyOtherSize ]
                   .before(-ANY_OTHER_SIZE);             // [ anyOtherSize ; anyOtherSize ]

        JoinWindows.of(ANY_OTHER_SIZE)   // [ -anyOtherSize ; anyOtherSize ]
                   .after(ANY_SIZE)                     // [ -anyOtherSize ; anySize ]
                   .after(0)                           // [ -anyOtherSize ; 0 ]
                   .after(-ANY_SIZE)                    // [ -anyOtherSize ; -anySize ]
                   .after(-ANY_OTHER_SIZE);              // [ -anyOtherSize ; -anyOtherSize ]
    }

    @Test(expected = IllegalArgumentException.class)
    public void timeDifferenceMustNotBeNegative() {
        JoinWindows.of(-1);
    }

    @Test
    public void endTimeShouldNotBeBeforeStart() {
        final JoinWindows windowSpec = JoinWindows.of(ANY_SIZE);
        try {
            windowSpec.after(-ANY_SIZE - 1);
            fail("window end time should not be before window start time");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void startTimeShouldNotBeAfterEnd() {
        final JoinWindows windowSpec = JoinWindows.of(ANY_SIZE);
        try {
            windowSpec.before(-ANY_SIZE - 1);
            fail("window start time should not be after window end time");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Deprecated
    @Test
    public void untilShouldSetMaintainDuration() {
        final JoinWindows windowSpec = JoinWindows.of(ANY_SIZE);
        final long windowSize = windowSpec.size();
        assertEquals(windowSize, windowSpec.until(windowSize).maintainMs());
    }

    @Deprecated
    @Test
    public void retentionTimeMustNoBeSmallerThanWindowSize() {
        final JoinWindows windowSpec = JoinWindows.of(ANY_SIZE);
        final long windowSize = windowSpec.size();
        try {
            windowSpec.until(windowSize - 1);
            fail("should not accept retention time smaller than window size");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void gracePeriodShouldEnforceBoundaries() {
        JoinWindows.of(3L).grace(0L);

        try {
            JoinWindows.of(3L).grace(-1L);
            fail("should not accept negatives");
        } catch (final IllegalArgumentException e) {
            //expected
        }
    }

}