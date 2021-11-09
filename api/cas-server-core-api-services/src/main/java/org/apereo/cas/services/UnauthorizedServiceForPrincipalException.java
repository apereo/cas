package org.apereo.cas.services;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * This is {@link UnauthorizedServiceForPrincipalException}
 * thrown when an attribute is missing from principal
 * attribute release policy that would otherwise grant access
 * to the service that is requesting authentication.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Getter
public class UnauthorizedServiceForPrincipalException extends UnauthorizedServiceException {

    private static final long serialVersionUID = 8909291297815558561L;

    /**
     * The code description.
     */
    private static final String CODE = "service.not.authorized.missing.attr";

    private final RegisteredService registeredService;

    private final String principalId;

    private final Map<String, List<Object>> attributes;

    public UnauthorizedServiceForPrincipalException(final String message, final RegisteredService registeredService,
                                                    final String principalId, final Map<String, List<Object>> attributes) {
        super(CODE, message);
        this.registeredService = registeredService;
        this.principalId = principalId;
        this.attributes = attributes;
    }
}
