/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package org.jasig.cas.services.jmx;

import org.jasig.cas.services.ServicesManager;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Supports the basic {@link org.jasig.cas.services.ServicesManager}.
 *
 * @author Scott Battaglia
 * @since 3.4.4
 */

@ManagedResource(objectName = "CAS:name=JasigCasServicesManagerMBean",
        description = "Exposes the services management tool via JMX", log = true, logFile="jasig_cas_jmx.logger",
        currencyTimeLimit = 15)
public final class ServicesManagerMBean extends AbstractServicesManagerMBean<ServicesManager> {

    public ServicesManagerMBean(final ServicesManager servicesManager) {
        super(servicesManager);
    }
}
