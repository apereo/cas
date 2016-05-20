/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.support.saml.web.flow.mdui;

import org.opensaml.saml.metadata.resolver.filter.impl.MetadataFilterChain;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * A {@link StaticMetadataResolverAdapter} that loads metadata from static xml files
 * served by urls or locally.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class StaticMetadataResolverAdapter extends AbstractMetadataResolverAdapter implements Job {
    private static final int DEFAULT_METADATA_REFRESH_INTERNAL_MINS = 300;

    /**
      * Refresh metadata every {@link #DEFAULT_METADATA_REFRESH_INTERNAL_MINS}
      * minutes by default.
      **/
    private int refreshIntervalInMinutes = DEFAULT_METADATA_REFRESH_INTERNAL_MINS;

    /**
     * New ctor - required for serialization and job scheduling.
     */
    public StaticMetadataResolverAdapter() {
        super();
    }

    /**
     * Instantiates a new static metadata resolver adapter.
     *
     * @param metadataResources the metadata resources
     */
    public StaticMetadataResolverAdapter(final Map<Resource, MetadataFilterChain> metadataResources) {
        super(metadataResources);
    }

    public void setRefreshIntervalInMinutes(final int refreshIntervalInMinutes) {
        this.refreshIntervalInMinutes = refreshIntervalInMinutes;
    }

    /**
     * Refresh metadata. Schedules the job to retrieve metadata.
     */
    @PostConstruct
    public void refreshMetadata() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                buildMetadataResolverAggregate();
            }
        });
        thread.start();

        final JobDetail job = JobBuilder.newJob(this.getClass())
                .withIdentity(this.getClass().getSimpleName()).build();
        final Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(this.refreshIntervalInMinutes)
                        .repeatForever()).build();

        final SchedulerFactory schFactory = new StdSchedulerFactory();

        try {
            final Scheduler sch = schFactory.getScheduler();
            sch.start();
            sch.scheduleJob(job, trigger);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        buildMetadataResolverAggregate();
    }
}
