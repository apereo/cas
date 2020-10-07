package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.shibboleth.utilities.java.support.resolver.Criterion;

/**
 * This is {@link SamlIdPSamlRegisteredServiceCriterion}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class SamlIdPSamlRegisteredServiceCriterion implements Criterion {
    private final SamlRegisteredService registeredService;
}
