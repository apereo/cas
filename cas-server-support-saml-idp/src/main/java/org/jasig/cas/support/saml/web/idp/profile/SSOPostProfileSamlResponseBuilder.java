package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.cryptacular.util.CertUtil;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.web.idp.SamlResponseBuilder;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.joda.time.DateTime;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectLocality;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.impl.FilesystemCredentialResolver;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link SSOPostProfileSamlResponseBuilder} is responsible for...
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

    private SubjectLocality buildSubjectLocality(final AuthnRequest authnRequest) throws Exception {
        final SubjectLocality subjectLocality = newSamlObject(SubjectLocality.class);
        final InetAddress address = InetAddress.getByName(
                new URL(authnRequest.getIssuer().getValue()).getHost());
        subjectLocality.setAddress(address.getHostAddress());
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
                          final SamlRegisteredService service) throws Exception {

        final List<Statement> statements = new ArrayList<>();
        statements.add(buildAuthnStatement(assertion, authnRequest));
        final AttributeStatement attributeStatement = buildAttributeStatement(assertion, authnRequest);
        if (attributeStatement != null) {
            statements.add(attributeStatement);
        }
        return buildResponse(authnRequest, statements, assertion, service);
    }

    private Issuer buildEntityIssuer() {
        final Issuer issuer = newIssuer(this.entityId);
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

    private NameID buildNameId(final AuthnRequest authnRequest, final Assertion assertion,
                               final SamlRegisteredService service)
            throws SAMLException {

        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            if (requiredNameFormat != null  && (requiredNameFormat.equals(NameID.ENCRYPTED)
                    || requiredNameFormat.equals(NameID.UNSPECIFIED))) {
                requiredNameFormat = null;
            }
        }

        final List<String> supportedNameFormats = service.getSupportedNameFormats();
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

        for (final String nameFormat : supportedNameFormats) {
            final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
            encoder.setNameFormat(nameFormat);
            if (authnRequest.getNameIDPolicy() != null) {
                encoder.setNameQualifier(authnRequest.getNameIDPolicy().getSPNameQualifier());
            }
            final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());
            final IdPAttributeValue<String> value = new StringAttributeValue(assertion.getPrincipal().getName());
            attribute.setValues(Arrays.asList(value));
            try {
                return encoder.encode(attribute);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private Subject buildSubject(final AuthnRequest authnRequest, final Assertion assertion,
                                   final SamlRegisteredService service) throws SAMLException {
        final NameID nameID = buildNameId(authnRequest, assertion, service);
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
                                   final SamlRegisteredService service) throws Exception {

        final String id = String.valueOf(new SecureRandom().nextLong());
        final Response samlResponse = newResponse(id, new DateTime(), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());

        org.opensaml.saml.saml2.core.Assertion assertion = null;
        if (statements != null && !statements.isEmpty()) {
            assertion = newAssertion(statements, this.entityId, new DateTime(), id);
            assertion.setSubject(buildSubject(authnRequest, casAssertion, service));
            signAssertion(authnRequest, statements, assertion, service);
            samlResponse.getAssertions().add(assertion);
        }

        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);

        return samlResponse;
    }

    private void signAssertion(final AuthnRequest authnRequest,
                               final List<Statement> statements,
                               final org.opensaml.saml.saml2.core.Assertion assertion,
                               final SamlRegisteredService service)
            throws SAMLException {
        logger.debug("Determining if SAML assertion to {} should be signed", service.getServiceId());
        if (!service.isSignAssertions()) {
            return;
        }

        logger.debug("Determining signing credential for assertion to relying party {}", service.getServiceId());
        final Credential signatureCredential = service.getSigningCredential();

        if (signatureCredential == null) {
            throw new SAMLException("No signing credential is specified for relying party configuration");
        }

        logger.debug("Signing assertion to relying party {}", service.getServiceId());
        final XMLObjectBuilder signatureBuilder = openSamlConfigBean.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        if (signatureBuilder == null) {
            throw new SAMLException("No signature builder can be determined");
        }
        final Signature signature = (Signature) signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(signatureCredential);
        assertion.setSignature(signature);

        try {
            final Marshaller assertionMarshaller = this.openSamlConfigBean.getMarshallerFactory().getMarshaller(assertion);
            if (assertionMarshaller == null) {
                throw new SAMLException("No signature marshaller is available");
            }

            assertionMarshaller.marshall(assertion);

            final SignatureSigningParameters signingParameters = buildSignatureSigningParameters(service.getSsoDescriptor());
            SignatureSupport.signObject(assertion, signingParameters);
        } catch (final Exception e) {
            logger.error("Unable to marshall assertion for signing", e);
            throw new SAMLException("Unable to marshall assertion for signing", e);
        }
    }

    private SignatureSigningParameters buildSignatureSigningParameters(final SSODescriptor descriptor) throws SAMLException {
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

            logger.info("Created signature signing parameters." +
                            "\nSignature algorithm: {}" +
                            "\nSignature canonicalization algorithm: {}" +
                            "\nSignature reference digest methods: {}",
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

        /*
        config.setBlacklistedAlgorithms(this.configuration.getBlackListedSignatureSigningAlgorithms());
        config.setSignatureAlgorithms(this.configuration.getSignatureAlgorithms());
        config.setSignatureCanonicalizationAlgorithm(this.configuration.getSignatureCanonicalizationAlgorithm());
        config.setSignatureReferenceDigestMethods(this.configuration.getSignatureReferenceDigestMethods());
        */

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
