package org.apereo.cas.audit;

import java.util.Arrays;

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

    /**
     * Gets unique parameter that is only found once in the array of parameters.
     *
     * @param <T>        the type parameter
     * @param parameters the parameters
     * @param clazz      the clazz
     * @return the unique parameter
     */
    protected <T> T getUniqueParameter(final Object[] parameters, final Class<T> clazz) {
        try {
            final Object result = Arrays.stream(parameters)
                .filter(r -> clazz.isAssignableFrom(r.getClass()))
                .findFirst()
                .orElse(null);
            if (result != null) {
                return clazz.cast(result);
            }
            return null;
        } catch (final Exception e) {
            throw new RuntimeException("Unable to extract parameter to execute auditable action: " + e.getMessage(), e);
        }
    }
}
