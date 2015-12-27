package org.jasig.cas.support.saml.web.idp.profile.builders;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.cryptacular.util.CertUtil;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.binding.security.impl.EndpointURLSchemeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlProfileSamlAssertionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("samlProfileSamlAssertionBuilder")
public class SamlProfileSamlAssertionBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Assertion> {
    private static final long serialVersionUID = -3945938960014421135L;

    @Value("${cas.samlidp.entityid:}")
    private String entityId;

    @Autowired
    @Qualifier("samlProfileSamlAuthNStatementBuilder")
    private SamlProfileSamlAuthNStatementBuilder samlProfileSamlAuthNStatementBuilder;

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileSamlAttributeStatementBuilder samlProfileSamlAttributeStatementBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSubjectBuilder")
    private SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder;

    @Autowired
    @Qualifier("samlProfileSamlConditionsBuilder")
    private SamlProfileSamlConditionsBuilder samlProfileSamlConditionsBuilder;

    @Value("${cas.samlidp.response.override.sig.can.alg:}")
    private String overrideSignatureCanonicalizationAlgorithm;

    @Autowired(required = false)
    @Qualifier("overrideSignatureReferenceDigestMethods")
    private List overrideSignatureReferenceDigestMethods;

    @Autowired(required = false)
    @Qualifier("overrideSignatureAlgorithms")
    private List overrideSignatureAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideBlackListedSignatureSigningAlgorithms")
    private List overrideBlackListedSignatureSigningAlgorithms;

    @Value("${cas.samlidp.metadata.location:}/idp-signing.crt")
    private File signingCertFile;

    @Value("${cas.samlidp.metadata.location:}/idp-signing.key")
    private File signingKeyFile;

    @Override
    public Assertion build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                       final org.jasig.cas.client.validation.Assertion casAssertion, final SamlRegisteredService service,
                       final SamlMetadataAdaptor adaptor) throws SamlException {

        final List<Statement> statements = new ArrayList<>();
        statements.add(samlProfileSamlAuthNStatementBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));
        statements.add(samlProfileSamlAttributeStatementBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));

        final String id = String.valueOf(Math.abs(new SecureRandom().nextLong()));
        final Assertion assertion = newAssertion(statements, this.entityId, DateTime.now(), id);
        assertion.setSubject(samlProfileSamlSubjectBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));
        assertion.setConditions(samlProfileSamlConditionsBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));

        signAssertion(assertion, service, adaptor);
        return assertion;
    }

    private void signAssertion(final Assertion assertion,
                               final SamlRegisteredService service,
                               final SamlMetadataAdaptor adaptor) throws SamlException {

        try {
            logger.debug("Determining if SAML assertionfor {} should be signed", service.getEntityId());
            if (!adaptor.isWantAssertionsSigned()) {
                logger.debug("Relying party does not want assertions signed, so assertions will not be signed");
                return;
            }

            final MessageContext<Assertion> outboundContext = new MessageContext<>();
            outboundContext.setMessage(assertion);

            final List<AssertionConsumerService> assertionConsumerServices = adaptor.getAssertionConsumerServices();
            final SAMLPeerEntityContext peerEntityContext = outboundContext.getSubcontext(SAMLPeerEntityContext.class, true);
            if (peerEntityContext != null) {
                final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
                if (endpointContext != null) {
                    endpointContext.setEndpoint(assertionConsumerServices.get(0));
                }
            }
            final SecurityParametersContext secParametersContext = outboundContext.getSubcontext(SecurityParametersContext.class, true);
            if (secParametersContext == null) {
                throw new RuntimeException("No signature signing parameters could be determined");
            }
            final SignatureSigningParameters signingParameters = buildSignatureSigningParameters(adaptor.getSsoDescriptor());
            secParametersContext.setSignatureSigningParameters(signingParameters);

            final EndpointURLSchemeSecurityHandler handlerEnd = new EndpointURLSchemeSecurityHandler();
            handlerEnd.initialize();
            handlerEnd.invoke(outboundContext);

            final SAMLOutboundDestinationHandler handlerDest = new SAMLOutboundDestinationHandler();
            handlerDest.initialize();
            handlerDest.invoke(outboundContext);

            final SAMLOutboundProtocolMessageSigningHandler handler = new SAMLOutboundProtocolMessageSigningHandler();
            handler.setSignErrorResponses(false);
            handler.invoke(outboundContext);
        } catch (final Exception e) {
            logger.error("Unable to marshall assertion for signing", e);
            throw new SamlException("Unable to marshall assertion for signing", e);
        }
    }

    private SignatureSigningParameters buildSignatureSigningParameters(final RoleDescriptor descriptor) throws SAMLException {
        try {
            final CriteriaSet criteria = new CriteriaSet();
            criteria.add(new SignatureSigningConfigurationCriterion(getSignatureSigningConfiguration()));
            criteria.add(new RoleDescriptorCriterion(descriptor));
            final SAMLMetadataSignatureSigningParametersResolver resolver =
                    new SAMLMetadataSignatureSigningParametersResolver();

            final SignatureSigningParameters params = resolver.resolveSingle(criteria);
            if (params == null) {
                throw new SAMLException("No signature signing parameter is available");
            }

            logger.info("Created signature signing parameters."
                            + "\nSignature algorithm: {}"
                            + "\nSignature canonicalization algorithm: {}"
                            + "\nSignature reference digest methods: {}",
                    params.getSignatureAlgorithm(), params.getSignatureCanonicalizationAlgorithm(),
                    params.getSignatureReferenceDigestMethod());

            return params;
        } catch (final Exception e) {
            throw new SAMLException(e.getMessage(), e);
        }
    }

    private SignatureSigningConfiguration getSignatureSigningConfiguration() throws Exception {
        final BasicSignatureSigningConfiguration config =
                DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();


        if (this.overrideBlackListedSignatureSigningAlgorithms != null && !this.overrideSignatureCanonicalizationAlgorithm.isEmpty()) {
            config.setBlacklistedAlgorithms(this.overrideBlackListedSignatureSigningAlgorithms);
        }

        if (this.overrideSignatureAlgorithms != null && !this.overrideSignatureAlgorithms.isEmpty()) {
            config.setSignatureAlgorithms(this.overrideSignatureAlgorithms);
        }

        if (this.overrideSignatureReferenceDigestMethods != null && !this.overrideSignatureReferenceDigestMethods.isEmpty()) {
            config.setSignatureReferenceDigestMethods(this.overrideSignatureReferenceDigestMethods);
        }

        if (StringUtils.isNotBlank(overrideSignatureCanonicalizationAlgorithm)) {
            config.setSignatureCanonicalizationAlgorithm(this.overrideSignatureCanonicalizationAlgorithm);
        }

        final PrivateKeyFactoryBean privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(new FileSystemResource(this.signingKeyFile));
        privateKeyFactoryBean.setAlgorithm("RSA");
        final PrivateKey privateKey = privateKeyFactoryBean.getObject();

        final X509Certificate certificate = readCertificate(new FileSystemResource(this.signingCertFile));
        final List<Credential> creds = new ArrayList<>();
        creds.add(new BasicX509Credential(certificate, privateKey));
        config.setSigningCredentials(creds);
        return config;
    }

    private static X509Certificate readCertificate(final Resource resource) {
        try (final InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new RuntimeException("Error reading certificate " + resource, e);
        }
    }
}
