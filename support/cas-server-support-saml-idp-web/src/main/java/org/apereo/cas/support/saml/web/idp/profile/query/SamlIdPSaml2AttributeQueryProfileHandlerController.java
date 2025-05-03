package org.apereo.cas.support.saml.web.idp.profile.query;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SamlIdPSaml2AttributeQueryProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SamlIdPSaml2AttributeQueryProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public SamlIdPSaml2AttributeQueryProfileHandlerController(final SamlProfileHandlerConfigurationContext context) {
        super(context);
    }

    /**
     * Handle post request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY)
    protected void handlePostRequest(final HttpServletResponse response,
                                     final HttpServletRequest request) throws Exception {
        val enabled = configurationContext.getCasProperties().getAuthn().getSamlIdp().getCore().isAttributeQueryProfileEnabled();
        if (!enabled) {
            LOGGER.warn("SAML2 attribute query profile is not enabled");
            response.setStatus(HttpStatus.SC_NOT_IMPLEMENTED);
            return;
        }

        val ctx = decodeSoapRequest(request);
        val query = (AttributeQuery) ctx.getMessage();
        try {
            val issuer = Objects.requireNonNull(query).getIssuer().getValue();
            val registeredService = verifySamlRegisteredService(issuer, request);
            val adaptor = getSamlMetadataFacadeFor(registeredService, query);
            val facade = adaptor.orElseThrow(() -> UnauthorizedServiceException.denied("Cannot find metadata linked to %s".formatted(issuer)));
            verifyAuthenticationContextSignature(ctx, request, query, facade, registeredService);

            val nameIdValue = determineNameIdForQuery(query, registeredService, facade);
            val factory = (SamlAttributeQueryTicketFactory) getConfigurationContext().getTicketFactory()
                .get(SamlAttributeQueryTicket.class);
            val id = factory.createTicketIdFor(nameIdValue, facade.getEntityId());
            LOGGER.debug("Created ticket id for attribute query [{}]", id);
            val ticket = getConfigurationContext().getTicketRegistry().getTicket(id, SamlAttributeQueryTicket.class);
            if (ticket == null || ticket.isExpired()) {
                LOGGER.warn("Attribute query ticket [{}] has either expired, or it is linked to "
                            + "a single sign-on session that is no longer valid and has now expired", id);
                throw new InvalidTicketException(id);
            }
            val authentication = ticket.getAuthentication();

            val principal = resolvePrincipalForAttributeQuery(authentication, registeredService);
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .applicationContext(getConfigurationContext().getOpenSamlConfigBean().getApplicationContext())
                .service(ticket.getService())
                .principal(principal)
                .build();

            val principalAttributes = registeredService.getAttributeReleasePolicy().getConsentableAttributes(releasePolicyContext);
            LOGGER.debug("Initial consentable principal attributes are [{}]", principalAttributes);

            val authenticationAttributes = getConfigurationContext().getAuthenticationAttributeReleasePolicy()
                .getAuthenticationAttributesForRelease(authentication, null, Map.of(), registeredService);
            val finalAttributes = CollectionUtils.merge(principalAttributes, authenticationAttributes);

            val usernameContext = RegisteredServiceUsernameProviderContext.builder()
                .registeredService(registeredService)
                .service(ticket.getService())
                .principal(authentication.getPrincipal())
                .applicationContext(getConfigurationContext().getOpenSamlConfigBean().getApplicationContext())
                .build();
            
            val principalId = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);
            LOGGER.debug("Principal id used for attribute query response should be [{}]", principalId);
            LOGGER.debug("Final attributes to be processed for the SAML2 response are [{}]", finalAttributes);

            val casAssertion = buildCasAssertion(principalId, registeredService, finalAttributes);
            request.setAttribute(AttributeQuery.class.getSimpleName(), query);

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(query)
                .httpRequest(request)
                .httpResponse(response)
                .authenticatedAssertion(Optional.of(casAssertion))
                .registeredService(registeredService)
                .adaptor(facade)
                .binding(SAMLConstants.SAML2_SOAP11_BINDING_URI)
                .messageContext(ctx)
                .build();
            getConfigurationContext().getResponseBuilder().build(buildContext);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR,
                "Unable to build SOAP response: " + StringUtils.defaultString(e.getMessage()));
            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(query)
                .httpRequest(request)
                .httpResponse(response)
                .binding(SAMLConstants.SAML2_SOAP11_BINDING_URI)
                .messageContext(ctx)
                .build();
            getConfigurationContext().getSamlFaultResponseBuilder().build(buildContext);
        }
    }

    private Principal resolvePrincipalForAttributeQuery(final Authentication authentication,
                                                        final RegisteredService registeredService) throws Throwable {
        val repositories = new HashSet<String>();
        if (registeredService != null) {
            repositories.addAll(registeredService.getAttributeReleasePolicy()
                .getPrincipalAttributesRepository().getAttributeRepositoryIds());
        }

        val principal = authentication.getPrincipal();
        val attributes = PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(getConfigurationContext().getAttributeRepository())
            .principalId(principal.getId())
            .activeAttributeRepositoryIdentifiers(repositories)
            .currentPrincipal(principal)
            .build()
            .retrieve();
        LOGGER.debug("Attributes retrieved from attribute repositories are [{}]", attributes);
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(principal.getId(), attributes);
    }

    private String determineNameIdForQuery(final AttributeQuery query,
                                           final SamlRegisteredService registeredService,
                                           final SamlRegisteredServiceMetadataAdaptor facade) {
        return query.getSubject().getNameID() == null
            ? getConfigurationContext().getSamlObjectEncrypter().decode(
            query.getSubject().getEncryptedID(), registeredService, facade).getValue()
            : query.getSubject().getNameID().getValue();

    }
}
