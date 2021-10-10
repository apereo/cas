package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OidcCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcCoreProperties")
public class OidcCoreProperties implements Serializable {

    private static final long serialVersionUID = 823028615694269276L;

    /**
     * OIDC issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas/oidc";

    /**
     * Skew value used to massage the authentication issue instance.
     */
    @DurationCapable
    private String skew = "PT5M";

    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private String dynamicClientRegistrationMode;

    /**
     * Mapping of user-defined scopes. Key is the new scope name
     * and value is a comma-separated list of claims mapped to the scope.
     */
    private Map<String, String> userDefinedScopes = new HashMap<>(0);

    /**
     * Map fixed claims to CAS attributes.
     * Key is the existing claim name for a scope and value is the new attribute
     * that should take its place and value.
     */
    private Map<String, String> claimsMap = new HashMap<>(0);

    /**
     * A mapping of authentication context refs (ACR) values.
     * This is where specific authentication context classes
     * are referenced and mapped to providers that CAS may support
     * mainly for MFA purposes.
     * <p>
     * Example might be {@code acr-value->mfa-duo}.
     */
    private List<String> authenticationContextReferenceMappings = new ArrayList<>(0);

    /**
     * As per OpenID Connect Core section 5.4, "The Claims requested by the {@code profile},
     * {@code email}, {@code address}, and {@code phone} scope values are returned from
     * the userinfo endpoint", except for {@code response_type}={@code id_token},
     * where they are returned in the id_token (as there is no
     * access token issued that could be used to access the userinfo endpoint).
     * The Claims requested by the profile, email, address, and phone scope values
     * are returned from the userinfo endpoint when a {@code response_type} value is
     * used that results in an access token being issued. However, when no
     * access token is issued (which is the case for the {@code response_type}
     * value {@code id_token}), the resulting Claims are returned in the ID Token.
     * <p>
     * Setting this flag to true will force CAS to include claims in the ID token
     * regardless of the response type. Note that this setting <strong>MUST ONLY</strong> be used
     * as a last resort, to stay compliant with the specification as much as possible.
     * <strong>DO NOT</strong> use this setting without due consideration.
     * <p>
     * Note that this setting is set to {@code true} by default mainly
     * provided to preserve backward compatibility with
     * previous CAS versions that included claims into the ID token without considering
     * the response type. The behavior of this setting may change and it may be removed
     * in future CAS releases.
     */
    private boolean includeIdTokenClaims = true;
}
