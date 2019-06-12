package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A deny rule to refuse all service from receiving attributes, whether default or not.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attributes,
                                                     final RegisteredService registeredService, final Service selectedService) {
        LOGGER.trace("Ignoring all attributes given the service is designed to never receive any.");
        return new HashMap<>(0);
    }

    @Override
    public boolean isExcludeDefaultAttributes() {
        return true;
    }

    @Override
    public boolean isAuthorizedToReleaseCredentialPassword() {
        LOGGER.trace("CAS will not authorize the release of credential password, given the service is denied access to all attributes.");
        return false;
    }

    @Override
    public boolean isAuthorizedToReleaseProxyGrantingTicket() {
        LOGGER.trace("CAS will not authorize the release of proxy-granting tickets, given the service is denied access to all attributes.");
        return false;
    }

    @Override
    public boolean isAuthorizedToReleaseAuthenticationAttributes() {
        LOGGER.trace("CAS will not authorize the release of authentication attributes, given the service is denied access to all attributes.");
        return false;
    }

    @Override
    protected Map<String, List<Object>> returnFinalAttributesCollection(final Map<String, List<Object>> attributesToRelease, final RegisteredService service) {
        LOGGER.info("CAS will not authorize anything for release, given the service is denied access to all attributes. "
            + "If there are any default attributes set to be released to all services, "
            + "those are also skipped for [{}]", service);
        return new HashMap<>(0);
    }
}
