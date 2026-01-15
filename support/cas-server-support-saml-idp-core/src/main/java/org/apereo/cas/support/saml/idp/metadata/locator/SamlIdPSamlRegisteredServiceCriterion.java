package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import net.shibboleth.shared.resolver.Criterion;


/**
 * This is {@link SamlIdPSamlRegisteredServiceCriterion}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public record SamlIdPSamlRegisteredServiceCriterion(SamlRegisteredService registeredService) implements Criterion {
}
