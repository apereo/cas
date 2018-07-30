package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Capture SAML authentication metadata.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
public class SamlAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    /**
     * The Constant ATTRIBUTE_AUTHENTICATION_METHOD.
     */
    public static final String ATTRIBUTE_AUTHENTICATION_METHOD = "samlAuthenticationStatementAuthMethod";

    /**
     * The Constant AUTHN_METHOD_PASSWORD.
     */
    public static final String AUTHN_METHOD_PASSWORD = "urn:oasis:names:tc:SAML:1.0:am:password";

    /**
     * The Constant AUTHN_METHOD_SSL_TLS_CLIENT.
     */
    public static final String AUTHN_METHOD_SSL_TLS_CLIENT = "urn:ietf:rfc:2246";

    /**
     * The Constant AUTHN_METHOD_X509_PUBLICKEY.
     */
    public static final String AUTHN_METHOD_X509_PUBLICKEY = "urn:oasis:names:tc:SAML:1.0:am:X509-PKI";

    /**
     * The Constant AUTHN_METHOD_UNSPECIFIED.
     */
    public static final String AUTHN_METHOD_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.0:am:unspecified";

    private final Map<String, String> authenticationMethods = new HashMap<>();

    /**
     * Instantiates a new SAML authentication meta data populator.
     */
    public SamlAuthenticationMetaDataPopulator() {
        this.authenticationMethods.put(HttpBasedServiceCredential.class.getName(), AUTHN_METHOD_SSL_TLS_CLIENT);
        this.authenticationMethods.put(UsernamePasswordCredential.class.getName(), AUTHN_METHOD_PASSWORD);
        this.authenticationMethods.put("org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential", AUTHN_METHOD_X509_PUBLICKEY);
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(c -> {
            final String credentialsClass = c.getClass().getName();
            final String authenticationMethod = this.authenticationMethods.getOrDefault(credentialsClass, AUTHN_METHOD_UNSPECIFIED);
            builder.addAttribute(ATTRIBUTE_AUTHENTICATION_METHOD, authenticationMethod);
        });

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
}
