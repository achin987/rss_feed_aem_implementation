/*
 *  Copyright 2018 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.fedexsite.core.schedulers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.List;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

@ExtendWith(AemContextExtension.class)
class RssfeedScheduledTaskTest {

    private final AemContext context = new AemContext();

    @InjectMocks
    private RssfeedScheduledTask fixture = new RssfeedScheduledTask();

    private TestLogger logger = TestLoggerFactory.getTestLogger(fixture.getClass());

    @Mock
    JobManager jobManager;

    @Mock
    Job job;

    @BeforeEach
    void setup() {
        TestLoggerFactory.clear();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void run() {
        RssfeedScheduledTask.Config config = mock(RssfeedScheduledTask.Config.class);
        when(config.enableScheduler()).thenReturn(false);
        fixture.activate(config);
        fixture.run();
        List<LoggingEvent> events = logger.getLoggingEvents();
        assertEquals(1, events.size());
        LoggingEvent event = events.get(0);
        assertEquals(Level.DEBUG, event.getLevel());
        assertEquals(1, event.getArguments().size());
        assertEquals(false, event.getArguments().get(0));
    }

    @Test
    void runSchedulerEnabled(AemContext context) {
        RssfeedScheduledTask.Config config = mock(RssfeedScheduledTask.Config.class);
        when(config.enableScheduler()).thenReturn(true);
        context.registerService(JobManager.class, jobManager);
        fixture.activate(config);
        fixture.run();
        List<LoggingEvent> events = logger.getLoggingEvents();
        assertEquals(1, events.size());
        LoggingEvent event = events.get(0);
        assertEquals(Level.DEBUG, event.getLevel());
        assertEquals(1, event.getArguments().size());
        assertEquals(true, event.getArguments().get(0));
    }
}
