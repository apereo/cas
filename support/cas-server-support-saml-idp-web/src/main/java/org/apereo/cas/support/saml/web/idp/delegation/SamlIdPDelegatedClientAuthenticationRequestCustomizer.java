package org.apereo.cas.support.saml.web.idp.delegation;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2ConfigurationContext;

import java.util.List;
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
        val authnRequest = (AuthnRequest) context
            .orElseThrow(() -> new IllegalArgumentException("SAML request could not be determined from session store"))
            .getLeft();
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
    public boolean isAuthorized(final JEEContext webContext, final IndirectClient client,
                                final WebApplicationService currentService) {
        val result = SamlIdPUtils.retrieveSamlRequest(webContext, sessionStore, openSamlConfigBean, AuthnRequest.class);
        if (result.isEmpty()) {
            LOGGER.trace("No SAML2 authentication request found in session store");
            return true;
        }

        val authnRequest = (AuthnRequest) result.get().getLeft();
        LOGGER.trace("Retrieved the SAML2 authentication request from [{}]", SamlIdPUtils.getIssuerFromSamlObject(authnRequest));

        val idpList = authnRequest.getScoping() != null ? authnRequest.getScoping().getIDPList() : null;
        val idpEntries = idpList != null && idpList.getIDPEntrys() != null ? idpList.getIDPEntrys() : List.<IDPEntry>of();
        val providerList = idpEntries.stream().map(IDPEntry::getProviderID).collect(Collectors.toList());

        LOGGER.debug("Scoped identity providers are [{}] to examine against client [{}]", providerList, client.getName());
        if (supports(client, webContext)) {
            val saml2Client = (SAML2Client) client;
            LOGGER.debug("Comparing [{}] against scoped identity providers [{}]",
                saml2Client.getIdentityProviderResolvedEntityId(), providerList);
            return providerList.isEmpty() || providerList.contains(saml2Client.getIdentityProviderResolvedEntityId());
        }
        return true;
    }

    @Override
    public boolean supports(final IndirectClient client, final WebContext webContext) {
        return client instanceof SAML2Client;
    }
}
