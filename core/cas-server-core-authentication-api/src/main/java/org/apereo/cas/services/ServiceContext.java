package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Simple container for holding a service principal and its corresponding registered service.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class ServiceContext {

    /**
     * Service principal.
     */
    private final Service service;

    /**
     * Registered service corresponding to service principal.
     */
    private final RegisteredService registeredService;

}
