package org.apereo.cas.support.saml.web.idp.profile.builders;

import com.google.common.base.Throwables;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.apereo.cas.support.saml.SamlException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * The {@link SamlProfileSamlResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlProfileSamlResponseBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Response> {
    private static final long serialVersionUID = -1891703354216174875L;
    
    /**
     * The Saml object encoder.
     */
    protected SamlObjectSigner samlObjectSigner;

    /**
     * The Velocity engine factory.
     */
    protected VelocityEngineFactory velocityEngineFactory;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private SamlProfileSamlAssertionBuilder samlProfileSamlAssertionBuilder;
    
    private SamlObjectEncrypter samlObjectEncrypter;

    @Override
    public Response build(final AuthnRequest authnRequest, final HttpServletRequest request,
                                final HttpServletResponse response, final org.jasig.cas.client.validation.Assertion casAssertion,
                                final SamlRegisteredService service,
                                final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        final Assertion assertion = this.samlProfileSamlAssertionBuilder.build(authnRequest, 
                request, response, casAssertion, service, adaptor);
        final Response finalResponse = buildResponse(assertion, authnRequest, service, adaptor, request, response);
        return encode(service, finalResponse, response, adaptor);
    }

    /**
     * Build response response.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param service      the service
     * @param adaptor      the adaptor
     * @param request      the request
     * @param response     the response
     * @return the response
     * @throws SamlException the saml exception
     */
    protected Response buildResponse(final Assertion assertion,
                                     final AuthnRequest authnRequest, final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request, final HttpServletResponse response)
            throws SamlException {
        final String id = "_" + String.valueOf(Math.abs(new SecureRandom().nextLong()));
        Response samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());
        samlResponse.setConsent(RequestAbstractType.UNSPECIFIED_CONSENT);
        
        final SAMLObject finalAssertion = encryptAssertion(assertion, request, response, service, adaptor);

        if (finalAssertion instanceof EncryptedAssertion) {
            logger.debug("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            logger.debug("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);

        SamlUtils.logSamlObject(this.configBean, samlResponse);

        if (service.isSignResponses()) {
            logger.debug("SAML entity id [{}] indicates that SAML responses should be signed",
                    adaptor.getEntityId());
            samlResponse = this.samlObjectSigner.encode(samlResponse, service, adaptor, response, request);
        }

        return samlResponse;
    }

    /**
     * Build entity issuer issuer.
     *
     * @return the issuer
     */
    protected Issuer buildEntityIssuer() {
        final Issuer issuer = newIssuer(casProperties.getAuthn().getSamlIdp().getEntityId());
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

    /**
     * Encode response.
     *
     * @param service      the service
     * @param samlResponse the saml response
     * @param httpResponse the http response
     * @param adaptor      the adaptor
     * @return the response
     * @throws SamlException the saml exception
     */
    protected Response encode(final SamlRegisteredService service, final Response samlResponse,
                              final HttpServletResponse httpResponse,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        try {
            final HTTPPostEncoder encoder = new HTTPPostEncoder();
            encoder.setHttpServletResponse(httpResponse);
            encoder.setVelocityEngine(this.velocityEngineFactory.createVelocityEngine());
            final MessageContext outboundMessageContext = new MessageContext<>();
            SamlIdPUtils.preparePeerEntitySamlEndpointContext(outboundMessageContext, adaptor);
            outboundMessageContext.setMessage(samlResponse);
            encoder.setMessageContext(outboundMessageContext);
            encoder.initialize();
            encoder.encode();
            return samlResponse;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Encrypt assertion.
     *
     * @param assertion the assertion
     * @param request   the request
     * @param response  the response
     * @param service   the service
     * @param adaptor   the adaptor
     * @return the saml object
     * @throws SamlException the saml exception
     */
    protected SAMLObject encryptAssertion(final Assertion assertion,
                                          final HttpServletRequest request, final HttpServletResponse response,
                                          final SamlRegisteredService service,
                                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        try {
            if (service.isEncryptAssertions()) {
                logger.info("SAML service [{}] requires assertions to be encrypted", adaptor.getEntityId());
                final EncryptedAssertion encryptedAssertion = 
                        this.samlObjectEncrypter.encode(assertion, service, adaptor, response, request);
                return encryptedAssertion;
            }
            logger.info("SAML registered service [{}] does not require assertions to be encrypted", adaptor.getEntityId());
            return assertion;
        } catch (final Exception e) {
            throw new SamlException("Unable to marshall assertion for encryption", e);
        }
    }

    public void setSamlObjectSigner(final SamlObjectSigner samlObjectSigner) {
        this.samlObjectSigner = samlObjectSigner;
    }

    public void setVelocityEngineFactory(final VelocityEngineFactory velocityEngineFactory) {
        this.velocityEngineFactory = velocityEngineFactory;
    }

    public void setSamlProfileSamlAssertionBuilder(final SamlProfileSamlAssertionBuilder samlProfileSamlAssertionBuilder) {
        this.samlProfileSamlAssertionBuilder = samlProfileSamlAssertionBuilder;
    }

    public void setSamlObjectEncrypter(final SamlObjectEncrypter samlObjectEncrypter) {
        this.samlObjectEncrypter = samlObjectEncrypter;
    }
}
