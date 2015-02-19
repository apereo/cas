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
package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.services.RegisteredService;
import org.ldaptive.LdapEntry;

/**
 * Strategy interface to define operations required when mapping LDAP
 * entries to registered services and vice versa.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @see DefaultLdapRegisteredServiceMapper
 * @since 3.0.0
 */
public interface LdapRegisteredServiceMapper {

    /**
     * Map to registered service from ldap.
     *
     * @param result the result
     * @return the registered service
     */
    RegisteredService mapToRegisteredService(final LdapEntry result);

    /**
     * Map from registered service to ldap.
     *
     * @param dn the dn
     * @param svc the svc
     * @return the ldap entry
     */
    LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc);

    /**
     * Gets the dn for registered service.
     *
     * @param parentDn the parent dn
     * @param svc the svc
     * @return the dn for registered service
     */
    String getDnForRegisteredService(String parentDn, RegisteredService svc);

    /**
     * Gets the name of the LDAP object class that represents service registry entries.
     *
     * @return Registered service object class.
     */
    String getObjectClass();

    /**
     * Gets the name of the LDAP attribute that stores the registered service integer unique identifier.
     *
     * @return Registered service unique ID attribute name.
     */
    String getIdAttribute();
}
