package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * @throws AbstractTicketException the abstract ticket exception
     */
    ValidationResult validate(String ticket, String service) throws AbstractTicketException;

    @SuperBuilder
    @Getter
    class ValidationResult implements Serializable {
        private static final long serialVersionUID = 8115764183802826474L;

        private final Principal principal;

        private final Service service;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new LinkedHashMap<>();

        @Builder.Default
        private final Map<String, Serializable> context = new LinkedHashMap<>();
    }
}
