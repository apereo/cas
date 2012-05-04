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
package org.jasig.cas.adaptors.cas;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.bind.CredentialsBinder;

/**
 * Custom Binder to populate the Legacy CAS Credentials with the required
 * ServletRequest.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LegacyCasCredentialsBinder implements CredentialsBinder {

    public void bind(final HttpServletRequest request,
        final Credentials credentials) {
        if (credentials.getClass().equals(LegacyCasCredentials.class)) {
            ((LegacyCasCredentials) credentials).setServletRequest(request);
        } else {
            ((LegacyCasTrustedCredentials) credentials)
                .setServletRequest(request);
        }
    }

    public boolean supports(final Class<?> clazz) {
        return !(clazz == null)
            && (clazz.equals(LegacyCasCredentials.class) || clazz
                .equals(LegacyCasTrustedCredentials.class));
    }

}