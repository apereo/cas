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

package org.jasig.cas.audit.spi;

import com.github.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.AopUtils;

import javax.validation.constraints.NotNull;

/**
 * Resolves a service id to the service.
 * <p>
 * The expectation is that args[0] is a Long.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.6
 */
public final class ServiceManagementResourceResolver implements AuditResourceResolver {

    public String[] resolveFrom(final JoinPoint target, final Object returnValue) {
        return findService(target);
    }

    public String[] resolveFrom(final JoinPoint target, final Exception exception) {
        return findService(target);
    }

    private String[] findService(final JoinPoint joinPoint) {
        final JoinPoint j = AopUtils.unWrapJoinPoint(joinPoint);

        final Long id = (Long) j.getArgs()[0];

        if (id == null) {
            return new String[] {""};
        }

        return new String[] {"id=" + id};
    }
}
