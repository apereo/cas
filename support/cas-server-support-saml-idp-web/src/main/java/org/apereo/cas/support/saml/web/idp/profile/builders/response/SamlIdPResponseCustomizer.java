package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

/**
 * This is {@link SamlIdPResponseCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface SamlIdPResponseCustomizer {
    /**
     * Customize.
     *
     * @param context      the context
     * @param builder      the builder
     * @param samlResponse the saml response
     */
    default void customizeResponse(final SamlProfileBuilderContext context, final Saml20ObjectBuilder builder, final Response samlResponse) {}

    /**
     * Customize assertion.
     *
     * @param context       the context
     * @param builder       the builder
     * @param samlAssertion the saml assertion
     */
    default void customizeAssertion(final SamlProfileBuilderContext context, final Saml20ObjectBuilder builder, final Assertion samlAssertion) {}
}
