package org.apereo.cas.configuration.model.support.pac4j;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

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
    private String keystorePath;

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
    private int maximumAuthenticationLifetime = 600;

    /**
     * The entity id of the SP/CAS that is used in the SP metadata generation process.
     */
    @RequiredProperty
    private String serviceProviderEntityId;

    /**
     * Location of the SP metadata to use and generate.
     */
    @RequiredProperty
    private String serviceProviderMetadataPath;

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
    private String authnContextClassRef;

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
     * The key alias used in the keystore.
     */
    private String keystoreAlias;

    /**
     * NameID policy to request in the authentication requests.
     */
    private String nameIdPolicyFormat;

    /**
     * Whether metadata should be marked to request sign assertions.
     */
    private boolean wantsAssertionsSigned;

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
     * Whether or not SAML SP metadata should be signed when generated.
     */
    private boolean signServiceProviderMetadata;

    /**
     * List of attributes requested by the service provider
     * that would be put into the service provider metadata.
     */
    private List<ServiceProviderRequestedAttribute> requestedAttributes = new ArrayList<>();

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
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
}
