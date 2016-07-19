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
package org.jasig.cas.services.audit;

import org.aspectj.lang.JoinPoint;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.audit.spi.TicketOrCredentialPrincipalResolver;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Principal resolver for inspektr based on pac4j.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Component("pac4jAuditablePrincipalResolver")
public class Pac4jAuditablePrincipalResolver extends TicketOrCredentialPrincipalResolver {
    
    /**
     * Instantiates a new Pac4j auditable principal resolver.
     *
     * @param centralAuthenticationService the central authentication service
     */
    @Autowired
    public Pac4jAuditablePrincipalResolver(@Qualifier("centralAuthenticationService") 
                                           final CentralAuthenticationService centralAuthenticationService) {
        super(centralAuthenticationService);
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        return getFromSecurityContext();
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Exception exception) {
        return getFromSecurityContext();
    }

    @Override
    public String resolve() {
        return getFromSecurityContext();
    }

    private String getFromSecurityContext() {
        return WebUtils.getAuthenticatedUsername();
    }
}
