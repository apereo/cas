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
package org.jasig.cas.monitor;

import javax.validation.constraints.NotNull;

import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.validation.DirContextValidator;

/**
 * Monitor that observes a {@link org.springframework.ldap.core.support.LdapContextSource}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class ContextSourceMonitor extends AbstractNamedMonitor<Status> {

    @NotNull
    private final LdapContextSource contextSource;

    @NotNull
    private final DirContextValidator dirContextValidator;


    /**
     * Creates a new monitor that observes the given LDAP context source.
     *
     * @param source LDAP context source to observe.
     * @param validator LDAP context validator.
     */
    public ContextSourceMonitor(final LdapContextSource source, final DirContextValidator validator) {
        this.contextSource = source;
        this.dirContextValidator = validator;
    }

    public Status observe() {
        if (dirContextValidator.validateDirContext(DirContextType.READ_ONLY, contextSource.getReadOnlyContext())) {
            return Status.OK;
        }
        return Status.ERROR;
    }
}
