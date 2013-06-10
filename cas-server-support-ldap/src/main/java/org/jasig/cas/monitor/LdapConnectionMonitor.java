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

import org.jasig.cas.util.LdapUtils;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.pool.Validator;

/**
 * Monitor that observes a {@link ConnectionFactory}
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class LdapConnectionMonitor extends AbstractNamedMonitor<Status> {

    private ConnectionFactory factory = null;
    private Validator<Connection> validator = null;
    /**
     * Creates a new monitor that observes the given LDAP context source.
     *
     */
    public LdapConnectionMonitor(@NotNull final ConnectionFactory factory,@NotNull final Validator<Connection> validator) {
        this.factory = factory;
        this.validator = validator;
    }

    @Override
    public Status observe() {
        Connection c = null;
        try {
            c = factory.getConnection();
            c.open();
            return validator.validate(c) ? Status.OK : Status.ERROR;
        } catch (final LdapException e) {
            log.debug(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(c);
        }
        return Status.ERROR;
    }
}
