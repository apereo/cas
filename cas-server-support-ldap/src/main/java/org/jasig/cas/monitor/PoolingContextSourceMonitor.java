/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.pool.DirContextType;
import org.springframework.ldap.pool.factory.PoolingContextSource;

import javax.naming.directory.DirContext;
import javax.validation.constraints.NotNull;

/**
 * LDAP pool monitor that observes a pool of LDAP connections provided by {@link PoolingContextSource}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class PoolingContextSourceMonitor extends AbstractPoolMonitor {
    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Pool to observe. */
    @NotNull
    private PoolingContextSource poolingContextSource;


    /**
     * Sets the pool to observe.
     *
     * @param pool Pool to observe.
     */
    public void setPoolingContextSource(final PoolingContextSource pool) {
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
