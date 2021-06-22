package org.apereo.cas.support.saml.web.idp.delegation;

import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2ConfigurationContext;

import java.util.stream.Collectors;

/**
 * This is {@link SamlIdPDelegatedClientAuthenticationRequestCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class SamlIdPDelegatedClientAuthenticationRequestCustomizer implements DelegatedClientAuthenticationRequestCustomizer {
    private final SessionStore sessionStore;

    private final OpenSamlConfigBean openSamlConfigBean;

    @Override
    public void customize(final IndirectClient client, final WebContext webContext) {
        val context = SamlIdPUtils.retrieveSamlRequest(webContext, sessionStore, openSamlConfigBean, AuthnRequest.class);
        val authnRequest = (AuthnRequest) context.getLeft();
        LOGGER.debug("Retrieved the SAML2 authentication request from [{}]", SamlIdPUtils.getIssuerFromSamlObject(authnRequest));
        if (authnRequest.isForceAuthn()) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
        }
        if (authnRequest.isPassive()) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true);
        }
        val requestedAuthnContext = authnRequest.getRequestedAuthnContext();
        if (requestedAuthnContext != null && requestedAuthnContext.getAuthnContextClassRefs() != null
            && !requestedAuthnContext.getAuthnContextClassRefs().isEmpty()) {
            val refs = requestedAuthnContext.getAuthnContextClassRefs().stream()
                .map(XSURI::getURI)
                .collect(Collectors.toList());
            webContext.setRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_AUTHN_CONTEXT_CLASS_REFS, refs);
            webContext.setRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_COMPARISON_TYPE, requestedAuthnContext.getComparison().name());
        }
    }

    @Override
    public boolean supports(final IndirectClient client, final WebContext webContext) {
        return client instanceof SAML2Client;
    }
}
