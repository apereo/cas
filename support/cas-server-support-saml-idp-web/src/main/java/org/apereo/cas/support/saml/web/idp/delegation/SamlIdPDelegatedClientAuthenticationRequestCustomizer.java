package org.apereo.cas.support.saml.web.idp.delegation;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2ConfigurationContext;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    @Override
    public void customize(final IndirectClient client, final WebContext webContext) {
        val result = SamlIdPSessionManager.of(openSamlConfigBean, sessionStore)
            .fetch(webContext, AuthnRequest.class)
            .map(Pair::getLeft)
            .map(AuthnRequest.class::cast);
        result.ifPresent(authnRequest -> {
            LOGGER.debug("Retrieved the SAML2 authentication request from [{}]",
                SamlIdPUtils.getIssuerFromSamlObject(authnRequest));
            if (Boolean.TRUE.equals(authnRequest.isForceAuthn())) {
                customizeForceAuthnRequest(client, webContext, authnRequest);
            }
            if (Boolean.TRUE.equals(authnRequest.isPassive())) {
                customizePassiveAuthnRequest(client, webContext);
            }
            customizeAuthnContextClass(client, webContext, authnRequest);
        });
    }

    @Override
    public boolean isAuthorized(final WebContext webContext, final IndirectClient client,
                                final WebApplicationService currentService) {
        val result = SamlIdPSessionManager.of(openSamlConfigBean, sessionStore)
            .fetch(webContext, AuthnRequest.class);
        if (result.isEmpty()) {
            LOGGER.trace("No SAML2 authentication request found in session store");
            return true;
        }

        val authnRequest = (AuthnRequest) result.get().getLeft();
        LOGGER.trace("Retrieved the SAML2 authentication request from [{}]", SamlIdPUtils.getIssuerFromSamlObject(authnRequest));
        val idpList = authnRequest.getScoping() != null ? authnRequest.getScoping().getIDPList() : null;
        val idpEntries = idpList != null && idpList.getIDPEntrys() != null ? idpList.getIDPEntrys() : List.<IDPEntry>of();
        val providerList = idpEntries.stream().map(IDPEntry::getProviderID).collect(Collectors.toSet());
        LOGGER.debug("Scoped identity providers are [{}] to examine against client [{}]", providerList, client.getName());
        if (supports(client, webContext)) {
            val saml2Client = (SAML2Client) client;
            val authorized = providerList.isEmpty() || providerList.contains(getIdentityProviderEntityId(saml2Client));
            if (!authorized) {
                val registeredService = servicesManager.findServiceBy(currentService);
                val delegatedAuthenticationPolicy = registeredService != null ? registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy() : null;
                return delegatedAuthenticationPolicy != null && delegatedAuthenticationPolicy.isProviderAllowed(saml2Client.getName(), registeredService);
            }
        }
        return true;
    }

    @Override
    public boolean supports(final IndirectClient client, final WebContext webContext) {
        return client instanceof SAML2Client;
    }

    protected void customizeAuthnContextClass(final IndirectClient client, final WebContext webContext,
                                              final AuthnRequest authnRequest) {

        val requestedAuthnContext = authnRequest.getRequestedAuthnContext();
        if (requestedAuthnContext != null && requestedAuthnContext.getAuthnContextClassRefs() != null
            && !requestedAuthnContext.getAuthnContextClassRefs().isEmpty()) {
            val authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs()
                .stream()
                .map(XSURI::getURI)
                .toList();

            val definedContexts = CollectionUtils.convertDirectedListToMap(
                casProperties.getAuthn().getSamlIdp().getCore().getContext().getAuthenticationContextClassMappings());
            LOGGER.debug("Defined authentication context mappings are [{}]", definedContexts);
            val mappedMethods = authnContextClassRefs.stream()
                .map(ref -> definedContexts.getOrDefault(ref, ref))
                .map(ref -> new ArrayList<>(StringUtils.commaDelimitedListToSet(ref)))
                .flatMap(List::stream)
                .toList();
            LOGGER.debug("Mapped authentication context classes are [{}]", mappedMethods);
            webContext.setRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_AUTHN_CONTEXT_CLASS_REFS, mappedMethods);
            val comparison = Optional.ofNullable(requestedAuthnContext.getComparison()).orElse(AuthnContextComparisonTypeEnumeration.EXACT);
            webContext.setRequestAttribute(SAML2ConfigurationContext.REQUEST_ATTR_COMPARISON_TYPE, comparison.name());
        }
    }

    protected void customizePassiveAuthnRequest(final IndirectClient client, final WebContext webContext) {
        webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true);
    }

    protected void customizeForceAuthnRequest(final IndirectClient client, final WebContext webContext,
                                              final AuthnRequest authnRequest) {
        webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
    }

    protected String getIdentityProviderEntityId(final SAML2Client saml2Client) {
        saml2Client.init();
        val configuration = saml2Client.getConfiguration();
        var identityProviderEntityId = configuration.getIdentityProviderEntityId();
        if (org.apache.commons.lang3.StringUtils.isBlank(identityProviderEntityId)) {
            val identityProviderMetadataResolver = configuration.getIdentityProviderMetadataResolver();
            identityProviderMetadataResolver.resolve(true);
            identityProviderEntityId = identityProviderMetadataResolver.getEntityId();
        }
        LOGGER.debug("Resolved SAML2 identity provider entity id as [{}]", identityProviderEntityId);
        return identityProviderEntityId;
    }
}
