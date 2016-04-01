package org.jasig.cas.monitor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Base class for all monitors that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public abstract class AbstractNamedMonitor<S extends Status> implements Monitor<S> {

    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Monitor name. */
    protected String name;

    /**
     * @return Monitor name.
     */
    @Override
    public String getName() {
        return StringUtils.defaultIfEmpty(this.name, getClass().getSimpleName());
    }

    /**
     * @param n Monitor name.
     */
    public void setName(final String n) {
        Assert.hasText(n, "Monitor name cannot be null or empty.");
        this.name = n;
    }
}
