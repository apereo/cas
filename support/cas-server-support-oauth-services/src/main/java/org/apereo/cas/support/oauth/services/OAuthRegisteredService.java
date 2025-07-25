package org.apereo.cas.support.oauth.services;

import org.apereo.cas.configuration.model.support.oauth.OAuthCoreProperties;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

/**
 * An extension of the {@link BaseRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OAuthRegisteredService extends BaseWebBasedRegisteredService {
    /**
     * Friendly name for this OAuth service.
     */
    public static final String OAUTH_REGISTERED_SERVICE_FRIENDLY_NAME = "OAuth2 Client";

    @Serial
    private static final long serialVersionUID = 5318897374067731021L;

    private String clientSecret;

    private String clientId;

    private boolean bypassApprovalPrompt;

    private boolean generateRefreshToken;

    private boolean renewRefreshToken;

    private boolean jwtAccessToken;
    
    private boolean jwtRefreshToken;

    private String jwtAccessTokenSigningAlg;

    private Set<String> audience = new HashSet<>();
    
    private RegisteredServiceOAuthCodeExpirationPolicy codeExpirationPolicy;

    private RegisteredServiceOAuthAccessTokenExpirationPolicy accessTokenExpirationPolicy;

    private RegisteredServiceOAuthRefreshTokenExpirationPolicy refreshTokenExpirationPolicy;

    private RegisteredServiceOAuthDeviceTokenExpirationPolicy deviceTokenExpirationPolicy;

    private RegisteredServiceOAuthTokenExchangePolicy tokenExchangePolicy;

    private Set<String> supportedGrantTypes = new HashSet<>();

    private Set<String> supportedResponseTypes = new HashSet<>();

    private OAuthCoreProperties.UserProfileViewTypes userProfileViewType;

    private Set<String> scopes = new HashSet<>();

    private String responseMode;

    private String introspectionSignedResponseAlg = "RS512";

    private String introspectionEncryptedResponseAlg;

    private String introspectionEncryptedResponseEncoding;

    private String tokenEndpointAuthenticationMethod;

    private String tlsClientAuthSubjectDn;

    private String tlsClientAuthSanDns;

    private String tlsClientAuthSanUri;

    private String tlsClientAuthSanIp;

    private String tlsClientAuthSanEmail;

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return OAUTH_REGISTERED_SERVICE_FRIENDLY_NAME;
    }

    @JsonIgnore
    @Override
    public int getEvaluationPriority() {
        return 2;
    }

    /**
     * Gets scopes.
     *
     * @return the scopes
     */
    public Set<String> getScopes() {
        if (this.scopes == null) {
            this.scopes = new HashSet<>();
        }
        return scopes;
    }

    /**
     * Sets scopes.
     *
     * @param scopes the scopes
     */
    public void setScopes(final Set<String> scopes) {
        getScopes().clear();
        getScopes().addAll(scopes);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.scopes = ObjectUtils.getIfNull(this.scopes, new HashSet<>());
        this.audience = ObjectUtils.getIfNull(this.audience, new HashSet<>());
    }
}
