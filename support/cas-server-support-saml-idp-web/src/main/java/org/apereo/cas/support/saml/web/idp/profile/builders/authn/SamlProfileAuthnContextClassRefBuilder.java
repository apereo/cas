package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;

/**
 * This is {@link SamlProfileAuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SamlProfileAuthnContextClassRefBuilder {

    /**
     * Gets authentication method from assertion.
     *
     * @param context the context
     * @return the authentication method from assertion
     * @throws Exception the exception
     */
    String build(SamlProfileBuilderContext context) throws Exception;
}
