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

package org.jasig.cas;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Formatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A context listener that reports back the CAS application
 * deployment environment info. Details such as CAS versin,
 * Java/OS info as well as the server container info are logged.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component
public final class CasEnvironmentContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasEnvironmentContextListener.class);

    private static AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    /**
     * Instantiates a new Cas environment context listener.
     */
    public CasEnvironmentContextListener() {
        super();
        LOGGER.debug("[{}] initialized...", CasEnvironmentContextListener.class.getSimpleName());
    }

    /**
     * Logs environment info by collecting
     * details on the java and os deployment
     * environment. Data is logged at DEBUG
     * level.
     */
    @PostConstruct
    public void logEnvironmentInfo() {
        if (!INITIALIZED.get()) {
            LOGGER.info(collectEnvironmentInfo());
            INITIALIZED.set(true);
        }
    }

    /**
     * Collect environment info with
     * details on the java and os deployment
     * versions.
     *
     * @return environment info
     */
    private String collectEnvironmentInfo() {
        final Properties properties = System.getProperties();
        final Formatter formatter = new Formatter();
        formatter.format("\n******************** Welcome to CAS ********************\n");
        formatter.format("CAS Version: %s\n", CasVersion.getVersion());
        formatter.format("Java Home: %s\n", properties.get("java.home"));
        formatter.format("Java Vendor: %s\n", properties.get("java.vendor"));
        formatter.format("Java Version: %s\n", properties.get("java.version"));
        formatter.format("OS Architecture: %s\n", properties.get("os.arch"));
        formatter.format("OS Name: %s\n", properties.get("os.name"));
        formatter.format("OS Version: %s\n", properties.get("os.version"));
        formatter.format("*******************************************************\n");
        return formatter.toString();
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append(collectEnvironmentInfo());
        return builder.toString();
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        LOGGER.info("[{}] has loaded the CAS application context",
                event.getServletContext().getServerInfo());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {}
}
