package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

/**
 * This is {@link EchoingPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
public class EchoingPrincipalResolver implements PrincipalResolver {
    @Setter(AccessLevel.PROTECTED)
    private PersonAttributeDao attributeRepository;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> principal,
                             final Optional<AuthenticationHandler> handler, final Optional<Service> service) throws Throwable {
        LOGGER.debug("Echoing back the authenticated principal [{}]", principal);
        return principal.orElse(null);
    }

    @Override
    public boolean supports(final Credential credential) {
        return StringUtils.isNotBlank(credential.getId());
    }
}
