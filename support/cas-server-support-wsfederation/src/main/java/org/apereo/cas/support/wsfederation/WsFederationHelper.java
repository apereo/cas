package org.apereo.cas.support.wsfederation;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
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
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class that does the heavy lifting with the openSaml library.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class WsFederationHelper {

    private final OpenSamlConfigBean configBean;

    private final ServicesManager servicesManager;

    private Clock clock = Clock.systemUTC();

    /**
     * createCredentialFromToken converts a SAML 1.1 assertion to a WSFederationCredential.
     *
     * @param assertion the provided assertion
     * @return an equivalent credential.
     */
    public WsFederationCredential createCredentialFromToken(final Assertion assertion) {
        val retrievedOn = ZonedDateTime.now(clock);
        LOGGER.trace("Retrieved on [{}]", retrievedOn);

        val credential = new WsFederationCredential();
        credential.setRetrievedOn(retrievedOn);
        credential.setId(assertion.getID());
        credential.setIssuer(assertion.getIssuer());
        credential.setIssuedOn(DateTimeUtils.zonedDateTimeOf(assertion.getIssueInstant()));
        val conditions = assertion.getConditions();
        if (conditions != null) {
            credential.setNotBefore(DateTimeUtils.zonedDateTimeOf(conditions.getNotBefore()));
            credential.setNotOnOrAfter(DateTimeUtils.zonedDateTimeOf(conditions.getNotOnOrAfter()));
            if (!conditions.getAudienceRestrictionConditions().isEmpty()) {
                credential.setAudience(conditions.getAudienceRestrictionConditions().get(0).getAudiences().get(0).getURI());
            }
        }
        if (!assertion.getAuthenticationStatements().isEmpty()) {
            credential.setAuthenticationMethod(assertion.getAuthenticationStatements().get(0).getAuthenticationMethod());
        }
        val attributes = new HashMap<String, List<Object>>();
        assertion.getAttributeStatements()
            .stream()
            .flatMap(attributeStatement -> attributeStatement.getAttributes().stream())
            .forEach(item -> {
                LOGGER.trace("Processed attribute: [{}]", item.getAttributeName());
                final List<Object> itemList = item.getAttributeValues().stream()
                    .map(xmlObject -> ((XSAny) xmlObject).getTextContent())
                    .collect(Collectors.toList());
                if (!itemList.isEmpty()) {
                    attributes.put(item.getAttributeName(), itemList);
                }
            });
        credential.setAttributes(attributes);
        LOGGER.debug("WsFederation Credential retrieved as: [{}]", credential);
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
            val document = configBean.getParserPool().parse(in);
            val metadataRoot = document.getDocumentElement();
            val unmarshallerFactory = configBean.getUnmarshallerFactory();
            val unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);
            if (unmarshaller == null) {
                throw new IllegalArgumentException("Unmarshaller for the metadata root element cannot be determined");
            }
            LOGGER.debug("Unmarshalling the document into a security token response");
            val rsToken = (RequestSecurityTokenResponse) unmarshaller.unmarshall(metadataRoot);
            if (rsToken.getRequestedSecurityToken() == null) {
                throw new IllegalArgumentException("Request security token response is null");
            }
            LOGGER.debug("Locating list of requested security tokens");
            val rst = rsToken.getRequestedSecurityToken();
            if (rst.isEmpty()) {
                throw new IllegalArgumentException("No requested security token response is provided in the response");
            }
            LOGGER.debug("Locating the first occurrence of a requested security token in the list");
            val reqToken = rst.get(0);
            if (reqToken.getSecurityTokens() == null || reqToken.getSecurityTokens().isEmpty()) {
                throw new IllegalArgumentException("Requested security token response is not carrying any security tokens");
            }
            return reqToken;
        } catch (final Exception ex) {
            LoggingUtils.error(LOGGER, ex);
        }
        return null;
    }

    /**
     * converts a token into an assertion.
     *
     * @param reqToken the req token
     * @param config   the config
     * @param service  the service
     * @return an assertion
     */
    public Pair<Assertion, WsFederationConfiguration> buildAndVerifyAssertion(final RequestedSecurityToken reqToken,
                                                                              final Collection<WsFederationConfiguration> config,
                                                                              final Service service) {
        val securityToken = getSecurityTokenFromRequestedToken(reqToken, config);
        if (securityToken instanceof Assertion) {
            LOGGER.trace("Security token is an assertion.");
            val assertion = Assertion.class.cast(securityToken);
            LOGGER.debug("Extracted assertion successfully: [{}]", assertion);
            val cfg = config.stream()
                .filter(c -> StringUtils.isNotBlank(c.getIdentityProviderIdentifier()))
                .filter(c -> {
                    val id = c.getIdentityProviderIdentifier();
                    LOGGER.trace("Comparing identity provider identifier [{}] with assertion issuer [{}]", id, assertion.getIssuer());
                    return RegexUtils.find(id, assertion.getIssuer());
                })
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Could not locate wsfed configuration for security token provided. The assertion issuer "
                        + assertion.getIssuer() + " does not match any of the identity provider identifiers in the configuration"));

            return Pair.of(assertion, cfg);
        }
        throw new IllegalArgumentException("Could not extract or decrypt an assertion based on the security token provided");
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
     * @param resultPair a provided assertion
     * @return true if the assertion's signature is valid, otherwise false
     */
    public boolean validateSignature(final Pair<Assertion, WsFederationConfiguration> resultPair) {
        if (resultPair == null) {
            LOGGER.warn("No assertion or its configuration was provided to validate signatures");
            return false;
        }
        val configuration = resultPair.getValue();
        val assertion = resultPair.getKey();

        if (assertion == null || configuration == null) {
            LOGGER.warn("No signature or configuration was provided to validate signatures");
            return false;
        }
        val signature = assertion.getSignature();
        if (signature == null) {
            LOGGER.warn("No signature is attached to the assertion to validate");
            return false;
        }
        try {
            LOGGER.debug("Validating the signature...");
            val validator = new SAMLSignatureProfileValidator();
            validator.validate(signature);

            val criteriaSet = new CriteriaSet();
            criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
            criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
            criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));
            criteriaSet.add(new EntityIdCriterion(configuration.getIdentityProviderIdentifier()));
            val engine = buildSignatureTrustEngine(configuration);
            LOGGER.debug("Validating signature via trust engine for [{}]", configuration.getIdentityProviderIdentifier());
            return engine.validate(signature, criteriaSet);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, "Failed to validate assertion signature", e);
        }
        SamlUtils.logSamlObject(this.configBean, assertion);
        LOGGER.error("Signature doesn't match any signing credential and cannot be validated.");
        return false;
    }

    /**
     * Get the relying party id for a service.
     *
     * @param service       the service to get an id for
     * @param configuration the configuration
     * @return relying party id
     */
    public String getRelyingPartyIdentifier(final Service service, final WsFederationConfiguration configuration) {
        val relyingPartyIdentifier = configuration.getRelyingPartyIdentifier();
        if (service != null) {
            val registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            if (RegisteredServiceProperty.RegisteredServiceProperties.WSFED_RELYING_PARTY_ID.isAssignedTo(registeredService)) {
                LOGGER.debug("Determined relying party identifier from service [{}] to be [{}]", service, relyingPartyIdentifier);
                val propertyValue = RegisteredServiceProperty.RegisteredServiceProperties.WSFED_RELYING_PARTY_ID.getPropertyValue(registeredService);
                return propertyValue != null ? propertyValue.getValue() : relyingPartyIdentifier;
            }
        }
        LOGGER.debug("Determined relying party identifier to be [{}]", relyingPartyIdentifier);
        return relyingPartyIdentifier;
    }

    /**
     * Build signature trust engine.
     *
     * @param wsFederationConfiguration the ws federation configuration
     * @return the signature trust engine
     */
    @SneakyThrows
    private static SignatureTrustEngine buildSignatureTrustEngine(final WsFederationConfiguration wsFederationConfiguration) {
        val signingWallet = wsFederationConfiguration.getSigningWallet();
        LOGGER.debug("Building signature trust engine based on the following signing certificates:");
        signingWallet.forEach(c -> LOGGER.debug("Credential entity id [{}] with public key [{}]", c.getEntityId(), c.getPublicKey()));

        val resolver = new StaticCredentialResolver(signingWallet);
        val keyResolver = new StaticKeyInfoCredentialResolver(signingWallet);
        return new ExplicitKeySignatureTrustEngine(resolver, keyResolver);
    }

    /**
     * Gets encryption credential.
     * The encryption private key will need to contain the private keypair in PEM format.
     * The encryption certificate is shared with ADFS in DER format, i.e certificate.crt.
     *
     * @param config the config
     * @return the encryption credential
     */
    @SneakyThrows
    private static Credential getEncryptionCredential(final WsFederationConfiguration config) {
        LOGGER.debug("Locating encryption credential private key [{}]", config.getEncryptionPrivateKey());
        val br = new BufferedReader(new InputStreamReader(config.getEncryptionPrivateKey().getInputStream(), StandardCharsets.UTF_8));
        Security.addProvider(new BouncyCastleProvider());
        LOGGER.debug("Parsing credential private key");
        try (val pemParser = new PEMParser(br)) {
            val privateKeyPemObject = pemParser.readObject();
            val converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());

            val kp = FunctionUtils.doIf(Predicates.instanceOf(PEMEncryptedKeyPair.class),
                Unchecked.supplier(() -> {
                    LOGGER.debug("Encryption private key is an encrypted keypair");
                    val ckp = (PEMEncryptedKeyPair) privateKeyPemObject;
                    val decProv = new JcePEMDecryptorProviderBuilder().build(config.getEncryptionPrivateKeyPassword().toCharArray());
                    LOGGER.debug("Attempting to decrypt the encrypted keypair based on the provided encryption private key password");
                    return converter.getKeyPair(ckp.decryptKeyPair(decProv));
                }),
                Unchecked.supplier(() -> {
                    LOGGER.debug("Extracting a keypair from the private key");
                    return converter.getKeyPair((PEMKeyPair) privateKeyPemObject);
                }))
                .apply(privateKeyPemObject);

            val certParser = new X509CertParser();
            LOGGER.debug("Locating encryption certificate [{}]", config.getEncryptionCertificate());
            certParser.engineInit(config.getEncryptionCertificate().getInputStream());
            LOGGER.debug("Invoking certificate engine to parse the certificate [{}]", config.getEncryptionCertificate());
            val cert = (X509CertificateObject) certParser.engineRead();
            LOGGER.debug("Creating final credential based on the certificate [{}] and the private key", cert.getIssuerDN());
            return new BasicX509Credential(cert, kp.getPrivate());
        }

    }

    private static Decrypter buildAssertionDecrypter(final WsFederationConfiguration config) {
        val list = new ArrayList<EncryptedKeyResolver>(3);
        list.add(new InlineEncryptedKeyResolver());
        list.add(new EncryptedElementTypeEncryptedKeyResolver());
        list.add(new SimpleRetrievalMethodEncryptedKeyResolver());
        LOGGER.trace("Built a list of encrypted key resolvers: [{}]", list);
        val encryptedKeyResolver = new ChainingEncryptedKeyResolver(list);
        LOGGER.trace("Building credential instance to decrypt data");
        val encryptionCredential = getEncryptionCredential(config);
        val resolver = new StaticKeyInfoCredentialResolver(encryptionCredential);
        val decrypter = new Decrypter(null, resolver, encryptedKeyResolver);
        decrypter.setRootInNewDocument(true);
        return decrypter;
    }

    private XMLObject getSecurityTokenFromRequestedToken(final RequestedSecurityToken reqToken, final Collection<WsFederationConfiguration> config) {
        LOGGER.debug("Locating the first occurrence of a security token from the requested security token");
        val securityTokenFromAssertion = getAssertionFromSecurityToken(reqToken);

        val func = FunctionUtils.doIf(Predicates.instanceOf(EncryptedData.class),
            () -> {
                LOGGER.trace("Security token is encrypted. Attempting to decrypt to extract the assertion");
                val encryptedData = EncryptedData.class.cast(securityTokenFromAssertion);
                val it = config.iterator();
                while (it.hasNext()) {
                    try {
                        val c = it.next();
                        val decrypter = buildAssertionDecrypter(c);
                        LOGGER.trace("Built an instance of [{}]", decrypter.getClass().getName());
                        return decrypter.decryptData(encryptedData);
                    } catch (final Exception e) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                }
                LOGGER.error("Could not extract or decrypt an assertion based on the security token provided");
                return null;
            },
            () -> securityTokenFromAssertion);

        return func.apply(securityTokenFromAssertion);
    }
}
