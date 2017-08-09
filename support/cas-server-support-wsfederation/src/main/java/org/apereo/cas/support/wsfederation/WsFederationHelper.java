package org.apereo.cas.support.wsfederation;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.Conditions;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.soap.wsfed.RequestSecurityTokenResponse;
import org.opensaml.soap.wsfed.RequestedSecurityToken;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.support.ChainingEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignaturePrevalidator;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Security;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper class that does the heavy lifting with the openSaml library.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(WsFederationHelper.class);

    private OpenSamlConfigBean configBean;

    /**
     * private constructor.
     */
    public WsFederationHelper() {
    }

    /**
     * createCredentialFromToken converts a SAML 1.1 assertion to a WSFederationCredential.
     *
     * @param assertion the provided assertion
     * @return an equivalent credential.
     */
    public WsFederationCredential createCredentialFromToken(final Assertion assertion) {
        final ZonedDateTime retrievedOn = ZonedDateTime.now();
        LOGGER.debug("Retrieved on [{}]", retrievedOn);

        final WsFederationCredential credential = new WsFederationCredential();
        credential.setRetrievedOn(retrievedOn);
        credential.setId(assertion.getID());
        credential.setIssuer(assertion.getIssuer());
        credential.setIssuedOn(ZonedDateTime.parse(assertion.getIssueInstant().toDateTimeISO().toString()));

        final Conditions conditions = assertion.getConditions();
        if (conditions != null) {
            credential.setNotBefore(ZonedDateTime.parse(conditions.getNotBefore().toDateTimeISO().toString()));
            credential.setNotOnOrAfter(ZonedDateTime.parse(conditions.getNotOnOrAfter().toDateTimeISO().toString()));

            if (!conditions.getAudienceRestrictionConditions().isEmpty()) {
                credential.setAudience(conditions.getAudienceRestrictionConditions().get(0).getAudiences().get(0).getUri());
            }
        }

        if (!assertion.getAuthenticationStatements().isEmpty()) {
            credential.setAuthenticationMethod(assertion.getAuthenticationStatements().get(0).getAuthenticationMethod());
        }

        //retrieve an attributes from the assertion
        final HashMap<String, List<Object>> attributes = new HashMap<>();
        assertion.getAttributeStatements().stream().flatMap(attributeStatement -> attributeStatement.getAttributes().stream()).forEach(item -> {
            LOGGER.debug("Processed attribute: [{}]", item.getAttributeName());
            final List<Object> itemList = IntStream.range(0, item.getAttributeValues().size())
                    .mapToObj(i -> ((XSAny) item.getAttributeValues().get(i)).getTextContent())
                    .collect(Collectors.toList());
            if (!itemList.isEmpty()) {
                attributes.put(item.getAttributeName(), itemList);
            }
        });

        credential.setAttributes(attributes);
        LOGGER.debug("Credential: [{}]", credential);
        return credential;
    }

    /**
     * Gets request security token response from result.
     *
     * @param wresult the wresult
     * @return the request security token response from result
     */
    public RequestedSecurityToken getRequestSecurityTokenFromResult(final String wresult) {
        LOGGER.debug("Result token received from ADFS is [{}]", wresult);

        try (InputStream in = new ByteArrayInputStream(wresult.getBytes(StandardCharsets.UTF_8))) {

            LOGGER.debug("Parsing token into a document");
            final Document document = configBean.getParserPool().parse(in);
            final Element metadataRoot = document.getDocumentElement();
            final UnmarshallerFactory unmarshallerFactory = configBean.getUnmarshallerFactory();
            final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);
            if (unmarshaller == null) {
                throw new IllegalArgumentException("Unmarshaller for the metadata root element cannot be determined");
            }

            LOGGER.debug("Unmarshalling the document into a security token response");
            final RequestSecurityTokenResponse rsToken = (RequestSecurityTokenResponse) unmarshaller.unmarshall(metadataRoot);

            if (rsToken.getRequestedSecurityToken() == null) {
                throw new IllegalArgumentException("Request security token response is null");
            }

            LOGGER.debug("Locating list of requested security tokens");
            final List<RequestedSecurityToken> rst = rsToken.getRequestedSecurityToken();

            if (rst.isEmpty()) {
                throw new IllegalArgumentException("No requested security token response is provided in the response");
            }

            LOGGER.debug("Locating the first occurrence of a requested security token in the list");
            final RequestedSecurityToken reqToken = rst.get(0);
            if (reqToken.getSecurityTokens() == null || reqToken.getSecurityTokens().isEmpty()) {
                throw new IllegalArgumentException("Requested security token response is not carrying any security tokens");
            }

            return reqToken;
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * converts a token into an assertion.
     *
     * @param reqToken the req token
     * @param config   the config
     * @return an assertion
     */
    public Pair<Assertion, WsFederationConfiguration> buildAndVerifyAssertion(final RequestedSecurityToken reqToken,
                                                                              final Collection<WsFederationConfiguration> config) {

        final XMLObject securityToken = getSecurityTokenFromRequestedToken(reqToken, config);

        if (securityToken instanceof Assertion) {
            LOGGER.debug("Security token is an assertion.");
            final Assertion assertion = Assertion.class.cast(securityToken);
            LOGGER.debug("Extracted assertion successfully: [{}]", assertion);

            final WsFederationConfiguration cfg = config.stream()
                    .filter(c -> c.getIdentityProviderIdentifier().equals(assertion.getIssuer()))
                    .findFirst()
                    .orElse(null);

            if (cfg == null) {
                throw new IllegalArgumentException("Could not locate wsfed configuration for security token provided");
            }

            return Pair.of(assertion, cfg);
        }

        throw new IllegalArgumentException("Could not extract or decrypt an assertion based on the security token provided");

    }

    private XMLObject getSecurityTokenFromRequestedToken(final RequestedSecurityToken reqToken,
                                                         final Collection<WsFederationConfiguration> config) {
        LOGGER.debug("Locating the first occurrence of a security token from the requested security token");
        XMLObject securityToken = getAssertionFromSecurityToken(reqToken);

        if (securityToken instanceof EncryptedData) {
            LOGGER.debug("Security token is encrypted. Attempting to decrypt to extract the assertion");
            final EncryptedData encryptedData = EncryptedData.class.cast(securityToken);

            final Iterator<WsFederationConfiguration> it = config.iterator();
            boolean found = false;
            while (!found && it.hasNext()) {
                try {
                    final WsFederationConfiguration c = it.next();
                    final Decrypter decrypter = buildAssertionDecrypter(c);
                    LOGGER.debug("Built an instance of [{}]", decrypter.getClass().getName());
                    securityToken = decrypter.decryptData(encryptedData);

                    LOGGER.debug("Decrypted assertion successfully");
                    found = true;
                } catch (final Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Could not extract or decrypt an assertion based on the security token provided");
            }
        }
        return securityToken;
    }

    /**
     * Gets assertion from security token.
     *
     * @param reqToken the req token
     * @return the assertion from security token
     */
    public XMLObject getAssertionFromSecurityToken(final RequestedSecurityToken reqToken) {
        return reqToken.getSecurityTokens().get(0);
    }

    /**
     * validateSignature checks to see if the signature on an assertion is valid.
     *
     * @param assertion a provided assertion
     * @return true if the assertion's signature is valid, otherwise false
     */
    public boolean validateSignature(final Pair<Assertion, WsFederationConfiguration> assertion) {

        if (assertion == null || assertion.getKey() == null || assertion.getValue() == null) {
            LOGGER.warn("No assertion or its configuration was provided to validate signatures");
            return false;
        }

        boolean valid = false;
        final Signature signature = assertion.getKey().getSignature();
        if (signature != null) {
            final SignaturePrevalidator validator = new SAMLSignatureProfileValidator();
            try {
                validator.validate(signature);

                final CriteriaSet criteriaSet = new CriteriaSet();
                criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
                criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
                criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));
                criteriaSet.add(new EntityIdCriterion(assertion.getValue().getIdentityProviderIdentifier()));

                try {
                    final SignatureTrustEngine engine = buildSignatureTrustEngine(assertion.getValue());
                    valid = engine.validate(signature, criteriaSet);
                } catch (final SecurityException e) {
                    LOGGER.warn(e.getMessage(), e);
                } finally {
                    if (!valid) {
                        LOGGER.error("Signature doesn't match any signing credential.");
                    }
                }

            } catch (final SignatureException e) {
                LOGGER.error("Failed to validate assertion signature", e);
            }
        }
        SamlUtils.logSamlObject(this.configBean, assertion.getKey());
        return valid;
    }

    /**
     * Build signature trust engine.
     *
     * @param wsFederationConfiguration the ws federation configuration
     * @return the signature trust engine
     */
    private static SignatureTrustEngine buildSignatureTrustEngine(final WsFederationConfiguration wsFederationConfiguration) {
        try {
            final List<Credential> signingWallet = wsFederationConfiguration.getSigningWallet();
            final CredentialResolver resolver = new
                    StaticCredentialResolver(signingWallet);
            final KeyInfoCredentialResolver keyResolver =
                    new StaticKeyInfoCredentialResolver(signingWallet);

            return new ExplicitKeySignatureTrustEngine(resolver, keyResolver);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setConfigBean(final OpenSamlConfigBean configBean) {
        this.configBean = configBean;
    }

    private static Credential getEncryptionCredential(final WsFederationConfiguration config) {
        try {
            // This will need to contain the private keypair in PEM format
            LOGGER.debug("Locating encryption credential private key [{}]", config.getEncryptionPrivateKey());
            final BufferedReader br = new BufferedReader(new InputStreamReader(
                    config.getEncryptionPrivateKey().getInputStream(), StandardCharsets.UTF_8));
            Security.addProvider(new BouncyCastleProvider());

            LOGGER.debug("Parsing credential private key");
            final PEMParser pemParser = new PEMParser(br);
            final Object privateKeyPemObject = pemParser.readObject();

            final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());

            final KeyPair kp;
            if (privateKeyPemObject instanceof PEMEncryptedKeyPair) {
                LOGGER.debug("Encryption private key is an encrypted keypair");
                final PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) privateKeyPemObject;
                final PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                        .build(config.getEncryptionPrivateKeyPassword().toCharArray());

                LOGGER.debug("Attempting to decrypt the encrypted keypair based on the provided encryption private key password");
                kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
            } else {
                LOGGER.debug("Extracting a keypair from the private key");
                kp = converter.getKeyPair((PEMKeyPair) privateKeyPemObject);
            }

            final X509CertParser certParser = new X509CertParser();
            // This is the certificate shared with ADFS in DER format, i.e certificate.crt
            LOGGER.debug("Locating encryption certificate [{}]", config.getEncryptionCertificate());
            certParser.engineInit(config.getEncryptionCertificate().getInputStream());

            LOGGER.debug("Invoking certificate engine to parse the certificate [{}]", config.getEncryptionCertificate());
            final X509CertificateObject cert = (X509CertificateObject) certParser.engineRead();
            LOGGER.debug("Creating final credential based on the certificate [{}] and the private key", cert.getIssuerDN());
            return new BasicX509Credential(cert, kp.getPrivate());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private static Decrypter buildAssertionDecrypter(final WsFederationConfiguration config) {
        final List<EncryptedKeyResolver> list = new ArrayList<>();
        list.add(new InlineEncryptedKeyResolver());
        list.add(new EncryptedElementTypeEncryptedKeyResolver());
        list.add(new SimpleRetrievalMethodEncryptedKeyResolver());

        LOGGER.debug("Built a list of encrypted key resolvers: [{}]", list);

        final ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver(list);

        LOGGER.debug("Building credential instance to decrypt data");
        final Credential encryptionCredential = getEncryptionCredential(config);
        final KeyInfoCredentialResolver resolver = new StaticKeyInfoCredentialResolver(encryptionCredential);
        final Decrypter decrypter = new Decrypter(null, resolver, encryptedKeyResolver);
        decrypter.setRootInNewDocument(true);
        return decrypter;
    }
}
