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
public record ServiceContext(Service service, RegisteredService registeredService) {
}
