package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlResponseEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlResponseArtifactEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlResponsePostEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlResponsePostSimpleSignEncoder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieUtils;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileSaml2ResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlProfileSaml2ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {
    private static final long serialVersionUID = 1488837627964481272L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlProfileSaml2ResponseBuilder.class);
    
    private final TicketRegistry ticketRegistry;
    private final SamlArtifactTicketFactory samlArtifactTicketFactory;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final SAMLArtifactMap samlArtifactMap;
    private final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    public SamlProfileSaml2ResponseBuilder(final OpenSamlConfigBean openSamlConfigBean,
                                           final BaseSamlObjectSigner samlObjectSigner,
                                           final VelocityEngineFactory velocityEngineFactory,
                                           final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
                                           final SamlObjectEncrypter samlObjectEncrypter,
                                           final TicketRegistry ticketRegistry,
                                           final SamlArtifactTicketFactory samlArtifactTicketFactory,
                                           final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                           final SAMLArtifactMap samlArtifactMap,
                                           final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory) {
        super(openSamlConfigBean, samlObjectSigner, velocityEngineFactory, samlProfileSamlAssertionBuilder, samlObjectEncrypter);
        this.ticketRegistry = ticketRegistry;
        this.samlArtifactTicketFactory = samlArtifactTicketFactory;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.samlArtifactMap = samlArtifactMap;
        this.samlAttributeQueryTicketFactory = samlAttributeQueryTicketFactory;
    }

    @Override
    protected Response buildResponse(final Assertion assertion,
                                     final Object casAssertion,
                                     final RequestAbstractType authnRequest,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final String binding) throws SamlException {
        final String id = '_' + String.valueOf(Math.abs(RandomUtils.getInstanceNative().nextLong()));
        Response samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());
        
        if (casProperties.getAuthn().getSamlIdp().isAttributeQueryProfileEnabled()) {
            storeAttributeQueryTicketInRegistry(assertion, request, adaptor);
        }
        
        final SAMLObject finalAssertion = encryptAssertion(assertion, request, response, service, adaptor);

        if (finalAssertion instanceof EncryptedAssertion) {
            LOGGER.debug("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            LOGGER.debug("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        final Status status = newStatus(StatusCode.SUCCESS, null);
        samlResponse.setStatus(status);

        SamlUtils.logSamlObject(this.configBean, samlResponse);

        if (service.isSignResponses()) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", adaptor.getEntityId());
            samlResponse = this.samlObjectSigner.encode(samlResponse, service, adaptor, response, request, binding);
            SamlUtils.logSamlObject(configBean, samlResponse);
        }

        return samlResponse;
    }

    @Override
    protected Response encode(final SamlRegisteredService service,
                              final Response samlResponse,
                              final HttpServletResponse httpResponse,
                              final HttpServletRequest httpRequest,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                              final String relayState,
                              final String binding,
                              final RequestAbstractType authnRequest,
                              final Object assertion) throws SamlException {
        
        LOGGER.debug("Constructing encoder based on binding [{}] for [{}]", binding, adaptor.getEntityId());
        
        if (binding.equalsIgnoreCase(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)) {
            final BaseSamlResponseEncoder encoder = new SamlResponseArtifactEncoder(this.velocityEngineFactory,
                    adaptor, httpRequest, httpResponse, authnRequest,
                    ticketRegistry, samlArtifactTicketFactory,
                    ticketGrantingTicketCookieGenerator, samlArtifactMap);
            return encoder.encode(samlResponse, relayState);
        }
        
        if (binding.equalsIgnoreCase(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI)) {
            final BaseSamlResponseEncoder encoder = new SamlResponsePostSimpleSignEncoder(this.velocityEngineFactory, adaptor, httpResponse, httpRequest);
            return encoder.encode(samlResponse, relayState);
        }
        
        final BaseSamlResponseEncoder encoder = new SamlResponsePostEncoder(this.velocityEngineFactory, adaptor, httpResponse, httpRequest);
        return encoder.encode(samlResponse, relayState);
    }

    private void storeAttributeQueryTicketInRegistry(final Assertion assertion, final HttpServletRequest request,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {

        final String value = assertion.getSubject().getNameID().getValue();
        final TicketGrantingTicket ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                ticketGrantingTicketCookieGenerator, this.ticketRegistry, request);

        final SamlAttributeQueryTicket ticket = samlAttributeQueryTicketFactory.create(value,
               assertion, adaptor.getEntityId(), ticketGrantingTicket);
        this.ticketRegistry.addTicket(ticket);

    }
}
