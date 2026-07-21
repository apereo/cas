package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.configuration.model.support.oauth.OAuthCoreProperties;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

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

    private List<OAuthRegisteredServiceClientSecret> clientSecrets = new ArrayList<>();

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
        if (scopes == null) {
            scopes = new HashSet<>();
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
        scopes = ObjectUtils.getIfNull(scopes, new HashSet<>());
        audience = ObjectUtils.getIfNull(audience, new HashSet<>());
        clientSecrets = ObjectUtils.getIfNull(clientSecrets, new ArrayList<>());
    }

    /**
     * Sets client secret and translates to {@link #clientSecrets}.
     * Mainly kept for backward compatibility.
     * @param clientSecret the client secret
     */
    @JsonSetter("clientSecret")
    public void setClientSecret(final String clientSecret) {
        clientSecrets = ObjectUtils.getIfNull(clientSecrets, new ArrayList<>());
        clientSecrets.add(OAuthRegisteredServiceClientSecret.withoutExpiration(clientSecret));
    }

    /**
     * Gets the first non-expiring client secret.
     *
     * @return the client secret
     */
    @JsonIgnore
    public String getClientSecret() {
        clientSecrets = ObjectUtils.getIfNull(clientSecrets, new ArrayList<>());
        if (clientSecrets.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return clientSecrets
            .stream()
            .filter(secret -> !secret.hasClientSecretExpired(this))
            .findFirst()
            .map(OAuthRegisteredServiceClientSecret::getValue)
            .orElse(StringUtils.EMPTY);
    }
}
