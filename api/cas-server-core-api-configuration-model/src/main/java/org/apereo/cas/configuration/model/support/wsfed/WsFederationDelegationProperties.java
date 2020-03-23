package org.apereo.cas.configuration.model.support.wsfed;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link WsFederationDelegationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-wsfederation-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class WsFederationDelegationProperties implements Serializable {

    private static final long serialVersionUID = 5743971334977239938L;

    /**
     * The attribute extracted from the assertion and used to construct the CAS principal id.
     */
    @RequiredProperty
    private String identityAttribute = "upn";

    /**
     * The entity id or the identifier of the Wsfed instance.
     */
    @RequiredProperty
    private String identityProviderIdentifier = "http://adfs.example.org/adfs/services/trust";

    /**
     * Wsfed identity provider url.
     */
    @RequiredProperty
    private String identityProviderUrl = "https://adfs.example.org/adfs/ls/";

    /**
     * Locations of signing certificates used to verify assertions.
     */
    @RequiredProperty
    private String signingCertificateResources = "classpath:adfs-signing.crt";

    /**
     * The identifier for CAS (RP) registered with wsfed.
     */
    @RequiredProperty
    private String relyingPartyIdentifier = "urn:cas:localhost";

    /**
     * Tolerance value used to skew assertions to support clock drift.
     */
    private String tolerance = "PT10S";

    /**
     * Indicates how attributes should be recorded into the principal object.
     * Useful if you wish to additionally resolve attributes on top of what wsfed provides.
     * Accepted values are {@code CAS,WSFED,BOTH}.
     */
    private String attributesType = "WSFED";

    /**
     * Whether CAS should enable its own attribute resolution machinery
     * after having received a response from wsfed.
     */
    private boolean attributeResolverEnabled = true;

    /**
     * Whether CAS should auto redirect to this wsfed instance.
     */
    private boolean autoRedirect = true;

    /**
     * The path to the private key used to handle and verify encrypted assertions.
     */
    private String encryptionPrivateKey = "classpath:private.key";

    /**
     * The path to the public key/certificate used to handle and verify encrypted assertions.
     */
    private String encryptionCertificate = "classpath:certificate.crt";

    /**
     * The private key password.
     */
    private String encryptionPrivateKeyPassword = "NONE";


    /**
     * Principal resolution settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * The order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Path to attribute mutator groovy script
     * that allows one to modify wsfed attributes before
     * establishing a final principal.
     */
    private Groovy attributeMutatorScript = new Groovy();

    /**
     * Signing/encryption settings related to managing the cookie that is used to keep track of the session.
     */
    @NestedConfigurationProperty
    private WsFederationDelegatedCookieProperties cookie = new WsFederationDelegatedCookieProperties();

    @RequiresModule(name = "cas-server-support-wsfederation-webflow", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 8079027843747126083L;
    }
}
