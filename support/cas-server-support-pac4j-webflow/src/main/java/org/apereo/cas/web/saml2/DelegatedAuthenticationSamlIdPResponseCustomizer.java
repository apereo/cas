package org.apereo.cas.web.saml2;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthenticatingAuthority;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.saml.client.SAML2Client;

/**
 * This is {@link DelegatedAuthenticationSamlIdPResponseCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedAuthenticationSamlIdPResponseCustomizer implements SamlIdPResponseCustomizer {
    private final DelegatedIdentityProviders identityProviders;

    @Override
    public void customizeAssertion(final SamlProfileBuilderContext context, final Saml20ObjectBuilder builder, final Assertion assertion) {
        val attributes = context.getAuthenticatedAssertion().orElseThrow().getAttributes();
        LOGGER.debug("Attributes to evaluate to customize SAML2 assertion are [{}]", attributes);
        if (attributes.containsKey(Pac4jConstants.CLIENT_NAME)) {
            val clientNames = CollectionUtils.toCollection(attributes.get(Pac4jConstants.CLIENT_NAME));
            clientNames.forEach(clientName -> identityProviders.findClient(clientName.toString())
                .filter(SAML2Client.class::isInstance)
                .map(SAML2Client.class::cast)
                .ifPresent(client -> assertion.getAuthnStatements().forEach(authnStatement -> {
                    val authnContext = authnStatement.getAuthnContext();
                    val authority = builder.newSamlObject(AuthenticatingAuthority.class);
                    authority.setURI(client.getIdentityProviderResolvedEntityId());
                    LOGGER.debug("Customizing SAML2 assertion to include authenticating authority [{}] linked to delegated client [{}]",
                        client.getIdentityProviderResolvedEntityId(), clientName);
                    authnContext.getAuthenticatingAuthorities().add(authority);
                })));
        }
    }
}
