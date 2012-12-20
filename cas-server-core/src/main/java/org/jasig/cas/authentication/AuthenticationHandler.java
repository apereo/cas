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

/**
 * An authentication handler authenticates a credential against a single authentication source.
 *
 * @author Marvin S. Addison
 * @author Scott Battaglia
 * @since 4.0
 * @see LegacyAuthenticationHandlerAdapter
 */
public interface AuthenticationHandler {
    /**
     * Determines whether the given credential is authentic.
     *
     * @param credential The credential to authenticate.
     *
     * @return A handler result successful authentication that contains information from the authentication source.
     * The most common information is an optional resolved {@link Principal} for
     * the credential and warnings issued by the source, for example, password expiration warnings.
     *
     * @throws GeneralSecurityException On authentication failures where the root cause is security related,
     * e.g. invalid credential.
     * @throws IOException On authentication failures caused by IO errors (e.g. socket errors).
     */
    HandlerResult authenticate(Credential credential) throws GeneralSecurityException, IOException;


    /**
     * Determines whether the handler has the capability to authenticate the given credential.
     * provided. It may be a simple check such as object type comparison or something more complicated such as scanning
     * the information contained in the given credential.
     *
     * @param credential The credential to check.
     *
     * @return True if the handler supports the Credential, false otherwise.
     */
    boolean supports(Credential credential);


    /**
     * Gets a unique name for this authentication handler within the Spring context that contains it.
     *
     * @return Unique name within a Spring context.
     */
    String getName();
}
