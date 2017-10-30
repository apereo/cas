package org.apereo.cas.support.saml.web.idp.profile.query;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link Saml2AttributeQueryProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class Saml2AttributeQueryProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(Saml2AttributeQueryProfileHandlerController.class);
    private final TicketRegistry ticketRegistry;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;
    private final SamlProfileObjectBuilder<? extends SAMLObject> samlFaultResponseBuilder;


    public Saml2AttributeQueryProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                                       final ParserPool parserPool,
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
                                                       final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory) {
        super(samlObjectSigner, parserPool, authenticationSystemSupport, servicesManager,
                webApplicationServiceFactory, metadataResolver, configBean,
                responseBuilder, casProperties, samlObjectSignatureValidator);
        this.ticketRegistry = ticketRegistry;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.samlAttributeQueryTicketFactory = samlAttributeQueryTicketFactory;
        this.casProperties = casProperties;
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

        final MessageContext ctx = decodeSoapRequest(request);
        final AttributeQuery query = (AttributeQuery) ctx.getMessage();
        try {
            final String issuer = query.getIssuer().getValue();
            final SamlRegisteredService service = verifySamlRegisteredService(issuer);
            final Optional<SamlRegisteredServiceServiceProviderMetadataFacade> adaptor = getSamlMetadataFacadeFor(service, query);
            if (!adaptor.isPresent()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }

            final SamlRegisteredServiceServiceProviderMetadataFacade facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, query, facade);

            final Map<String, Object> attrs = new LinkedHashMap<>();
            if (query.getAttributes().isEmpty()) {
                final String id = this.samlAttributeQueryTicketFactory.createTicketIdFor(query.getSubject().getNameID().getValue());
                final SamlAttributeQueryTicket ticket = this.ticketRegistry.getTicket(id, SamlAttributeQueryTicket.class);

                final Authentication authentication = ticket.getGrantingTicket().getAuthentication();
                final Principal principal = authentication.getPrincipal();

                final Map<String, Object> authnAttrs = authentication.getAttributes();
                final Map<String, Object> principalAttrs = principal.getAttributes();

                query.getAttributes().forEach(a -> {
                    if (authnAttrs.containsKey(a.getName())) {
                        attrs.put(a.getName(), authnAttrs.get(a.getName()));
                    } else if (principalAttrs.containsKey(a.getName())) {
                        attrs.put(a.getName(), principalAttrs.get(a.getName()));
                    }
                });
            }

            final Assertion casAssertion = buildCasAssertion(issuer, service, attrs);
            this.responseBuilder.build(query, request, response, casAssertion, service, facade, SAMLConstants.SAML2_SOAP11_BINDING_URI);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, e.getMessage());
            samlFaultResponseBuilder.build(query, request, response, null, null, null, SAMLConstants.SAML2_SOAP11_BINDING_URI);
        }
    }
}
