package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import com.google.common.base.Throwables;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
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
import java.security.SecureRandom;
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

    public SamlProfileSaml2ResponseBuilder(final OpenSamlConfigBean openSamlConfigBean,
                                           final BaseSamlObjectSigner samlObjectSigner,
                                           final VelocityEngineFactory velocityEngineFactory,
                                           final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
                                           final SamlObjectEncrypter samlObjectEncrypter) {
        super(openSamlConfigBean, samlObjectSigner, velocityEngineFactory, samlProfileSamlAssertionBuilder, samlObjectEncrypter);
    }

    @Override
    protected Response buildResponse(final Assertion assertion,
                                     final org.jasig.cas.client.validation.Assertion casAssertion,
                                     final AuthnRequest authnRequest,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final String binding) throws SamlException {
        final String id = '_' + String.valueOf(Math.abs(new SecureRandom().nextLong()));
        Response samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());
        samlResponse.setConsent(RequestAbstractType.UNSPECIFIED_CONSENT);

        final SAMLObject finalAssertion = encryptAssertion(assertion, request, response, service, adaptor);

        if (finalAssertion instanceof EncryptedAssertion) {
            LOGGER.debug("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            LOGGER.debug("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);

        SamlUtils.logSamlObject(this.configBean, samlResponse);

        if (service.isSignResponses()) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", adaptor.getEntityId());
            samlResponse = this.samlObjectSigner.encode(samlResponse, service, adaptor, 
                    response, request, binding);
        }

        return samlResponse;
    }

    @Override
    protected Response encode(final SamlRegisteredService service,
                              final Response samlResponse,
                              final HttpServletResponse httpResponse,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                              final String relayState, 
                              final String binding) throws SamlException {
        try {
            if (httpResponse != null) {
                final HTTPPostEncoder encoder = new HTTPPostEncoder();
                encoder.setHttpServletResponse(httpResponse);
                encoder.setVelocityEngine(this.velocityEngineFactory.createVelocityEngine());
                final MessageContext outboundMessageContext = new MessageContext<>();
                outboundMessageContext.setMessage(samlResponse);
                SAMLBindingSupport.setRelayState(outboundMessageContext, relayState);
                SamlIdPUtils.preparePeerEntitySamlEndpointContext(outboundMessageContext, adaptor, binding);
                encoder.setMessageContext(outboundMessageContext);
                encoder.initialize();
                encoder.encode();
            }
            return samlResponse;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
