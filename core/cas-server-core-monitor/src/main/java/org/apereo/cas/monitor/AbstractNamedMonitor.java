package org.apereo.cas.monitor;

import org.apache.commons.lang3.StringUtils;

/**
 * Base class for all monitors that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public abstract class AbstractNamedMonitor<S extends Status> implements Monitor<S> {

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
