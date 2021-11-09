package org.apereo.cas.support.saml.web.idp.profile.query;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

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
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY)
    protected void handlePostRequest(final HttpServletResponse response,
                                     final HttpServletRequest request) {
        val ctx = decodeSoapRequest(request);
        val query = (AttributeQuery) ctx.getMessage();
        try {
            val issuer = Objects.requireNonNull(query).getIssuer().getValue();
            val registeredService = verifySamlRegisteredService(issuer);
            val adaptor = getSamlMetadataFacadeFor(registeredService, query);
            val facade = adaptor.orElseThrow(() -> new UnauthorizedServiceException(
                UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer));
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
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(ticket.getService())
                .principal(principal)
                .build();

            val principalAttributes = registeredService.getAttributeReleasePolicy().getConsentableAttributes(context);
            LOGGER.debug("Initial consentable principal attributes are [{}]", principalAttributes);

            val authenticationAttributes = getConfigurationContext().getAuthenticationAttributeReleasePolicy()
                .getAuthenticationAttributesForRelease(authentication, null, Map.of(), registeredService);
            val finalAttributes = CollectionUtils.merge(principalAttributes, authenticationAttributes);

            val principalId = registeredService.getUsernameAttributeProvider()
                .resolveUsername(authentication.getPrincipal(), ticket.getService(), registeredService);
            LOGGER.debug("Principal id used for attribute query response should be [{}]", principalId);
            LOGGER.debug("Final attributes to be processed for the SAML2 response are [{}]", finalAttributes);

            val casAssertion = buildCasAssertion(principalId, registeredService, finalAttributes);
            request.setAttribute(AttributeQuery.class.getSimpleName(), query);
            getConfigurationContext().getResponseBuilder().build(query, request, response, casAssertion,
                registeredService, facade, SAMLConstants.SAML2_SOAP11_BINDING_URI, ctx);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR,
                "Unable to build SOAP response: " + StringUtils.defaultString(e.getMessage()));
            getConfigurationContext().getSamlFaultResponseBuilder().build(query, request, response,
                null, null, null, SAMLConstants.SAML2_SOAP11_BINDING_URI, ctx);
        }
    }

    private Principal resolvePrincipalForAttributeQuery(final Authentication authentication,
                                                        final RegisteredService registeredService) {
        val repositories = new HashSet<String>(0);
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
                                           final SamlRegisteredServiceServiceProviderMetadataFacade facade) {
        return query.getSubject().getNameID() == null
            ? getConfigurationContext().getSamlObjectEncrypter().decode(
            query.getSubject().getEncryptedID(), registeredService, facade).getValue()
            : query.getSubject().getNameID().getValue();

    }
}
