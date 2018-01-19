package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;

/**
 * Simple container for holding a service principal and its corresponding registered service.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class ServiceContext {

    /** Service principal. */
    private final Service service;

    /** Registered service corresponding to service principal. */
    private final RegisteredService registeredService;

}
