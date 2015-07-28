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
package org.jasig.cas.services;

import java.io.Serializable;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;

/**
 * The release policy that decides how attributes are to be released for a given service.
 * Each policy has the ability to apply an optional filter.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface AttributeReleasePolicy extends Serializable {

    /**
     * Is authorized to release credential password?
     *
     * @return the boolean
     */
    boolean isAuthorizedToReleaseCredentialPassword();

    /**
     * Is authorized to release proxy granting ticket?
     *
     * @return the boolean
     */
    boolean isAuthorizedToReleaseProxyGrantingTicket();

    /**
     * Sets the attribute filter.
     *
     * @param filter the new attribute filter
     */
    void setAttributeFilter(AttributeFilter filter);
    
    /**
     * Gets the attributes, having applied the filter.
     *
     * @param p the principal that contains the resolved attributes
     * @return the attributes
     */
    Map<String, Object> getAttributes(Principal p);
}
