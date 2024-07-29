package org.apereo.cas.configuration.model.support.token;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link TokenAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-token-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class TokenAuthenticationProperties implements Serializable, CasFeatureModule {

    @Serial
    private static final long serialVersionUID = 6016124091895278265L;

    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    @RegularExpressionCapable
    private String credentialCriteria;

    /**
     * Crypto settings.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(100);

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;

    /**
     * Order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * When set to true, will generate a token and store it as an authentication
     * attribute into the single sign-on session. This token can be used later on
     * for token-based authentication attempts, and should allow SSO access to the server.
     */
    private boolean ssoTokenEnabled;

    public TokenAuthenticationProperties() {
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
