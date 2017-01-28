package org.apereo.cas.monitor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all monitors that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public abstract class AbstractNamedMonitor<S extends Status> implements Monitor<S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNamedMonitor.class);
    
    /** Monitor name. */
    protected final String name;

    public AbstractNamedMonitor(final String name) {
        this.name = name;
    }

    /**
     * @return Monitor name.
     */
    @Override
    public String getName() {
        return StringUtils.defaultIfEmpty(this.name, getClass().getSimpleName());
    }
}
