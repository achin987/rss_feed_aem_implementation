/*
 *  Copyright 2015 Adobe Systems Incorporated
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

import com.fedexsite.core.utils.FedexConstants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RSS cron-job like tasks that get executed regularly once in 10 minutes.
 * It starts a job for every 10 minutes
 * set the property values in /system/console/configMgr
 */
@Designate(ocd= RssfeedScheduledTask.Config.class)
@Component(service=Runnable.class)
public class RssfeedScheduledTask implements Runnable {

    @ObjectClassDefinition(name="A RSS Stack scheduled task",
                           description = "Cron job to fetch articles from RSS feed")
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 11 11 11 11 ?";

        @AttributeDefinition(name = "Concurrent task",
                             description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "Enable Rss Stack Scheduler",
            description = "To enable Scheduler. It will be true in author and false in publish by run modes")
        boolean enableScheduler() default false;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean enableScheduler;

    @Reference
    private JobManager jobManager;
    
    @Override
    public void run() {
        logger.debug("Job to fetch details from RSS Feed is now running, myParameter='{}'", enableScheduler);
        if(enableScheduler){
            Map<String, Object> jobProperties = new HashMap<>();
            jobProperties.put(FedexConstants.DATE, new SimpleDateFormat(FedexConstants.YYYY_MM_DD).format(Calendar.getInstance().getTime()));
            jobManager.addJob("rssfeed/migration/job",jobProperties);
        }

    }

    @Activate
    protected void activate(final Config config) {
        enableScheduler = config.enableScheduler();
    }

}
