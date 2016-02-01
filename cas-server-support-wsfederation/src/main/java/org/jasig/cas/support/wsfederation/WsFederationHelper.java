package org.jasig.cas.support.wsfederation;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.Attribute;
import org.opensaml.saml.saml1.core.Conditions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.soap.wsfed.RequestSecurityTokenResponse;
import org.opensaml.soap.wsfed.RequestedSecurityToken;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignaturePrevalidator;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper class that does the heavy lifting with the openSaml library.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Component("wsFederationHelper")
public final class WsFederationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(WsFederationHelper.class);

    @Autowired
    @NotNull
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
        final DateTime retrievedOn = new DateTime().withZone(DateTimeZone.UTC);
        LOGGER.debug("Retrieved on {}", retrievedOn);

        final WsFederationCredential credential = new WsFederationCredential();
        credential.setRetrievedOn(retrievedOn);
        credential.setId(assertion.getID());
        credential.setIssuer(assertion.getIssuer());
        credential.setIssuedOn(assertion.getIssueInstant());

        final Conditions conditions = assertion.getConditions();
        if (conditions != null) {
            credential.setNotBefore(conditions.getNotBefore());
            credential.setNotOnOrAfter(conditions.getNotOnOrAfter());
            credential.setAudience(conditions.getAudienceRestrictionConditions().get(0).getAudiences().get(0).getUri());
        }

        if (!assertion.getAuthenticationStatements().isEmpty()) {
            credential.setAuthenticationMethod(assertion.getAuthenticationStatements().get(0).getAuthenticationMethod());
        }

        //retrieve an attributes from the assertion
        final HashMap<String, List<Object>> attributes = new HashMap<>();
        for (final Attribute item : assertion.getAttributeStatements().get(0).getAttributes()) {
            LOGGER.debug("Processed attribute: {}", item.getAttributeName());

            final List<Object> itemList = new ArrayList<>();
            for (int i = 0; i < item.getAttributeValues().size(); i++) {
                itemList.add(((XSAny) item.getAttributeValues().get(i)).getTextContent());
            }

            if (!itemList.isEmpty()) {
                attributes.put(item.getAttributeName(), itemList);
            }
        }
        credential.setAttributes(attributes);
        LOGGER.debug("Credential: {}", credential);
        return credential;
    }



    /**
     * parseTokenFromString converts a raw wresult and extracts it into an assertion.
     *
     * @param wresult the raw token returned by the IdP
     * @return an assertion
     */
    public Assertion parseTokenFromString(final String wresult) {
        try (final InputStream in = new ByteArrayInputStream(wresult.getBytes("UTF-8"))) {

            final Document document = configBean.getParserPool().parse(in);
            final Element metadataRoot = document.getDocumentElement();
            final UnmarshallerFactory unmarshallerFactory = configBean.getUnmarshallerFactory();
            final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);
            if (unmarshaller == null) {
                throw new IllegalArgumentException("Unmarshaller for the metadata root element cannot be determined");
            }

            final RequestSecurityTokenResponse rsToken = (RequestSecurityTokenResponse) unmarshaller.unmarshall(metadataRoot);

            //Get our SAML token
            final List<RequestedSecurityToken> rst = rsToken.getRequestedSecurityToken();
            final Assertion assertion = (Assertion) rst.get(0).getSecurityTokens().get(0);

            if (assertion == null) {
                LOGGER.debug("Assertion is null");
            } else {
                LOGGER.debug("Assertion: {}", assertion);
            }
            return assertion;
        } catch (final Exception ex) {
            LOGGER.warn(ex.getMessage());
            return null;
        }
    }

    /**
     * validateSignature checks to see if the signature on an assertion is valid.
     *
     * @param assertion a provided assertion
     * @param wsFederationConfiguration WS-Fed configuration provided.
     * @return true if the assertion's signature is valid, otherwise false
     */
    public boolean validateSignature(final Assertion assertion,
                                     final WsFederationConfiguration wsFederationConfiguration) {

        if (assertion == null) {
            LOGGER.warn("No assertion was provided to validate signatures");
            return false;
        }

        boolean valid = false;
        if (assertion.getSignature() != null) {
            final SignaturePrevalidator validator = new SAMLSignatureProfileValidator();
            try {
                validator.validate(assertion.getSignature());

                final CriteriaSet criteriaSet = new CriteriaSet();
                criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
                criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
                criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));
                criteriaSet.add(new EntityIdCriterion(wsFederationConfiguration.getIdentityProviderIdentifier()));

                try {
                    final SignatureTrustEngine engine = buildSignatureTrustEngine(wsFederationConfiguration);
                    valid = engine.validate(assertion.getSignature(), criteriaSet);
                } catch (final SecurityException e) {
                    LOGGER.warn(e.getMessage(), e);
                } finally {
                    if (!valid) {
                        LOGGER.warn("Signature doesn't match any signing credential.");
                    }
                }

            } catch (final SignatureException e) {
                LOGGER.warn("Failed to validate assertion signature", e);
            }
        }
        return valid;
    }

    /**
     * Build signature trust engine.
     *
     * @param wsFederationConfiguration the ws federation configuration
     * @return the signature trust engine
     */
    private SignatureTrustEngine buildSignatureTrustEngine(final WsFederationConfiguration wsFederationConfiguration) {
        try {
            final CredentialResolver resolver = new
                    StaticCredentialResolver(wsFederationConfiguration.getSigningCertificates());
            final KeyInfoCredentialResolver keyResolver =
                    new StaticKeyInfoCredentialResolver(wsFederationConfiguration.getSigningCertificates());

            return new ExplicitKeySignatureTrustEngine(resolver, keyResolver);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
