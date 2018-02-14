package org.apereo.cas.audit;

/**
 * This is {@link BaseAuditableExecution}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class BaseAuditableExecution implements AuditableExecution {

    /**
     * Gets parameter.
     *
     * @param <T>        the type parameter
     * @param parameters the parameters
     * @param index      the index
     * @param clazz      the clazz
     * @return the parameter
     */
    protected <T> T getParameter(final Object[] parameters, final int index, final Class<T> clazz) {
        try {
            final Object param = parameters[index];
            return clazz.cast(param);
        } catch (final Exception e) {
            throw new RuntimeException("Unable to extract parameter to execute auditable action: " + e.getMessage(), e);
        }
    }
}
