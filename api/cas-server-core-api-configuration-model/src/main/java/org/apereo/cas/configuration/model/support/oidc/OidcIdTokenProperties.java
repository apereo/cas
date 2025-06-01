package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcIdTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcIdTokenProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 813328615694269276L;

    /**
     * Hard timeout to kill the ID token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT8H";

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
