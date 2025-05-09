package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import java.util.Set;

/**
 * This is {@link AttributeRepositoryQuery}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */

@SuperBuilder
@With
@Getter
@RequiredArgsConstructor
public class AttributeRepositoryQuery {
    private final AuthenticationHandler authenticationHandler;
    private final Principal principal;
    private final Service service;
    private final Set<String> activeRepositoryIds;
    private final RegisteredService registeredService;
    private final String tenant;
}
