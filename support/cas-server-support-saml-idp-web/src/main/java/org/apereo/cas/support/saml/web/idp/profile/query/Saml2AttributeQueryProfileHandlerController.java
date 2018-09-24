package org.apereo.cas.support.saml.web.idp.profile.query;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

/**
 * This is {@link Saml2AttributeQueryProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class Saml2AttributeQueryProfileHandlerController extends AbstractSamlProfileHandlerController {

    private final TicketRegistry ticketRegistry;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;
    private final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;


    public Saml2AttributeQueryProfileHandlerController(final SamlIdPObjectSigner samlObjectSigner,
                                                       final AuthenticationSystemSupport authenticationSystemSupport,
                                                       final ServicesManager servicesManager,
                                                       final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                       final SamlRegisteredServiceCachingMetadataResolver metadataResolver,
                                                       final OpenSamlConfigBean configBean,
                                                       final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder,
                                                       final CasConfigurationProperties casProperties,
                                                       final SamlObjectSignatureValidator samlObjectSignatureValidator,
                                                       final TicketRegistry ticketRegistry,
                                                       final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder,
                                                       final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                       final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
                                                       final Service callbackService) {
        super(samlObjectSigner, authenticationSystemSupport, servicesManager,
            webApplicationServiceFactory, metadataResolver, configBean,
            responseBuilder, casProperties, samlObjectSignatureValidator, callbackService);
        this.ticketRegistry = ticketRegistry;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.samlAttributeQueryTicketFactory = samlAttributeQueryTicketFactory;
        this.samlFaultResponseBuilder = samlFaultResponseBuilder;
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
            val issuer = query.getIssuer().getValue();
            val service = verifySamlRegisteredService(issuer);
            val adaptor = getSamlMetadataFacadeFor(service, query);
            if (!adaptor.isPresent()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }

            val facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, query, facade);

            val attrs = new LinkedHashMap<String, Object>();
            if (query.getAttributes().isEmpty()) {
                val id = this.samlAttributeQueryTicketFactory.createTicketIdFor(query.getSubject().getNameID().getValue());
                val ticket = this.ticketRegistry.getTicket(id, SamlAttributeQueryTicket.class);

                val authentication = ticket.getTicketGrantingTicket().getAuthentication();
                val principal = authentication.getPrincipal();

                val authnAttrs = authentication.getAttributes();
                val principalAttrs = principal.getAttributes();

                query.getAttributes().forEach(a -> {
                    if (authnAttrs.containsKey(a.getName())) {
                        attrs.put(a.getName(), authnAttrs.get(a.getName()));
                    } else if (principalAttrs.containsKey(a.getName())) {
                        attrs.put(a.getName(), principalAttrs.get(a.getName()));
                    }
                });
            }

            val casAssertion = buildCasAssertion(issuer, service, attrs);
            this.responseBuilder.build(query, request, response, casAssertion, service, facade, SAMLConstants.SAML2_SOAP11_BINDING_URI, ctx);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, e.getMessage());
            samlFaultResponseBuilder.build(query, request, response, null, null, null, SAMLConstants.SAML2_SOAP11_BINDING_URI, ctx);
        }
    }
}
