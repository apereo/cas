package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.cryptacular.util.CertUtil;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.web.idp.SamlResponseBuilder;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.binding.security.impl.EndpointURLSchemeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectLocality;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link SSOPostProfileSamlResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("ssoPostProfileSamlResponseBuilder")
public class SSOPostProfileSamlResponseBuilder extends AbstractSaml20ObjectBuilder implements SamlResponseBuilder {
    private static final long serialVersionUID = -1891703354216174875L;

    @Value("${cas.samlidp.entityid:}")
    private String entityId;

    @Value("${cas.samlidp.metadata.location:}/idp-signing.crt")
    private File signingCertFile;

    @Value("${cas.samlidp.metadata.location:}/idp-signing.key")
    private File signingKeyFile;

    @Autowired
    private OpenSamlConfigBean openSamlConfigBean;

    @Value("${cas.samlidp.response.skewAllowance:0}")
    private int skewAllowance;

    @Value("${cas.samlidp.response.override.sig.can.alg:}")
    private String overrideSignatureCanonicalizationAlgorithm;

    @Autowired(required=false)
    @Qualifier("overrideSignatureReferenceDigestMethods")
    private List overrideSignatureReferenceDigestMethods;

    @Autowired(required=false)
    @Qualifier("overrideSignatureAlgorithms")
    private List overrideSignatureAlgorithms;

    @Autowired(required=false)
    @Qualifier("overrideBlackListedSignatureSigningAlgorithms")
    private List overrideBlackListedSignatureSigningAlgorithms;

    private SubjectLocality buildSubjectLocality(final AuthnRequest authnRequest) throws Exception {
        final SubjectLocality subjectLocality = newSamlObject(SubjectLocality.class);
        subjectLocality.setAddress(authnRequest.getIssuer().getValue());
        return subjectLocality;
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @return constructed authentication statement
     */
    private AuthnStatement buildAuthnStatement(final Assertion assertion, final AuthnRequest authnRequest)
            throws Exception {
        final AuthnStatement statement = newAuthnStatement(getAuthenticationMethodFromAssertion(assertion),
                new DateTime(assertion.getAuthenticationDate()));
        if (assertion.getValidUntilDate() != null) {
            statement.setSessionNotOnOrAfter(new DateTime(assertion.getValidUntilDate()));
        }
        statement.setSubjectLocality(buildSubjectLocality(authnRequest));
        return statement;
    }

    private static String getAuthenticationMethodFromAssertion(final Assertion assertion) {
        final Object object = assertion.getAttributes().get(CasProtocolConstants.VALIDATION_AUTHENTICATION_METHOD_ATTRIBUTE_NAME);
        if (object != null) {
            return object.toString();
        }
        return "";
    }

    private AttributeStatement buildAttributeStatement(final Assertion assertion, final AuthnRequest authnRequest)
            throws SAMLException {
        final Map<String, Object> attributes = new HashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());
        return newAttributeStatement(attributes);
    }

    @Override
    public Response build(final AuthnRequest authnRequest, final HttpServletRequest request,
                          final HttpServletResponse response, final Assertion assertion,
                          final SamlRegisteredService service, final SamlMetadataAdaptor adaptor) throws Exception {

        final List<Statement> statements = new ArrayList<>();
        statements.add(buildAuthnStatement(assertion, authnRequest));
        final AttributeStatement attributeStatement = buildAttributeStatement(assertion, authnRequest);
        if (attributeStatement != null) {
            statements.add(attributeStatement);
        }
        final Response finalResponse = buildResponse(authnRequest, statements, assertion, service, adaptor);
        return finalResponse;
    }

    private Issuer buildEntityIssuer() {
        final Issuer issuer = newIssuer(this.entityId);
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

    private NameID buildNameId(final AuthnRequest authnRequest, final Assertion assertion,
                               final SamlRegisteredService service, final SamlMetadataAdaptor adaptor)
            throws SAMLException {

        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            if (requiredNameFormat != null && (requiredNameFormat.equals(NameID.ENCRYPTED)
                    || requiredNameFormat.equals(NameID.UNSPECIFIED))) {
                requiredNameFormat = null;
            }
        }

        final List<String> supportedNameFormats = adaptor.getSupportedNameFormats();
        if (requiredNameFormat != null) {
            supportedNameFormats.clear();
            supportedNameFormats.add(requiredNameFormat);
        }

        final Map<String, Object> principalAttributes = assertion.getPrincipal().getAttributes();
        if (principalAttributes == null || principalAttributes.isEmpty()) {
            if (requiredNameFormat != null) {
                throw new SAMLException("No attributes for principal, so NameID format required is not supported");
            }
            logger.debug("No attributes for principal {}, no name identifier will be created.",
                    assertion.getPrincipal().getName());
            return null;
        }

        if (!supportedNameFormats.isEmpty()) {
            logger.debug("SP-supported name formats: {}", supportedNameFormats);
        } else {
            logger.debug("SP indicated no preferred name formats.");
        }
        try {
            for (final String nameFormat : supportedNameFormats) {
                final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
                encoder.setNameFormat(nameFormat);
                if (authnRequest.getNameIDPolicy() != null) {
                    encoder.setNameQualifier(authnRequest.getNameIDPolicy().getSPNameQualifier());
                }
                final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());
                final IdPAttributeValue<String> value = new StringAttributeValue(assertion.getPrincipal().getName());
                attribute.setValues(Collections.singletonList(value));
                return encoder.encode(attribute);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private Subject buildSubject(final AuthnRequest authnRequest, final Assertion assertion,
                                 final SamlRegisteredService service, final SamlMetadataAdaptor adaptor) throws SAMLException {
        final NameID nameID = buildNameId(authnRequest, assertion, service, adaptor);
        if (nameID == null) {
            throw new SAMLException("NameID cannot be determined for authN request");
        }

        final Subject subject = newSubject(nameID.getFormat(), nameID.getValue(),
                authnRequest.getAssertionConsumerServiceURL(),
                new DateTime(assertion.getValidFromDate()),
                authnRequest.getID());
        subject.setNameID(nameID);
        return subject;
    }

    private Response buildResponse(final AuthnRequest authnRequest,
                                   final List<Statement> statements,
                                   final Assertion casAssertion,
                                   final SamlRegisteredService service,
                                   final SamlMetadataAdaptor adaptor) throws Exception {

        final String id = String.valueOf(new SecureRandom().nextLong());
        final Response samlResponse = newResponse(id, new DateTime(), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());

        final org.opensaml.saml.saml2.core.Assertion assertion;
        if (statements != null && !statements.isEmpty()) {
            assertion = newAssertion(statements, this.entityId, new DateTime(), id);
            assertion.setSubject(buildSubject(authnRequest, casAssertion, service, adaptor));
            assertion.setConditions(buildConditions(authnRequest, casAssertion, service, adaptor));
            signAssertion(authnRequest, statements, assertion, service, adaptor);
            samlResponse.getAssertions().add(assertion);
        }

        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);

        return samlResponse;
    }

    private Conditions buildConditions(final AuthnRequest authnRequest, final Assertion assertion,
                                       final SamlRegisteredService service, final SamlMetadataAdaptor adaptor) throws SAMLException {

        final DateTime currentDateTime = new DateTime();
        final Conditions conditions = newConditions(currentDateTime,
                currentDateTime.plusSeconds(this.skewAllowance),
                service.getEntityId());
        return conditions;
    }

    private void signAssertion(final AuthnRequest authnRequest,
                               final List<Statement> statements,
                               final org.opensaml.saml.saml2.core.Assertion assertion,
                               final SamlRegisteredService service,
                               final SamlMetadataAdaptor adaptor) throws SAMLException {

        try {
            logger.debug("Determining if SAML assertionfor {} should be signed", service.getEntityId());
            if (!adaptor.isWantAssertionsSigned()) {
                logger.debug("Relying party does not want assertions signed, so assertions will not be signed");
                return;
            }

            final MessageContext<org.opensaml.saml.saml2.core.Assertion> outboundContext = new MessageContext<>();
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
            throw new SAMLException("Unable to marshall assertion for signing", e);
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
