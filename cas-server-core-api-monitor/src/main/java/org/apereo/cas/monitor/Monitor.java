package org.apereo.cas.monitor;

/**
 * A monitor observes a resource and reports its status.
 *
 * @author Marvin S. Addison
 * @param <S> the generic type of the monitor
 * @since 3.5.0
 */
public interface Monitor<S extends Status> {

    /**
     * Gets the name of the monitor.
     *
     * @return Monitor name.
     */
    String getName();


    /**
     * Observes the monitored resource and reports the status.
     *
     * @return Status of monitored resource.
     */
    S observe();
}
