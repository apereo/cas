package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

/**
 * This is {@link RegisteredServiceAccessStrategyActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAccessStrategyActivationCriteria extends Serializable, Ordered {
    /**
     * Never activate criteria.
     *
     * @return the registered service access strategy activation criteria
     */
    static RegisteredServiceAccessStrategyActivationCriteria never() {
        return request -> false;
    }

    /**
     * Always activate criteria.
     *
     * @return the registered service access strategy activation criteria
     */
    static RegisteredServiceAccessStrategyActivationCriteria always() {
        return request -> true;
    }

    /**
     * Should activate policy based on this request?
     *
     * @param request the request
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean shouldActivate(RegisteredServiceAccessStrategyRequest request) throws Throwable;

    /**
     * Gets deactivation status.
     *
     * @return the deactivation status
     */
    default boolean isAllowIfInactive() {
        return true;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
