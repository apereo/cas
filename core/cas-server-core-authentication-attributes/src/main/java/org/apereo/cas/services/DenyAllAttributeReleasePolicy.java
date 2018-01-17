package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * A deny rule to refuse all service from receiving attributes, whether default or not.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class DenyAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6215588543966639050L;

    public DenyAllAttributeReleasePolicy() {
        setExcludeDefaultAttributes(true);
        setPrincipalIdAttribute(null);
        setAuthorizedToReleaseAuthenticationAttributes(false);
        setAuthorizedToReleaseCredentialPassword(false);
        setAuthorizedToReleaseProxyGrantingTicket(false);
    }

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attributes, final RegisteredService service) {
        LOGGER.debug("Ignoring all attributes given the service is designed to never receive any.");
        return new HashMap<>(0);
    }

    @Override
    public boolean isExcludeDefaultAttributes() {
        return true;
    }

    @Override
    public boolean isAuthorizedToReleaseCredentialPassword() {
        LOGGER.debug("CAS will not authorize the release of credential password, given the service is denied access to all attributes.");
        return false;
    }

    @Override
    public boolean isAuthorizedToReleaseProxyGrantingTicket() {
        LOGGER.debug("CAS will not authorize the release of proxy-granting tickets, given the service is denied access to all attributes.");
        return false;
    }

    @Override
    public boolean isAuthorizedToReleaseAuthenticationAttributes() {
        LOGGER.debug("CAS will not authorize the release of authentication attributes, given the service is denied access to all attributes.");
        return false;
    }

    @Override
    protected Map<String, Object> returnFinalAttributesCollection(final Map<String, Object> attributesToRelease, final RegisteredService service) {
        LOGGER.info("CAS will not authorize anything for release, given the service is denied access to all attributes. "
            + "If there are any default attributes set to be released to all services, "
            + "those are also skipped for [{}]", service);
        return new HashMap<>(0);
    }
}
