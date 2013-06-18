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

import org.jasig.cas.services.ReloadableServicesManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Provides capabilities to reload a {@link org.jasig.cas.services.ReloadableServicesManager} from JMX.
 * <p>
 * You should only expose either this class or
 * the {@link org.jasig.cas.services.jmx.ServicesManagerMBean}, but not both.
 *
 * @author Scott Battaglia

 * @since 3.4.4
 */
@ManagedResource(objectName = "CAS:name=JasigCasServicesManagerMBean",
        description = "Exposes the services management tool via JMX", log = true, logFile="jasig_cas_jmx.logger",
        currencyTimeLimit = 15)
public final class ReloadableServicesManagerMBean extends AbstractServicesManagerMBean<ReloadableServicesManager> {

    public ReloadableServicesManagerMBean(final ReloadableServicesManager reloadableServicesManager) {
        super(reloadableServicesManager);
    }

    @ManagedOperation(description = "Reloads the list of the services from the persistence storage.")
    public void reload() {
        getServicesManager().reload();
    }
}
