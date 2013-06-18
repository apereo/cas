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

/**
 * Sets an authentication attribute containing the collection of authentication handlers (by name) that successfully
 * authenticated credential. The attribute name is given by {@link #SUCCESSFUL_AUTHENTICATION_HANDLERS}.
 * This component provides a simple method to inject successful handlers into the CAS ticket validation
 * response to support level of assurance and MFA use cases.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class SuccessfulHandlerMetaDataPopulator implements AuthenticationMetaDataPopulator {
    /** Attribute name containing collection of handler names that successfully authenticated credential. */
    public static final String SUCCESSFUL_AUTHENTICATION_HANDLERS = "successfulAuthenticationHandlers";

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        builder.addAttribute(SUCCESSFUL_AUTHENTICATION_HANDLERS, builder.getSuccesses().keySet());
    }
}
