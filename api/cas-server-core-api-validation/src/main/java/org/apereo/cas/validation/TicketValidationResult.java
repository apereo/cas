package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link TicketValidationResult}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SuperBuilder
@Getter
public class TicketValidationResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 8115764183802826474L;

    private final Principal principal;

    private final Service service;

    @Builder.Default
    private final Map<String, List<Object>> attributes = new LinkedHashMap<>();

    private final Assertion assertion;

    private final RegisteredService registeredService;
}
