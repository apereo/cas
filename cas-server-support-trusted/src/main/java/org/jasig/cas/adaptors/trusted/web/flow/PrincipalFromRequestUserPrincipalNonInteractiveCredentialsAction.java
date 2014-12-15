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
package org.jasig.cas.adaptors.trusted.web.flow;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Implementation of the NonInteractiveCredentialsAction that looks for a user
 * principal that is set in the <code>HttpServletRequest</code> and attempts
 * to construct a Principal (and thus a PrincipalBearingCredential). If it
 * doesn't find one, this class returns and error event which tells the web flow
 * it could not find any credentials.
 *
 * @author Scott Battaglia
 * @since 3.0.0.5
 */
public final class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction
            extends AbstractNonInteractiveCredentialsAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Credential constructCredentialsFromRequest(
            final RequestContext context) {
        final HttpServletRequest request = WebUtils
                .getHttpServletRequest(context);
        final Principal principal = request.getUserPrincipal();

        if (principal != null) {

            logger.debug("UserPrincipal [{}] found in HttpServletRequest", principal.getName());
            return new PrincipalBearingCredential(this.principalFactory.createPrincipal(principal.getName()));
        }

        logger.debug("UserPrincipal not found in HttpServletRequest.");
        return null;
    }
}
