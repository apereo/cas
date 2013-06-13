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
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;

/**
 * LDAP pool monitor that observes a pool of LDAP connections.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class PoolingLdapConnectionMonitor extends AbstractPoolMonitor {

    /** Pool whose connections to observe. */
    @NotNull
    private final PooledConnectionFactory pooledConnectionFactory;

    /** Provides a validator. */
    @NotNull
    private ConnectionPool connectionPool;

    public PoolingLdapConnectionMonitor(final PooledConnectionFactory pool) {
        this.pooledConnectionFactory = pool;
        this.connectionPool = pool.getConnectionPool();
    }


    /** {@inheritDoc} */
    @Override
    protected StatusCode checkPool() throws Exception {
        Connection c = null;
        try {
            final Validator<Connection> validator = this.connectionPool.getValidator();
            final boolean success = validator.validate(this.pooledConnectionFactory.getConnection());
            return success ? StatusCode.OK : StatusCode.ERROR;
        } finally {
            LdapUtils.closeConnection(c);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected int getIdleCount() {
        return connectionPool.availableCount() - this.getActiveCount();
    }


    /** {@inheritDoc} */
    @Override
    protected int getActiveCount() {
        return connectionPool.activeCount();
    }
}
