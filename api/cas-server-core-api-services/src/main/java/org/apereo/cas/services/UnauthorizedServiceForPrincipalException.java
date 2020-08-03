package org.apereo.cas.services;

import lombok.Getter;

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

    private final Map<String, Object> attributes;

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message           the message
     * @param registeredService the registered service
     * @param principalId       the principal id
     * @param attributes        the attributes
     */
    public UnauthorizedServiceForPrincipalException(final String message, final RegisteredService registeredService,
                                                    final String principalId, final Map<String, Object> attributes) {
        super(CODE, message);
        this.registeredService = registeredService;
        this.principalId = principalId;
        this.attributes = attributes;
    }
}
