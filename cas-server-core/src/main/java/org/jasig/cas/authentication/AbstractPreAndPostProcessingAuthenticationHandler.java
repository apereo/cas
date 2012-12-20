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
package org.jasig.cas.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Abstract authentication handler that allows deployers to utilize the bundled
 * AuthenticationHandlers while providing a mechanism to perform tasks before
 * and after authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractPreAndPostProcessingAuthenticationHandler implements AuthenticationHandler {

    /** Instance of logging for subclasses. */
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** The name of the authentication handler. */
    @NotNull
    private String name = getClass().getName();

    /**
     * Method to execute before authentication occurs.
     * 
     * @param credential Credential to authenticate.
     *
     * @return True if authentication should continue, false otherwise.
     */
    protected boolean preAuthenticate(final Credential credential) {
        return true;
    }

    /**
     * Method to execute after authentication occurs.
     * 
     * @param credential Successfully authenticated credential.
     * @param result Result produced by the authentication handler that authenticated the credential.
     *
     * @return Handler result provided or a modified version thereof.
     *
     * @throws GeneralSecurityException When authentication should fail for security reasons.
     * @throws IOException When authentication fails for reasons other than security.
     */
    protected HandlerResult postAuthenticate(final Credential credential, final HandlerResult result)
        throws GeneralSecurityException, IOException {
        return result;
    }
    
    public final void setName(final String name) {
        this.name = name;
    }
    
    public final String getName() {
        if (StringUtils.hasText(this.name)) {
            return this.name;
        }
        return getClass().getSimpleName();
    }

    public final HandlerResult authenticate(final Credential credential)
            throws GeneralSecurityException, IOException {

        if (!preAuthenticate(credential)) {
            throw new GeneralSecurityException("Pre-authentication failed.");
        }
        return postAuthenticate(credential, doAuthentication(credential));
    }

    protected abstract HandlerResult doAuthentication(Credential credential)
            throws GeneralSecurityException, IOException;
}
