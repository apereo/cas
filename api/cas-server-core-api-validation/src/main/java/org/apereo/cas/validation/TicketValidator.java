package org.apereo.cas.validation;
import module java.base;

/**
 * This is {@link TicketValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface TicketValidator {
    /**
     * Validate ticket and return result.
     *
     * @param ticket  the ticket
     * @param service the service
     * @return the validation result
     * @throws Throwable the throwable
     */
    TicketValidationResult validate(String ticket, String service) throws Throwable;
}
