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
package org.jasig.cas.web.bind;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.Credential;

/**
 * Interface for a class that can bind items stored in the request to a
 * particular credential implementation. This allows for binding beyond the
 * basic JavaBean/Request parameter binding that is handled by Spring
 * automatically. Implementations are free to pass part or all of the
 * HttpServletRequest to the Credential.
 *
 * @author Scott Battaglia

 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 *
 * @deprecated Future versions of CAS will provide a mechanism to gain access to standard items from the Request object.
 */
@Deprecated
public interface CredentialsBinder {

    /**
     * Method to allow manually binding attributes from the request object to
     * properties of the credential. Useful when there is no mapping of
     * attribute to property for the usual Spring binding to handle.
     *
     * @param request The HttpServletRequest from which we wish to bind
     * credential to
     * @param credential The credential we will be doing custom binding to.
     */
    void bind(HttpServletRequest request, Credential credential);

    /**
     * Method to determine if a CredentialsBinder supports a specific class or
     * not.
     *
     * @param clazz The class to determine is supported or not
     * @return true if this class is supported by the CredentialsBinder, false
     * otherwise.
     */
    boolean supports(Class<?> clazz);
}
