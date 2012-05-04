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
package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.services.RegisteredService;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.filter.Filter;

/**
 * The LdapServiceMapper is responsible for how RegisteredService instances are mapped to LDAP Contexts and vice versa
 *
 * @author Siegfried Puchbauer, SPP (http://www.spp.at/)
 *
 */
public abstract class LdapServiceMapper extends AbstractParameterizedContextMapper<RegisteredService> {

    /**
     * Method inherited from AbstractParameterizedContextMapper&lt;RegisteredService&gt;
     * Should read the attributes from the DirContextAdapter and create a <code>RegisteredService</code>
     *
     * @param ctx the DirContextAdapter to read from
     * @return the created RegisteredServiceImpl instnace
     */
    protected abstract RegisteredService doMapFromContext(DirContextOperations ctx);

    /**
     * This method should map the properties of the RegisteredService to the DirContextAdapter
     *
     * @param service the RegisteredService
     * @param ctx the DirContextAdapter
     * @return the modified DirContextAdapter
     */
    protected abstract DirContextAdapter doMapToContext(RegisteredService service, DirContextAdapter ctx);

    /**
     * Create a new DirContextAdapter (set the naming attribute, sub-path etc.)
     *
     * @param parentDn the base DN for Registered Services
     * @param service the service
     * @return the newly created DirContextAdapter
     */
    protected abstract DirContextAdapter createCtx(String parentDn, RegisteredService service);

    /**
     * This method returns the LDAP Filter for finding registered Services based on the given ID
     *
     * @param id The registered service's ID
     * @return the LDAP Filter as a <code>org.springframework.ldap.filter.Filter</code>
     */
    protected abstract Filter getSearchFilter(Long id);

    /**
     * This method returns the LDAP Filter whichs fetches all Registered Services
     *
     * @return the LDAP Filter as a <code>org.springframework.ldap.filter.Filter</code>
     */
    protected abstract Filter getLoadFilter();
}
