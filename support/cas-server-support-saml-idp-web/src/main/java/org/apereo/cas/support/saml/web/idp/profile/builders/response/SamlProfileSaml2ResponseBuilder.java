package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponseArtifactEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostSimpleSignEncoder;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;

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
@Slf4j
public class SamlProfileSaml2ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {
    private static final long serialVersionUID = 1488837627964481272L;
    
    public SamlProfileSaml2ResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext);
    }

    @Override
    public Response buildResponse(final Assertion assertion,
                                  final Object casAssertion,
                                  final RequestAbstractType authnRequest,
                                  final SamlRegisteredService service,
                                  final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                  final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final String binding,
                                  final MessageContext messageContext) throws SamlException {
        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);

        if (StringUtils.isBlank(service.getIssuerEntityId())) {
            samlResponse.setIssuer(buildSamlResponseIssuer(getSamlResponseBuilderConfigurationContext().getCasProperties()
                .getAuthn().getSamlIdp().getEntityId()));
        } else {
            samlResponse.setIssuer(buildSamlResponseIssuer(service.getIssuerEntityId()));
        }

        val acs = SamlIdPUtils.determineEndpointForRequest(authnRequest, adaptor, binding);
        val location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        samlResponse.setDestination(location);

        if (getSamlResponseBuilderConfigurationContext().getCasProperties()
            .getAuthn().getSamlIdp().isAttributeQueryProfileEnabled()) {
            storeAttributeQueryTicketInRegistry(assertion, request, adaptor);
        }

        val finalAssertion = encryptAssertion(assertion, request, response, service, adaptor);

        if (finalAssertion instanceof EncryptedAssertion) {
            LOGGER.trace("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            LOGGER.trace("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        val status = newStatus(StatusCode.SUCCESS, null);
        samlResponse.setStatus(status);

        SamlUtils.logSamlObject(this.openSamlConfigBean, samlResponse);

        if (service.isSignResponses()) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", adaptor.getEntityId());
            val samlResponseSigned = getSamlResponseBuilderConfigurationContext().getSamlObjectSigner()
                .encode(samlResponse, service, adaptor, response, request, binding, authnRequest);
            SamlUtils.logSamlObject(openSamlConfigBean, samlResponseSigned);
            return samlResponseSigned;
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
        LOGGER.trace("Constructing encoder based on binding [{}] for [{}]", binding, adaptor.getEntityId());
        if (binding.equalsIgnoreCase(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)) {
            val encoder = new SamlResponseArtifactEncoder(
                getSamlResponseBuilderConfigurationContext().getVelocityEngineFactory(),
                adaptor, httpRequest, httpResponse,
                getSamlResponseBuilderConfigurationContext().getSamlArtifactMap());
            return encoder.encode(authnRequest, samlResponse, relayState);
        }

        if (binding.equalsIgnoreCase(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI)) {
            val encoder = new SamlResponsePostSimpleSignEncoder(getSamlResponseBuilderConfigurationContext().getVelocityEngineFactory(),
                adaptor, httpResponse, httpRequest);
            return encoder.encode(authnRequest, samlResponse, relayState);
        }

        val encoder = new SamlResponsePostEncoder(getSamlResponseBuilderConfigurationContext().getVelocityEngineFactory(), adaptor, httpResponse, httpRequest);
        return encoder.encode(authnRequest, samlResponse, relayState);
    }

    private void storeAttributeQueryTicketInRegistry(final Assertion assertion, final HttpServletRequest request,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {

        val value = assertion.getSubject().getNameID().getValue();
        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            getSamlResponseBuilderConfigurationContext().getTicketGrantingTicketCookieGenerator(),
            getSamlResponseBuilderConfigurationContext().getTicketRegistry(), request);

        val ticket = getSamlResponseBuilderConfigurationContext().getSamlAttributeQueryTicketFactory().create(value,
            assertion, adaptor.getEntityId(), ticketGrantingTicket);
        getSamlResponseBuilderConfigurationContext().getTicketRegistry().addTicket(ticket);

    }
}
