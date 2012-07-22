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

import javax.naming.directory.DirContext;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.factory.PoolingContextSource;

/**
 * LDAP pool monitor that observes a pool of LDAP connections provided by {@link PoolingContextSource}.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class PoolingContextSourceMonitor extends AbstractPoolMonitor {
    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Pool to observe. */
    @NotNull
    private final PoolingContextSource poolingContextSource;


    public PoolingContextSourceMonitor(final PoolingContextSource pool) {
        this.poolingContextSource = pool;
    }


    /** {@inheritDoc} */
    protected StatusCode checkPool() throws Exception {
        final boolean success;
        DirContext ctxt = null;
        try {
            ctxt = poolingContextSource.getReadOnlyContext();
            success = poolingContextSource.getDirContextValidator().validateDirContext(DirContextType.READ_ONLY, ctxt);
        } finally {
            if (ctxt != null) {
                ctxt.close();
            }
        }
        return success ? StatusCode.OK : StatusCode.ERROR;
    }


    /** {@inheritDoc} */
    protected int getIdleCount() {
        return poolingContextSource.getNumIdle();
    }


    /** {@inheritDoc} */
    protected int getActiveCount() {
        return poolingContextSource.getNumActive();
    }
}
