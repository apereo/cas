package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;

import java.util.Map;
import java.util.Set;

/**
 * This is a strategy interface which acts as a façade to be used by upstream components that need to call into registered service
 * access strategy APIs.
 *
 * Implementors of this API are used as Spring-managed beans injected into their consumers thus eligible
 * for Spring container services like AOP proxies for audits, transactions, etc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public interface RegisteredServiceAccessStrategyEnforcer {

    /**
     * Enforce service access strategy with given components.
     *
     * @param service the target service
     * @param registeredService descriptor for the target service
     * @param authentication current successful authentication with principal trying to gain access to the target service
     * @param retrievePrincipalAttributesFromReleasePolicy flag to control principal attributes source strategy
     * @return ServiceAccessCheckResult the result of the strategy enforcement operation. Note that upstream components using this API
     *         would be responsible for checking and re-throwing {@link PrincipalException} encapsulated in this result, for upper layers
     *         to deal with.
     */
    ServiceAccessCheckResult enforceServiceAccessStrategy(Service service,
                                                          RegisteredService registeredService,
                                                          Authentication authentication,
                                                          boolean retrievePrincipalAttributesFromReleasePolicy);



    /**
     * Immutable class to encapsulate result of service access check.
     */
    class ServiceAccessCheckResult {
        private final PrincipalException principalException;
        private final String principalId;
        private final Map<String, Object> principalAttributes;
        private final String serviceId;
        private final RegisteredService registeredService;

        public ServiceAccessCheckResult(final PrincipalException principalException,
                                        final String principalId,
                                        final Map<String, Object> principalAttributes,
                                        final String serviceId,
                                        final RegisteredService registeredService) {

            this.principalException = principalException;
            this.principalId = principalId;
            this.principalAttributes = principalAttributes;
            this.serviceId = serviceId;
            this.registeredService = registeredService;
        }

        public String getServiceId() {
            return serviceId;
        }

        public Map<String, Object> getPrincipalAttributes() {
            return principalAttributes;
        }

        public Map<String, Set<String>> getServiceAccessRequiredAttributes() {
            return registeredService.getAccessStrategy().getRequiredAttributes();
        }

        /**
         * Is access to the target service denied?
         *
         * @return true if denied, false otherwise
         */
        public boolean accessDenied() {
            return principalException != null;
        }

        /**
         * Re-throw encapsulated {@link PrincipalException} if occurred, or noop.
         */
        public void reThrowAccessDeniedTargetExceptionIfOccured() {
            if (principalException != null) {
                throw principalException;
            }
        }
    }
}
