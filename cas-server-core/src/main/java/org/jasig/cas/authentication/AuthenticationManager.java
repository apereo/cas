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

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * The AuthenticationManager class is the entity that determines the
 * authenticity of the credentials provided. It (or a class it delegates to) is
 * the sole authority on whether credentials are valid or not.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface AuthenticationManager {
    
    String AUTHENTICATION_METHOD_ATTRIBUTE = "authenticationMethod";

    /**
     * Method to validate the credentials provided. On successful validation, a
     * fully populated Authentication object will be returned. Typically this
     * will involve resolving a principal and providing any additional
     * attributes, but specifics are left to the individual implementations to
     * determine. Failure to authenticate is considered an exceptional case, and
     * an AuthenticationException is thrown.
     * 
     * @param credentials The credentials provided for authentication.
     * @return fully populated Authentication object.
     * @throws AuthenticationException if unable to determine validity of
     * credentials or there is an extenuating circumstance related to
     * credentials (i.e. Account locked).
     */
    Authentication authenticate(final Credentials credentials)
        throws AuthenticationException;
}
