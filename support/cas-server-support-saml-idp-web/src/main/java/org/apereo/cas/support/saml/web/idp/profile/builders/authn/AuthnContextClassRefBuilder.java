package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;

/**
 * This is {@link AuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface AuthnContextClassRefBuilder {

    /**
     * Gets authentication method from assertion.
     *
     * @param context the context
     * @return the authentication method from assertion
     */
    String build(SamlProfileBuilderContext context);
}
