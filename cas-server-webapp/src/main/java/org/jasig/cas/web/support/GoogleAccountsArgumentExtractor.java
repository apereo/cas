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
package org.jasig.cas.web.support;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.GoogleAccountsService;
import org.jasig.cas.authentication.principal.WebApplicationService;

/**
 * Constructs a GoogleAccounts compatible service and provides the public and
 * private keys.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class GoogleAccountsArgumentExtractor extends AbstractSingleSignOutEnabledArgumentExtractor {

    @NotNull
    private PublicKey publicKey;

    @NotNull
    private PrivateKey privateKey;

    private String alternateUsername;

    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return GoogleAccountsService.createServiceFrom(request,
            this.privateKey, this.publicKey, this.alternateUsername);
    }

    public void setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Sets an alternate username to send to Google (i.e. fully qualified email address).  Relies on an appropriate
     * attribute available for the user.
     * <p>
     * Note that this is optional and the default is to use the normal identifier.
     *
     * @param alternateUsername the alternate username.  This is OPTIONAL.
     */
    public void setAlternateUsername(final String alternateUsername) {
        this.alternateUsername = alternateUsername;
    }
}
