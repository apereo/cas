package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.model.TriStateBoolean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link Pac4jSamlClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jSamlClientProperties extends Pac4jBaseClientProperties {

    private static final long serialVersionUID = -862819796533384951L;

    /**
     * The destination binding to use
     * when creating authentication requests.
     */
    private String destinationBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";

    /**
     * The password to use when generating the SP/CAS keystore.
     */
    @RequiredProperty
    private String keystorePassword;

    /**
     * The password to use when generating the private key for the SP/CAS keystore.
     */
    @RequiredProperty
    private String privateKeyPassword;

    /**
     * Location of the keystore to use and generate the SP/CAS keystore.
     */
    @RequiredProperty
    private String keystorePath = Beans.getTempFilePath("samlSpKeystore", ".jks");

    /**
     * The metadata location of the identity provider that is to handle authentications.
     */
    @RequiredProperty
    private String identityProviderMetadataPath;

    /**
     * Once you have an authenticated session on the identity provider, usually it won't prompt you again to enter your
     * credentials and it will automatically generate a new assertion for you. By default, the SAML client
     * will accept assertions based on a previous authentication for one hour.
     * You can adjust this behavior by modifying this setting. The unit of time here is seconds.
     */
    private int maximumAuthenticationLifetime = 3600;

    /**
     * Maximum skew in seconds between SP and IDP clocks.
     * This skew is added onto the {@code NotOnOrAfter} field in seconds
     * for the SAML response validation.
     */
    private int acceptedSkew = 300;

    /**
     * Describes the map of attributes that are to be fetched from the credential (map keys)
     * and then transformed/renamed using map values before they are put into a profile.
     * An example might be to fetch {@code givenName} from credential and rename it to {@code urn:oid:2.5.4.42} or vice versa.
     * Note that this setting only applies to attribute names, and not friendly-names.
     */
    private List<ServiceProviderMappedAttribute> mappedAttributes = new ArrayList<>(0);

    /**
     * The entity id of the SP/CAS that is used in the SP metadata generation process.
     */
    @RequiredProperty
    private String serviceProviderEntityId = "https://apereo.org/cas/samlsp";

    /**
     * Location of the SP metadata to use and generate.
     */
    @RequiredProperty
    private String serviceProviderMetadataPath = Beans.getTempFilePath("samlSpMetadata", ".xml");

    /**
     * Whether authentication requests should be tagged as forced auth.
     */
    private boolean forceAuth;

    /**
     * Whether authentication requests should be tagged as passive.
     */
    private boolean passive;

    /**
     * Requested authentication context class in authn requests.
     */
    private List<String> authnContextClassRef = new ArrayList<>(0);

    /**
     * Specifies the comparison rule that should be used to evaluate the specified authentication methods.
     * For example, if exact is specified, the authentication method used must match one of the authentication
     * methods specified by the AuthnContextClassRef elements.
     * AuthContextClassRef element require comparison rule to be used to evaluate the specified
     * authentication methods. If not explicitly specified "exact" rule will be used by default.
     * Other acceptable values are minimum, maximum, better.
     */
    private String authnContextComparisonType = "exact";

    /**
     * Force generation of the keystore.
     */
    private boolean forceKeystoreGeneration;

    /**
     * The key alias used in the keystore.
     */
    private String keystoreAlias;

    /**
     * A name to append to signing certificates generated.
     * The named part appended can be useful to identify for which clientName it was generated
     * If no name is provided the default certificate name will be used.
     */
    private String certificateNameToAppend;

    /**
     * NameID policy to request in the authentication requests.
     */
    private String nameIdPolicyFormat;

    /**
     * Flag to indicate whether the allow-create flags
     * for nameid policies should be set to true, false or ignored/defined.
     */
    private TriStateBoolean nameIdPolicyAllowCreate = TriStateBoolean.TRUE;

    /**
     * Whether metadata should be marked to request sign assertions.
     */
    private boolean wantsAssertionsSigned;
    
    /**
     * Whether a response has to be mandatory signed.
     */
    private boolean wantsResponsesSigned;

    /**
     * Whether the signature validation should be disabled.
     * Never set this property to {@code true} in production.
     */
    private boolean allSignatureValidationDisabled;

    /**
     * AttributeConsumingServiceIndex attribute of AuthnRequest element.
     * The given index points out a specific AttributeConsumingService structure, declared into the
     * Service Provider (SP)'s metadata, to be used to specify all the attributes that the Service Provider
     * is asking to be released within the authentication assertion returned by the Identity Provider (IdP).
     * This attribute won't be sent with the request unless a positive value (including 0) is defined.
     */
    private int attributeConsumingServiceIndex;

    /**
     * Allows the SAML client to select a specific ACS url from the metadata, if defined.
     * A negative value de-activates the selection process and is the default.
     */
    private int assertionConsumerServiceIndex = -1;

    /**
     * Whether name qualifiers should be produced
     * in the final saml response.
     */
    private boolean useNameQualifier = true;

    /**
     * The attribute found in the saml response
     * that may be used to establish the authenticated
     * user and build a profile for CAS.
     */
    private String principalIdAttribute;

    /**
     * Whether or not SAML SP metadata should be signed when generated.
     */
    private boolean signServiceProviderMetadata;

    /**
     * Whether or not the authnRequest should be signed.
     */
    private boolean signAuthnRequest;

    /**
     * Whether or not the Logout Request sent from the SP should be signed.
     */
    private boolean signServiceProviderLogoutRequest;

    /**
     * List of attributes requested by the service provider
     * that would be put into the service provider metadata.
     */
    private List<ServiceProviderRequestedAttribute> requestedAttributes = new ArrayList<>(0);

    /**
     * Collection of signing signature blacklisted algorithms, if any, to override the global defaults.
     */
    private List<String> blackListedSignatureSigningAlgorithms = new ArrayList<>(0);

    /**
     * Collection of signing signature algorithms, if any, to override the global defaults.
     */
    private List<String> signatureAlgorithms = new ArrayList<>(0);

    /**
     * Collection of signing signature reference digest methods, if any, to override the global defaults.
     */
    private List<String> signatureReferenceDigestMethods = new ArrayList<>(0);

    /**
     * The signing signature canonicalization algorithm, if any, to override the global defaults.
     */
    private String signatureCanonicalizationAlgorithm;

    /**
     * Provider name set for the saml authentication request.
     * Sets the human-readable name of the requester for use by
     * the presenter's user agent or the identity provider.
     */
    private String providerName;

    /**
     * Factory implementing this interface provides services for storing and retrieval of SAML messages for
     * e.g. verification of retrieved responses. The default factory is an always empty store.
     * You may choose {@code org.pac4j.saml.store.HttpSessionStore} instead which allows SAML messages to be stored in a distributed session store
     * specially required for high availability deployments and validation operations.
     */
    private String messageStoreFactory = "org.pac4j.saml.store.EmptyStoreFactory";

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ServiceProviderRequestedAttribute implements Serializable {
        private static final long serialVersionUID = -862819796533384951L;

        /**
         * Attribute name.
         */
        private String name;

        /**
         * Attribute friendly name.
         */
        private String friendlyName;

        /**
         * Attribute name format.
         */
        private String nameFormat = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";

        /**
         * Whether this attribute is required and should
         * be marked so in the metadata.
         */
        private boolean required;
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ServiceProviderMappedAttribute implements Serializable {
        private static final long serialVersionUID = -762819796533384951L;

        /**
         * Attribute name.
         */
        private String name;

        /**
         * The name that should be used to rename {@link #name}.
         */
        private String mappedTo;
    }
}
