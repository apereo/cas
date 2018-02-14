package org.apereo.cas.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link AuditableExecutionResult}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AuditableExecutionResult {
    private RegisteredService registeredService;
    private Service service;
    private Authentication authentication;
    private RuntimeException exception;
    private Map<String, Object> properties = new TreeMap<>();

    public boolean isExecutionFailure() {
        return exception != null;
    }

    /**
     * Throw exception if needed.
     */
    public void throwExceptionIfNeeded() {
        if (isExecutionFailure()) {
            throw this.exception;
        }
    }

    /**
     * Factory method to create a result.
     *
     * @param e                 the exception
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final RuntimeException e, final Authentication authentication,
                                              final Service service, final RegisteredService registeredService) {
        final AuditableExecutionResult result = new AuditableExecutionResult();
        result.setAuthentication(authentication);
        result.setException(e);
        result.setRegisteredService(registeredService);
        result.setService(service);
        return result;
    }

    /**
     * Factory method to create a result.
     *
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final Authentication authentication,
                                              final Service service, final RegisteredService registeredService) {
        return of(null, authentication, service, registeredService);
    }
}
