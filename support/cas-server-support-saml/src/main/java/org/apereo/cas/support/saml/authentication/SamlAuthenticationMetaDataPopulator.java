package org.apereo.cas.support.saml.authentication;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthenticationMetaDataPopulator to retrieve the Authentication Type.
 * <p>
 * Note: Authentication Methods are exposed under the key:
 * {@code samlAuthenticationStatement::authMethod} in the Authentication
 * attributes map.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */

public class SamlAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    /** The Constant ATTRIBUTE_AUTHENTICATION_METHOD. */
    public static final String ATTRIBUTE_AUTHENTICATION_METHOD = "samlAuthenticationStatementAuthMethod";

    /** The Constant AUTHN_METHOD_PASSWORD. */
    public static final String AUTHN_METHOD_PASSWORD = "urn:oasis:names:tc:SAML:1.0:am:password";

    /** The Constant AUTHN_METHOD_SSL_TLS_CLIENT. */
    public static final String AUTHN_METHOD_SSL_TLS_CLIENT = "urn:ietf:rfc:2246";

    /** The Constant AUTHN_METHOD_X509_PUBLICKEY. */
    public static final String AUTHN_METHOD_X509_PUBLICKEY = "urn:oasis:names:tc:SAML:1.0:am:X509-PKI";

    /** The Constant AUTHN_METHOD_UNSPECIFIED. */
    public static final String AUTHN_METHOD_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.0:am:unspecified";

    private final Map<String, String> authenticationMethods = new HashMap<>();

    /**
     * Instantiates a new SAML authentication meta data populator.
     */
    public SamlAuthenticationMetaDataPopulator() {
        this.authenticationMethods.put(
                HttpBasedServiceCredential.class.getName(),
                AUTHN_METHOD_SSL_TLS_CLIENT);
        this.authenticationMethods.put(
                UsernamePasswordCredential.class.getName(),
                AUTHN_METHOD_PASSWORD);

        // Next two classes are in other modules, so avoid using Class#getName() to prevent circular dependency
        this.authenticationMethods.put(
                "org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentials",
                AUTHN_METHOD_UNSPECIFIED);
        this.authenticationMethods.put(
                "org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredentials",
                AUTHN_METHOD_X509_PUBLICKEY);
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final String credentialsClass = transaction.getCredential().getClass().getName();
        final String authenticationMethod = this.authenticationMethods.get(credentialsClass);
        builder.addAttribute(ATTRIBUTE_AUTHENTICATION_METHOD, authenticationMethod);
    }

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }

    /**
     * Map of user-defined mappings. Note it is possible to override the
     * defaults. Mapping should be of the following type:
     * <pre>Package/Class Name as String -&gt; Name SAML Type</pre>
     * <p>
     * Example: ({@code "HttpBasedServiceCredential"
     * -&gt; SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client})
     *
     * @param userDefinedMappings map of user defined authentication types.
     */
    public void setUserDefinedMappings(final Map<String, String> userDefinedMappings) {
        this.authenticationMethods.putAll(userDefinedMappings);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("authenticationMethods", authenticationMethods)
                .toString();
    }
}
